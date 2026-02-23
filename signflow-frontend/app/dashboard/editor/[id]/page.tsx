"use client";
import { useEffect, useState, useRef, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { getDocumentById, finalizeSignature, DocumentDetailDto } from "../../../lib/api";
import { Document, Page, pdfjs } from "react-pdf";
import Image from "next/image";
import Draggable from "react-draggable";
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// Set up worker
pdfjs.GlobalWorkerOptions.workerSrc = `//unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

type PlacedSignature = {
    id: string;
    x: number;
    y: number;
    page: number;
};

const DraggableSignatureItem = ({
    sig,
    signatureBase64,
    updatePosition,
    removeSignature
}: {
    sig: PlacedSignature;
    signatureBase64: string;
    updatePosition: (id: string, x: number, y: number) => void;
    removeSignature: (id: string) => void;
}) => {
    const nodeRef = useRef<HTMLDivElement>(null);
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted) return null;

    return (
        <Draggable
            nodeRef={nodeRef}
            bounds="parent"
            position={{ x: sig.x, y: sig.y }}
            onDrag={(e, data) => updatePosition(sig.id, data.x, data.y)}
        >
            <div ref={nodeRef} className="absolute top-0 left-0 z-10 cursor-move border-2 border-transparent hover:border-dashed hover:border-blue-400 group p-1 rounded transition-colors">
                <img src={signatureBase64} alt="Signature to place" className="max-h-[60px] max-w-[200px] object-contain mix-blend-multiply pointer-events-none" draggable={false} />

                {/* Remove button */}
                <button
                    onClick={(e) => { e.stopPropagation(); removeSignature(sig.id); }}
                    className="absolute -top-3 -right-3 w-6 h-6 bg-red-500 hover:bg-red-600 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-md z-20"
                >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </button>

                {/* Grab handle visual */}
                <div className="absolute top-1/2 -left-3 -translate-y-1/2 w-6 h-6 bg-white border border-zinc-200 text-zinc-500 rounded-full flex items-center justify-center shadow-sm opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 3h6v6M9 21H3v-6M21 3l-7 7M3 21l7-7" /></svg>
                </div>
            </div>
        </Draggable>
    );
};

export default function Editor() {
    const params = useParams();
    const router = useRouter();
    const id = params.id as string;

    const [doc, setDoc] = useState<DocumentDetailDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [signatureBase64, setSignatureBase64] = useState<string | null>(null);
    const [signing, setSigning] = useState(false);
    const [requestData, setRequestData] = useState<any>(null);

    // PDF state
    const [numPages, setNumPages] = useState<number>(1);
    const [pageNumber, setPageNumber] = useState<number>(1);
    const [pdfScale, setPdfScale] = useState<number>(1.2);

    // Dragging state
    const [placedSignatures, setPlacedSignatures] = useState<PlacedSignature[]>([]);

    // Refs for calculating percentages
    const pdfWrapperRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            router.push("/login");
            return;
        }

        const savedSig = sessionStorage.getItem("current_signature");
        if (savedSig) {
            setSignatureBase64(savedSig);
        }

        const savedReq = sessionStorage.getItem("signflow_request");
        if (savedReq) {
            setRequestData(JSON.parse(savedReq));
        }

        const fetchDoc = async () => {
            try {
                const data = await getDocumentById(id, token);
                setDoc(data);
            } catch (err) {
                console.error(err);
                alert("Failed to load document");
                router.push("/dashboard");
            } finally {
                setLoading(false);
            }
        };

        fetchDoc();
    }, [id, router]);

    const onDocumentLoadSuccess = ({ numPages }: { numPages: number }) => {
        setNumPages(numPages);
    };

    // HTML5 Drag and Drop Handlers
    const handleDragStart = (e: React.DragEvent) => {
        if (typeof window === 'undefined') return;
        if (!signatureBase64) return;
        try {
            e.dataTransfer.setData("application/signflow-signature", "true");
            e.dataTransfer.effectAllowed = "copy";
        } catch (err) { }
    };

    const handleDragOver = (e: React.DragEvent) => {
        if (typeof window === 'undefined') return;
        e.preventDefault();
        try {
            e.dataTransfer.dropEffect = "copy";
        } catch (err) { }
    };

    const handleDrop = (e: React.DragEvent) => {
        if (typeof window === 'undefined') return;
        e.preventDefault();
        try {
            const isSignature = e.dataTransfer.getData("application/signflow-signature");
            if (!isSignature || !pdfWrapperRef.current) return;

            const rect = pdfWrapperRef.current.getBoundingClientRect();

            // Ensure drop is within bounds roughly, placing the center of the signature at the mouse cursor
            const x = e.clientX - rect.left - 75; // 75 is half of default 150 signature width
            const y = e.clientY - rect.top - 25; // 25 is half of default 50 signature height

            const newSig: PlacedSignature = {
                id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
                x: Math.max(0, Math.min(x, rect.width - 150)),
                y: Math.max(0, Math.min(y, rect.height - 50)),
                page: pageNumber
            };

            setPlacedSignatures(prev => [...prev, newSig]);
        } catch (err) {
            console.error(err);
        }
    };

    const updateSignaturePosition = (sigId: string, x: number, y: number) => {
        setPlacedSignatures(prev => prev.map(sig => sig.id === sigId ? { ...sig, x, y } : sig));
    };

    const removeSignature = (sigId: string) => {
        setPlacedSignatures(prev => prev.filter(sig => sig.id !== sigId));
    };

    const handleSign = async () => {
        const token = localStorage.getItem("token");
        if (!token || !doc) return;

        if (requestData) {
            setSigning(true);
            try {
                // We'll add this API call in the next step
                const { sendSignatureRequest } = await import("../../../lib/api");
                await sendSignatureRequest(id, requestData, token);

                sessionStorage.removeItem("signflow_request");
                alert("Signature request sent successfully to " + requestData.receivers.length + " receiver(s)!");
                router.push("/dashboard");
            } catch (err) {
                console.error(err);
                alert("Failed to send signature request.");
            } finally {
                setSigning(false);
            }
            return;
        }

        if (!signatureBase64) {
            alert("No signature generated! Please go back and make sure you created a signature.");
            return;
        }

        if (!pdfWrapperRef.current || placedSignatures.length === 0) {
            alert("Please place at least one signature on the document.");
            return;
        }

        const pdfRect = pdfWrapperRef.current.getBoundingClientRect();

        const positions = placedSignatures.map(sig => ({
            pageNumber: sig.page,
            xPercent: (sig.x / pdfRect.width) * 100,
            yPercent: (sig.y / pdfRect.height) * 100,
            width: 150,
            height: 50
        }));

        setSigning(true);
        try {
            await finalizeSignature(
                id,
                signatureBase64,
                positions,
                token
            );

            // Removing signatureBase64 from session storage to reset flow
            sessionStorage.removeItem("current_signature");

            alert("Document signed successfully!");
            router.push("/dashboard");
        } catch (err) {
            console.error(err);
            alert("Signature process failed. The backend might require a valid signature.png file or missing keystore.");
        } finally {
            setSigning(false);
        }
    };

    const pdfFileConfig = useMemo(() => {
        return {
            url: `http://localhost:8084/api/documents/${id}/download`,
            httpHeaders: typeof window !== "undefined" ? { Authorization: `Bearer ${localStorage.getItem("token")}` } : {}
        };
    }, [id]);

    if (loading) {
        return (
            <div className="min-h-screen bg-zinc-100 flex items-center justify-center">
                <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-950 via-[#1a0b2e] to-black flex flex-col font-sans h-screen overflow-hidden text-white">
            {/* Navbar Minimal */}
            <nav className="flex items-center justify-between px-6 py-3 border-b border-purple-500/20 bg-purple-900/10 backdrop-blur-xl flex-none z-50">
                <div className="flex items-center gap-4">
                    <button onClick={() => router.push("/dashboard")} className="text-zinc-500 hover:text-zinc-800 transition-colors p-2 rounded-full hover:bg-zinc-100">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 12H5M12 19l-7-7 7-7" /></svg>
                    </button>
                    <div className="flex flex-col items-center justify-center cursor-pointer hover:scale-105 transition-transform duration-300">
                        <div className="relative w-24 h-24 flex items-center justify-center">
                            <Image src="/sf-logo.png" alt="Signflow Editor Logo" fill className="object-contain" priority />
                        </div>
                        <span className="text-[10px] sm:text-xs font-bold tracking-[0.2em] uppercase text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-400 drop-shadow-sm -mt-3">Signflow Editor</span>
                    </div>
                </div>
                <div className="text-sm font-medium text-zinc-500 truncate max-w-md">
                    {doc?.filename}
                </div>
            </nav>

            <div className="flex flex-1 overflow-hidden">
                {/* Left Sidebar - Signing Options */}
                <div className="w-80 bg-white border-r border-zinc-200 flex flex-col shadow-sm z-10 flex-none">
                    <div className="p-6 border-b border-zinc-100">
                        <h2 className="text-xl font-bold text-zinc-800 tracking-tight">Signing options</h2>
                    </div>

                    <div className="p-6 flex-1 overflow-y-auto">
                        <div className="mb-8">
                            {!requestData ? (
                                <>
                                    <h3 className="text-sm font-semibold text-zinc-500 uppercase tracking-wider mb-4">Required fields</h3>

                                    {/* Draggable Signature Item in Sidebar */}
                                    <div
                                        draggable={!!signatureBase64}
                                        onDragStart={handleDragStart}
                                        className={`border-2 border-dashed rounded-lg p-4 flex flex-col items-center justify-center relative transition-colors min-h-[100px] ${signatureBase64 ? 'cursor-grab active:cursor-grabbing border-blue-300 bg-blue-50 hover:bg-blue-100' : 'cursor-not-allowed opacity-50 border-zinc-200 bg-zinc-50'}`}
                                    >
                                        <div className="absolute top-2 left-2 flex flex-col gap-1 items-center text-blue-500">
                                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="9" cy="5" r="1" /><circle cx="9" cy="12" r="1" /><circle cx="9" cy="19" r="1" /><circle cx="15" cy="5" r="1" /><circle cx="15" cy="12" r="1" /><circle cx="15" cy="19" r="1" /></svg>
                                            <div className="w-6 h-6 bg-blue-500 rounded text-white flex items-center justify-center shadow-sm">
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 20h9" /><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" /></svg>
                                            </div>
                                        </div>

                                        <div className="text-xs font-semibold text-blue-500 absolute top-2 left-10">Signature</div>

                                        {signatureBase64 ? (
                                            <>
                                                <img src={signatureBase64} alt="Signature" className="max-h-16 mt-4 object-contain mix-blend-multiply opacity-75 pointer-events-none" draggable={false} />
                                                <div className="text-xs text-blue-500 mt-2 font-medium">Drag me to the document</div>
                                            </>
                                        ) : (
                                            <span className="text-zinc-400 font-medium">No signature</span>
                                        )}
                                    </div>
                                </>
                            ) : (
                                <div className="text-sm text-zinc-600 space-y-4">
                                    <p>You have configured <strong>{requestData.receivers.length}</strong> receiver(s) to sign this document.</p>
                                    <p>Review the document preview. When ready, click 'Send to Sign' below to dispatch the secure email notifications.</p>
                                    <div className="mt-4 border border-blue-100 bg-blue-50 p-4 rounded-lg shadow-sm">
                                        <h4 className="font-semibold text-blue-800 text-xs uppercase tracking-widest mb-2">Receivers</h4>
                                        <ul className="space-y-2">
                                            {requestData.receivers.map((r: any, idx: number) => (
                                                <li key={idx} className="flex items-center gap-2 text-sm text-blue-900 border-l-2 border-blue-400 pl-2 opacity-90">
                                                    <span className="font-medium shrink-0">{r.role}:</span>
                                                    <span className="truncate" title={`${r.name} <${r.email}>`}>{r.name || r.email}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                </div>
                            )}
                        </div>


                    </div>

                    <div className="p-6 border-t border-zinc-100 flex-none bg-white">
                        <button
                            onClick={handleSign}
                            disabled={signing || (!requestData && placedSignatures.length === 0)}
                            className="w-full py-4 bg-[#f99d95] hover:bg-[#ff867a] disabled:opacity-50 disabled:cursor-not-allowed text-white text-lg font-bold rounded-xl shadow-md transition-colors flex items-center justify-center gap-2"
                        >
                            {requestData ? (signing ? "Sending..." : "Send to Sign") : (signing ? "Signing..." : "Sign Document")}
                            {!signing && <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10" /><polyline points="12 16 16 12 12 8" /><line x1="8" y1="12" x2="16" y2="12" /></svg>}
                        </button>
                    </div>
                </div>

                {/* Right Content - PDF Viewer */}
                <div className="flex-1 bg-[#dcdde1] overflow-y-auto relative flex justify-center py-10">
                    {/* Controls overlay */}
                    <div className="fixed top-20 right-8 bg-white rounded-lg shadow-md flex items-center gap-2 p-1 z-20">
                        <button
                            onClick={() => setPdfScale(s => Math.max(0.5, s - 0.1))}
                            className="w-8 h-8 flex items-center justify-center rounded hover:bg-zinc-100 text-zinc-600"
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="5" y1="12" x2="19" y2="12" /></svg>
                        </button>
                        <span className="text-sm font-medium text-zinc-600 min-w-[3ch] text-center">{Math.round(pdfScale * 100)}%</span>
                        <button
                            onClick={() => setPdfScale(s => Math.min(2.5, s + 0.1))}
                            className="w-8 h-8 flex items-center justify-center rounded hover:bg-zinc-100 text-zinc-600"
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
                        </button>
                    </div>

                    <div className="flex flex-col items-center">
                        <div
                            ref={pdfWrapperRef}
                            className="relative bg-white shadow-xl max-w-max border-2 border-transparent"
                            onDragOver={handleDragOver}
                            onDrop={handleDrop}
                        >
                            <Document
                                file={pdfFileConfig}
                                onLoadSuccess={onDocumentLoadSuccess}
                                loading={<div className="p-20 text-zinc-500">Loading document...</div>}
                                error={<div className="p-20 text-red-500">Failed to load PDF. Check backend CORS or permissions.</div>}
                            >
                                <Page
                                    pageNumber={pageNumber}
                                    scale={pdfScale}
                                    renderTextLayer={true}
                                    renderAnnotationLayer={false}
                                />
                            </Document>

                            {/* Draggable Signatures Overlay on PDF */}
                            {signatureBase64 && placedSignatures.filter(sig => sig.page === pageNumber).map(sig => (
                                <DraggableSignatureItem
                                    key={sig.id}
                                    sig={sig}
                                    signatureBase64={signatureBase64}
                                    updatePosition={updateSignaturePosition}
                                    removeSignature={removeSignature}
                                />
                            ))}
                        </div>

                        {numPages > 1 && (
                            <div className="mt-6 flex items-center gap-4 bg-white px-4 py-2 rounded-full shadow-sm">
                                <button
                                    disabled={pageNumber <= 1}
                                    onClick={() => setPageNumber(p => p - 1)}
                                    className="w-8 h-8 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-zinc-200 disabled:opacity-50 text-zinc-700"
                                >
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 18l-6-6 6-6" /></svg>
                                </button>
                                <span className="text-sm font-medium text-zinc-600">Page {pageNumber} of {numPages}</span>
                                <button
                                    disabled={pageNumber >= numPages}
                                    onClick={() => setPageNumber(p => p + 1)}
                                    className="w-8 h-8 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-zinc-200 disabled:opacity-50 text-zinc-700"
                                >
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 18l6-6-6-6" /></svg>
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
