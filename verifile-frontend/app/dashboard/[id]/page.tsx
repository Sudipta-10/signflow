"use client";
import { useEffect, useState, useRef } from "react";
import { getDocumentById, downloadSignedDocument, DocumentDetailDto } from "../../lib/api";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";

export default function DocumentDetail() {
    const router = useRouter();
    const params = useParams();
    const documentId = params.id as string;

    const [doc, setDoc] = useState<DocumentDetailDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [downloading, setDownloading] = useState(false);

    // Signature Modal State
    const [showModal, setShowModal] = useState(false);
    const [fullName, setFullName] = useState("Sudipta Pratiher");
    const [initials, setInitials] = useState("SP");
    const [activeTab, setActiveTab] = useState("Signature");
    const [activeSideTab, setActiveSideTab] = useState("Text");
    const [selectedStyle, setSelectedStyle] = useState(0);
    const [selectedColor, setSelectedColor] = useState("#333333");
    const [uploadedSigUrl, setUploadedSigUrl] = useState<string | null>(null);
    const [isDrawing, setIsDrawing] = useState(false);

    const canvasRef = useRef<HTMLCanvasElement>(null);
    const sigInputRef = useRef<HTMLInputElement>(null);

    const fontStyles = [
        { name: "'Caveat', cursive", url: "https://fonts.googleapis.com/css2?family=Caveat:wght@600&display=swap" },
        { name: "'Dancing Script', cursive", url: "https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&display=swap" },
        { name: "'Pacifico', cursive", url: "https://fonts.googleapis.com/css2?family=Pacifico&display=swap" },
        { name: "'Great Vibes', cursive", url: "https://fonts.googleapis.com/css2?family=Great+Vibes&display=swap" }
    ];

    const signatureColors = ["#333333", "#e53935", "#1e88e5", "#43a047"];

    const fetchDoc = async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            router.push("/login");
            return;
        }
        try {
            const data = await getDocumentById(documentId, token);
            setDoc(data);
        } catch (err) {
            alert("Failed to load document details");
            router.push("/dashboard");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDoc();
    }, [documentId]);

    // Initialize canvas context
    useEffect(() => {
        if (showModal && activeSideTab === "Draw" && canvasRef.current) {
            const canvas = canvasRef.current;
            const ctx = canvas.getContext("2d");
            if (ctx) {
                const rect = canvas.parentElement?.getBoundingClientRect();
                if (rect) {
                    canvas.width = rect.width;
                    canvas.height = rect.height;
                    ctx.scale(1, 1);
                }
                ctx.lineCap = "round";
                ctx.lineJoin = "round";
                ctx.lineWidth = 3;
                ctx.strokeStyle = selectedColor;
            }
        }
    }, [showModal, activeSideTab, selectedColor]);

    const handleSignClick = () => {
        setShowModal(true);
    };

    const handleDownload = async () => {
        const token = localStorage.getItem("token");
        if (!token || !doc?.isSigned) return;

        setDownloading(true);
        try {
            await downloadSignedDocument(documentId, token);
        } catch (err) {
            alert("Failed to download document.");
        } finally {
            setDownloading(false);
        }
    };

    const executeSignAndEdit = async () => {
        let signatureBase64 = "";

        try {
            if (activeSideTab === "Draw") {
                const canvas = canvasRef.current;
                if (canvas) signatureBase64 = canvas.toDataURL("image/png");
            } else if (activeSideTab === "Text") {
                const canvas = document.createElement("canvas");
                canvas.width = 400;
                canvas.height = 150;
                const ctx = canvas.getContext("2d");
                if (ctx) {
                    ctx.fillStyle = selectedColor;
                    const fontName = fontStyles[selectedStyle].name.split(",")[0].replace(/'/g, "");
                    ctx.font = `60px ${fontName}, cursive`;
                    ctx.textBaseline = "middle";
                    ctx.textAlign = "center";
                    ctx.fillText(fullName || "Sudipta Pratiher", 200, 75);
                    signatureBase64 = canvas.toDataURL("image/png");
                }
            } else if (activeSideTab === "Upload" && sigInputRef.current?.files?.[0]) {
                const file = sigInputRef.current.files[0];
                const reader = new FileReader();
                signatureBase64 = await new Promise((resolve) => {
                    reader.onloadend = () => resolve(reader.result as string);
                    reader.readAsDataURL(file);
                });
            }

            if (signatureBase64) {
                sessionStorage.setItem("current_signature", signatureBase64);
            }
        } catch (e) {
            console.error("Failed to generate signature", e);
        }

        setShowModal(false);
        router.push(`/dashboard/editor/${documentId}`);
    };

    // Drawing handlers
    const startDrawing = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        ctx.strokeStyle = selectedColor;
        const rect = canvas.getBoundingClientRect();
        const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
        const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
        ctx.beginPath();
        ctx.moveTo(clientX - rect.left, clientY - rect.top);
        setIsDrawing(true);
    };

    const draw = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
        if (!isDrawing || !canvasRef.current) return;
        if ('touches' in e && e.cancelable) e.preventDefault();
        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        const rect = canvas.getBoundingClientRect();
        const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
        const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
        ctx.lineTo(clientX - rect.left, clientY - rect.top);
        ctx.stroke();
    };

    const stopDrawing = () => setIsDrawing(false);

    const clearCanvas = () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    };

    const handleSigFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) setUploadedSigUrl(URL.createObjectURL(file));
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-purple-950 via-[#1a0b2e] to-black text-white flex items-center justify-center">
                <div className="w-8 h-8 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!doc) return null;

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-950 via-[#1a0b2e] to-black text-white selection:bg-purple-500/30 font-sans">
            <style dangerouslySetInnerHTML={{
                __html: `
                @import url('https://fonts.googleapis.com/css2?family=Caveat:wght@600&family=Dancing+Script:wght@600&family=Great+Vibes&family=Pacifico&display=swap');
            `}} />

            <nav className="flex items-center px-8 py-4 border-b border-purple-500/20 backdrop-blur-xl sticky top-0 z-50 bg-purple-900/10">
                <Link href="/dashboard" className="flex items-center gap-2 text-zinc-400 hover:text-white transition-colors">
                    <span className="text-xl">←</span> Back to Dashboard
                </Link>
            </nav>

            <main className="max-w-4xl mx-auto p-8 pt-12 relative">
                <div className="bg-zinc-900/50 border border-white/10 rounded-3xl p-8 mb-8 backdrop-blur-sm">
                    <div className="flex flex-col md:flex-row md:items-start justify-between gap-6 mb-8">
                        <div>
                            <div className="inline-flex items-center px-3 py-1 rounded-full bg-zinc-800 text-xs font-medium text-zinc-300 mb-4 border border-white/5">
                                ID: {doc.id}
                            </div>
                            <h1 className="text-3xl font-bold tracking-tight mb-2 truncate max-w-2xl" title={doc.filename}>
                                {doc.filename}
                            </h1>
                            <p className="text-zinc-500 text-lg">{doc.originalFilename}</p>
                        </div>

                        <div className="flex flex-col gap-3 min-w-[200px]">
                            {doc.isSigned ? (
                                <button
                                    onClick={handleDownload}
                                    disabled={downloading}
                                    className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-emerald-500 text-black rounded-xl font-medium hover:bg-emerald-400 transition-colors disabled:opacity-50"
                                >
                                    {downloading ? "Downloading..." : "⬇ Download Signed PDF"}
                                </button>
                            ) : (
                                <button
                                    onClick={handleSignClick}
                                    className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-500 transition-colors shadow-[0_0_15px_rgba(37,99,235,0.4)]"
                                >
                                    ✍️ Sign Document
                                </button>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-6 pt-8 border-t border-white/10">
                        <div>
                            <p className="text-zinc-500 text-sm mb-1">Status</p>
                            <p className={`font-medium ${doc.isSigned ? "text-emerald-400" : "text-amber-400"}`}>
                                {doc.isSigned ? "Signed" : doc.fileStatus || "Pending"}
                            </p>
                        </div>
                        <div>
                            <p className="text-zinc-500 text-sm mb-1">Date Uploaded</p>
                            <p className="font-medium text-zinc-200">
                                {doc.uploadDate ? new Date(doc.uploadDate).toLocaleDateString() : "Unknown"}
                            </p>
                        </div>
                        <div>
                            <p className="text-zinc-500 text-sm mb-1">File Size</p>
                            <p className="font-medium text-zinc-200">
                                {doc.fileSize ? (doc.fileSize / 1024).toFixed(1) + " KB" : "Unknown"}
                            </p>
                        </div>
                        <div>
                            <p className="text-zinc-500 text-sm mb-1">Uploaded By</p>
                            <p className="font-medium text-zinc-200">{doc.uploadedByUsername || "You"}</p>
                        </div>
                    </div>
                </div>
            </main>

            {/* Signature Modal */}
            {showModal && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-md p-4 animate-in fade-in duration-200">
                    <div className="bg-white text-zinc-900 rounded-2xl w-full max-w-5xl shadow-2xl relative overflow-hidden flex flex-col max-h-[90vh]">
                        <button
                            onClick={() => setShowModal(false)}
                            className="absolute top-6 right-6 text-zinc-400 hover:text-zinc-600 transition-colors z-[101]"
                        >
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6L6 18M6 6l12 12" /></svg>
                        </button>

                        <div className="p-8 pb-4 border-b border-zinc-100 flex-none">
                            <h2 className="text-2xl font-bold text-zinc-800 tracking-tight">Set your signature details</h2>
                        </div>

                        <div className="flex-1 overflow-y-auto p-8 bg-zinc-50/50">
                            <div className="flex flex-col h-full space-y-8 animate-in slide-in-from-right-8 duration-300">
                                <div className="flex flex-col md:flex-row gap-6">
                                    <div className="flex-none flex items-center justify-center">
                                        <div className="w-12 h-12 rounded-full border-2 border-[#e53935] text-[#e53935] flex items-center justify-center bg-red-50">
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
                                        </div>
                                    </div>
                                    <div className="flex-1 space-y-1">
                                        <label className="text-sm font-semibold text-zinc-700">Full name:</label>
                                        <input
                                            type="text"
                                            value={fullName}
                                            onChange={(e) => setFullName(e.target.value)}
                                            className="w-full border border-zinc-300 rounded-lg p-3 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-shadow bg-white"
                                            placeholder="Your name"
                                        />
                                    </div>
                                    <div className="flex-1 space-y-1">
                                        <label className="text-sm font-semibold text-zinc-700">Initials:</label>
                                        <input
                                            type="text"
                                            value={initials}
                                            onChange={(e) => setInitials(e.target.value)}
                                            className="w-full border border-zinc-300 rounded-lg p-3 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-shadow bg-white"
                                            placeholder="Your initials"
                                        />
                                    </div>
                                </div>

                                <div className="border border-zinc-200 rounded-xl bg-white overflow-hidden shadow-sm flex flex-col flex-1 relative min-h-[400px]">
                                    {/* Top Tabs */}
                                    <div className="flex border-b border-zinc-200 bg-zinc-50/50">
                                        {["Signature", "Initials", "Company Stamp"].map((tab) => (
                                            <button
                                                key={tab}
                                                onClick={() => setActiveTab(tab)}
                                                className={`flex-1 py-4 font-medium text-sm flex items-center justify-center gap-2 transition-colors ${activeTab === tab ? "text-zinc-900 border-b-2 border-[#e53935] bg-white" : "text-zinc-500 hover:text-zinc-700"}`}
                                            >
                                                {tab === "Signature" && <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20h9" /><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" /></svg>}
                                                {tab === "Initials" && <span className="underline decoration-2 underline-offset-4">AC</span>}
                                                {tab === "Company Stamp" && <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="8" width="18" height="12" rx="2" /><path d="M12 8v-4" /><path d="M8 4h8" /></svg>}
                                                {tab}
                                            </button>
                                        ))}
                                    </div>

                                    <div className="flex flex-1 absolute inset-0 top-[54px] bottom-0 left-0 right-0">
                                        {/* Sub Sidebar */}
                                        <div className="w-16 flex border-r border-zinc-100 flex-col bg-zinc-50/30 z-10">
                                            {["Text", "Draw", "Upload"].map((sideTab) => (
                                                <button
                                                    key={sideTab}
                                                    onClick={() => {
                                                        setActiveSideTab(sideTab);
                                                        if (sideTab !== 'Upload') setUploadedSigUrl(null);
                                                    }}
                                                    className={`p-4 flex justify-center border-l-2 transition-colors ${activeSideTab === sideTab ? "border-blue-500 text-blue-600 bg-blue-50/30" : "border-transparent text-zinc-400 hover:text-zinc-600 hover:bg-zinc-50"}`}
                                                >
                                                    {sideTab === "Text" && <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="4" y="4" width="16" height="16" rx="2" /><path d="M9 9h6M12 9v6" /></svg>}
                                                    {sideTab === "Draw" && <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 19l7-7 3 3-7 7-3-3z" /><path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z" /><path d="M2 2l7.586 7.586" /><circle cx="11" cy="11" r="2" /></svg>}
                                                    {sideTab === "Upload" && <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="17 8 12 3 7 8" /><line x1="12" y1="3" x2="12" y2="15" /></svg>}
                                                </button>
                                            ))}
                                        </div>

                                        {/* Main View Area */}
                                        <div className="flex-1 p-6 flex flex-col relative">
                                            {activeSideTab === "Text" && (
                                                <div className="flex flex-col h-full animate-in fade-in duration-200">
                                                    <div className="flex-1 border border-zinc-200 rounded-lg overflow-y-auto w-full">
                                                        {fontStyles.map((font, idx) => (
                                                            <div key={idx} onClick={() => setSelectedStyle(idx)} className={`p-4 border-b border-zinc-100 last:border-0 flex items-center gap-4 cursor-pointer hover:bg-zinc-50 transition-colors ${selectedStyle === idx ? 'bg-zinc-50' : ''}`}>
                                                                <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-none ${selectedStyle === idx ? 'border-emerald-500' : 'border-zinc-300'}`}>
                                                                    {selectedStyle === idx && <div className="w-2.5 h-2.5 rounded-full bg-emerald-500" />}
                                                                </div>
                                                                <div className="text-2xl pt-1 flex-1 overflow-hidden" style={{ fontFamily: font.name, color: selectedColor, textShadow: "0.5px 0.5px 1px rgba(0,0,0,0.05)" }}>
                                                                    {fullName || "Your name"}
                                                                </div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                    <div className="mt-5 flex items-center gap-3 pl-2 flex-none">
                                                        <span className="text-sm font-medium text-zinc-600">Color:</span>
                                                        <div className="flex gap-2">
                                                            {signatureColors.map(color => (
                                                                <button key={color} onClick={() => setSelectedColor(color)} className={`w-5 h-5 rounded-full transition-transform ${selectedColor === color ? 'ring-2 ring-offset-2 ring-zinc-400 scale-110' : 'hover:scale-110'}`} style={{ backgroundColor: color }} />
                                                            ))}
                                                        </div>
                                                    </div>
                                                </div>
                                            )}

                                            {activeSideTab === "Draw" && (
                                                <div className="flex flex-col h-full animate-in fade-in duration-200">
                                                    <div className="flex-1 w-full bg-zinc-50/50 rounded-lg border border-zinc-200 overflow-hidden relative" style={{ touchAction: 'none' }}>
                                                        <canvas ref={canvasRef} className="absolute inset-0 w-full h-full cursor-crosshair bg-transparent" onMouseDown={startDrawing} onMouseMove={draw} onMouseUp={stopDrawing} onMouseOut={stopDrawing} onTouchStart={startDrawing} onTouchMove={draw} onTouchEnd={stopDrawing} />
                                                        {!isDrawing && !canvasRef.current?.getContext('2d')?.getImageData(0, 0, 1, 1) && (
                                                            <div className="absolute inset-0 flex items-center justify-center pointer-events-none text-zinc-300 select-none">Sign here</div>
                                                        )}
                                                    </div>
                                                    <div className="mt-5 flex items-center justify-between px-2 flex-none">
                                                        <div className="flex items-center gap-3">
                                                            <span className="text-sm font-medium text-zinc-600">Color:</span>
                                                            <div className="flex gap-2">
                                                                {signatureColors.map(color => (
                                                                    <button key={color} onClick={() => setSelectedColor(color)} className={`w-5 h-5 rounded-full transition-transform ${selectedColor === color ? 'ring-2 ring-offset-2 ring-zinc-400 scale-110' : 'hover:scale-110'}`} style={{ backgroundColor: color }} />
                                                                ))}
                                                            </div>
                                                        </div>
                                                        <button onClick={clearCanvas} className="text-sm text-zinc-500 hover:text-red-500 transition-colors px-3 py-1 rounded-md hover:bg-red-50 font-medium">Clear</button>
                                                    </div>
                                                </div>
                                            )}

                                            {activeSideTab === "Upload" && (
                                                <div className="flex flex-col h-full justify-center items-center animate-in fade-in duration-200">
                                                    {!uploadedSigUrl ? (
                                                        <div onClick={() => sigInputRef.current?.click()} className="w-full flex-1 max-h-[300px] border-2 border-dashed border-blue-200 bg-blue-50/30 hover:bg-blue-50 rounded-2xl flex flex-col items-center justify-center cursor-pointer transition-colors group">
                                                            <div className="w-16 h-16 bg-white rounded-full shadow-sm flex items-center justify-center text-blue-500 mb-4 group-hover:scale-110 transition-transform">
                                                                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="17 8 12 3 7 8" /><line x1="12" y1="3" x2="12" y2="15" /></svg>
                                                            </div>
                                                            <span className="text-lg font-semibold text-zinc-700">Browse your files</span>
                                                            <span className="text-sm text-zinc-500 mt-1">Upload a photo of your signature (PNG, JPG)</span>
                                                            <input type="file" className="hidden" ref={sigInputRef} accept="image/png, image/jpeg" onChange={handleSigFileChange} />
                                                        </div>
                                                    ) : (
                                                        <div className="w-full flex-1 flex flex-col items-center justify-center space-y-4">
                                                            <div className="border border-zinc-200 rounded-xl p-4 bg-zinc-50 flex items-center justify-center max-w-full overflow-hidden" style={{ maxHeight: '200px' }}>
                                                                <img src={uploadedSigUrl} alt="Uploaded" className="max-h-full max-w-full object-contain mix-blend-multiply" />
                                                            </div>
                                                            <button onClick={() => setUploadedSigUrl(null)} className="text-zinc-500 hover:text-red-500 text-sm font-medium">Remove image</button>
                                                        </div>
                                                    )}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="p-6 border-t border-zinc-100 flex justify-end flex-none bg-white rounded-b-2xl z-10">
                            <button
                                onClick={executeSignAndEdit}
                                className="px-8 py-3 bg-[#e53935] text-white rounded-lg font-semibold hover:bg-[#d32f2f] transition-all shadow-md shadow-red-500/20 active:scale-95 text-lg"
                            >
                                Apply
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
