"use client";
import { useEffect, useState, useRef } from "react";
import { getDocuments, uploadDocument, DocumentDto } from "../lib/api";
import { useRouter } from "next/navigation";
import Image from "next/image";
import Link from "next/link";

export default function Dashboard() {
  const router = useRouter();
  const [docs, setDocs] = useState<DocumentDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const sigInputRef = useRef<HTMLInputElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // Modal state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [modalStep, setModalStep] = useState<"WHO_SIGNS" | "SIGN_DETAILS" | "SEVERAL_PEOPLE">("WHO_SIGNS");

  // Several People State
  const [receivers, setReceivers] = useState([{ id: 1, name: "", email: "", role: "Signer", color: "#ffcdd2", requirePassword: false, password: "" }]);
  const [setOrder, setSetOrder] = useState(false);
  const [changeExpiration, setChangeExpiration] = useState(false);

  // New Settings State
  const [multipleRequests, setMultipleRequests] = useState(false);
  const [emailNotifications, setEmailNotifications] = useState(false);
  const [enableReminders, setEnableReminders] = useState(false);
  const [reminderDays, setReminderDays] = useState(1);
  const [digitalSignature, setDigitalSignature] = useState(false);
  const [setLanguageState, setSetLanguageState] = useState(false);
  const [language, setLanguage] = useState("English");
  const [customizeEmail, setCustomizeEmail] = useState(false);
  const [showUuid, setShowUuid] = useState(true);
  const [signatureVerificationCode, setSignatureVerificationCode] = useState(false);
  const [emailBranding, setEmailBranding] = useState(false);

  const addReceiver = () => {
    const colors = ["#ffcdd2", "#bbdefb", "#c8e6c9", "#fff9c4", "#e1bee7"];
    const newColor = colors[receivers.length % colors.length];
    setReceivers([...receivers, { id: Date.now(), name: "", email: "", role: "Signer", color: newColor, requirePassword: false, password: "" }]);
  };

  const removeReceiver = (id: number) => {
    setReceivers(receivers.filter(r => r.id !== id));
  };

  const updateReceiver = (id: number, field: string, value: any) => {
    setReceivers(receivers.map(r => r.id === id ? { ...r, [field]: value } : r));
  };

  // Signature Details State
  const [fullName, setFullName] = useState("Sudipta Pratiher");
  const [initials, setInitials] = useState("SP");
  const [activeTab, setActiveTab] = useState("Signature");
  const [activeSideTab, setActiveSideTab] = useState("Text");
  const [selectedStyle, setSelectedStyle] = useState(0);
  const [selectedColor, setSelectedColor] = useState("#333333");
  const [uploadedSigUrl, setUploadedSigUrl] = useState<string | null>(null);

  // Drawing state
  const [isDrawing, setIsDrawing] = useState(false);

  const fontStyles = [
    { name: "'Caveat', cursive", url: "https://fonts.googleapis.com/css2?family=Caveat:wght@600&display=swap" },
    { name: "'Dancing Script', cursive", url: "https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&display=swap" },
    { name: "'Pacifico', cursive", url: "https://fonts.googleapis.com/css2?family=Pacifico&display=swap" },
    { name: "'Great Vibes', cursive", url: "https://fonts.googleapis.com/css2?family=Great+Vibes&display=swap" }
  ];

  const signatureColors = ["#333333", "#e53935", "#1e88e5", "#43a047"];

  const fetchDocs = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }
    try {
      const data = await getDocuments(token);
      setDocs(data || []);
      setLoading(false);
    } catch {
      alert("Session expired. Login again");
      localStorage.removeItem("token");
      router.push("/login");
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  // Initialize canvas context
  useEffect(() => {
    if (activeSideTab === "Draw" && canvasRef.current) {
      const canvas = canvasRef.current;
      const ctx = canvas.getContext("2d");
      if (ctx) {
        // Handle high DPI displays
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
  }, [activeSideTab, selectedColor]);

  const logout = () => {
    localStorage.removeItem("token");
    router.push("/login");
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setSelectedFile(file);
    setModalStep("WHO_SIGNS");
    setShowModal(true);
  };

  const handleSigUploadClick = () => {
    sigInputRef.current?.click();
  };

  const handleSigFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setUploadedSigUrl(url);
    }
  };

  const executeUpload = async () => {
    if (!selectedFile) return;

    const title = prompt(`Document Title:`, selectedFile.name);
    if (!title) {
      closeModal();
      return;
    }

    setUploading(true);
    let signatureBase64 = "";

    try {
      // Capture Signature as Base64
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
          // Remove wrapping quotes from font name if any, but simpler to use generic string
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
    const token = localStorage.getItem("token");
    try {
      if (modalStep === "SEVERAL_PEOPLE") {
        const requestData = {
          receivers,
          settings: { setOrder, changeExpiration, multipleRequests, emailNotifications, enableReminders, reminderDays, digitalSignature, setLanguageState, language, customizeEmail, showUuid, signatureVerificationCode, emailBranding }
        };
        sessionStorage.setItem("Verifile_request", JSON.stringify(requestData));
      } else {
        sessionStorage.removeItem("Verifile_request");
      }

      const response = await uploadDocument(selectedFile, title, token!);

      // Navigate to Editor
      if (response && response.id) {
        router.push(`/dashboard/editor/${response.id}`);
      } else {
        // Fallback if no ID is returned (e.g. backend response doesn't match expected)
        await fetchDocs();
      }
    } catch (err) {
      alert("Upload failed. Make sure it's a valid PDF.");
    } finally {
      setUploading(false);
      setSelectedFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedFile(null);
    setModalStep("WHO_SIGNS");
    setReceivers([{ id: 1, name: "", email: "", role: "Signer", color: "#ffcdd2", requirePassword: false, password: "" }]);
    if (fileInputRef.current) fileInputRef.current.value = "";
    if (sigInputRef.current) sigInputRef.current.value = "";
    setUploadedSigUrl(null);
  };

  const getStatusBadge = (status: string, isSigned: boolean) => {
    if (isSigned) return <span className="px-3 py-1 rounded-full bg-emerald-500/20 text-emerald-400 text-xs font-medium border border-emerald-500/30">Signed</span>;
    if (status === "PENDING") return <span className="px-3 py-1 rounded-full bg-amber-500/20 text-amber-400 text-xs font-medium border border-amber-500/30">Pending</span>;
    return <span className="px-3 py-1 rounded-full bg-zinc-500/20 text-zinc-400 text-xs font-medium border border-zinc-500/30">{status || "Unknown"}</span>;
  };

  // Drawing handlers
  const startDrawing = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // Use selected color
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

    // Prevent scrolling when drawing on touch devices
    if ('touches' in e && e.cancelable) {
      e.preventDefault();
    }

    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;

    ctx.lineTo(clientX - rect.left, clientY - rect.top);
    ctx.stroke();
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearCanvas = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-950 via-[#1a0b2e] to-black text-white selection:bg-purple-500/30 font-sans">
      {/* Inject Fonts directly for styling if needed */}
      <style dangerouslySetInnerHTML={{
        __html: `
        @import url('https://fonts.googleapis.com/css2?family=Caveat:wght@600&family=Dancing+Script:wght@600&family=Great+Vibes&family=Pacifico&display=swap');
      `}} />

      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-4 border-b border-purple-500/20 backdrop-blur-xl sticky top-0 z-50 bg-purple-900/10">
        <div className="flex flex-col items-center justify-center cursor-pointer hover:scale-105 transition-transform duration-300">
          <div className="relative w-24 h-24 flex items-center justify-center">
            <Image src="/sf-logo.png" alt="Verifile Logo" fill className="object-contain" priority />
          </div>
          <span className="text-xs font-bold tracking-[0.2em] uppercase text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-400 drop-shadow-sm -mt-3">Verifile</span>
        </div>
        <button
          onClick={logout}
          className="px-4 py-2 text-sm font-medium text-zinc-400 hover:text-white transition-colors"
        >
          Logout
        </button>
      </nav>

      <main className="max-w-6xl mx-auto p-8 pt-12 relative">
        <div className="flex justify-between items-end mb-12">
          <div>
            <h1 className="text-4xl font-bold tracking-tight mb-2 bg-gradient-to-b from-white to-zinc-400 bg-clip-text text-transparent">Your Documents</h1>
            <p className="text-zinc-500">Manage, track, and execute your document workflows.</p>
          </div>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="application/pdf"
            className="hidden"
          />
          <button
            onClick={handleUploadClick}
            disabled={uploading}
            className="flex items-center gap-2 px-6 py-3 bg-white text-black rounded-full font-medium hover:bg-zinc-200 transition-all hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 shadow-[0_0_20px_rgba(255,255,255,0.1)]"
          >
            {uploading ? "Uploading..." : "Upload Document"}
          </button>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center p-24 border border-white/5 border-dashed rounded-3xl bg-zinc-900/20">
            <div className="w-8 h-8 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin mb-4"></div>
            <p className="text-zinc-400">Loading documents...</p>
          </div>
        ) : docs.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-24 border border-white/5 border-dashed rounded-3xl bg-zinc-900/20">
            <div className="w-16 h-16 mb-4 opacity-50 bg-zinc-800 rounded-2xl flex items-center justify-center text-3xl">📄</div>
            <h3 className="text-xl font-semibold mb-2">No documents yet</h3>
            <p className="text-zinc-500 mb-6 text-center max-w-sm">Upload your first PDF to get started with secure digital signatures.</p>
            <button
              onClick={handleUploadClick}
              className="text-blue-400 hover:text-blue-300 font-medium transition-colors"
            >
              Upload a file
            </button>
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {docs.map((doc, i) => (
              <Link href={`/dashboard/${doc.id}`} key={doc.id || i} className="group block p-6 rounded-2xl bg-zinc-900/50 border border-white/5 hover:border-blue-500/30 hover:bg-zinc-900 transition-all hover:shadow-[0_0_30px_rgba(59,130,246,0.1)] cursor-pointer">
                <div className="flex justify-between items-start mb-4">
                  <div className="p-3 bg-zinc-800/80 rounded-xl group-hover:bg-blue-500/10 group-hover:text-blue-400 transition-colors">
                    📄
                  </div>
                  {getStatusBadge(doc.fileStatus, doc.isSigned)}
                </div>

                <h3 className="text-lg font-semibold truncate mb-1" title={doc.filename}>{doc.filename || "Untitled Document"}</h3>
                <p className="text-sm text-zinc-500 truncate mb-4" title={doc.originalFilename}>{doc.originalFilename || "No file"}</p>

                <div className="flex items-center justify-between text-xs text-zinc-600 border-t border-white/5 pt-4 mt-auto">
                  <span>{doc.uploadDate ? new Date(doc.uploadDate).toLocaleDateString() : "Just now"}</span>
                  <span>{doc.fileSize ? (doc.fileSize / 1024).toFixed(1) + " KB" : ""}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>

      {/* Upload Modal Overlay */}
      {showModal && selectedFile && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-md p-4 animate-in fade-in duration-200">
          <div className="bg-white text-zinc-900 rounded-2xl w-full max-w-5xl shadow-2xl relative overflow-hidden flex flex-col max-h-[90vh]">
            <button
              onClick={closeModal}
              className="absolute top-6 right-6 text-zinc-400 hover:text-zinc-600 transition-colors z-[101]"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6L6 18M6 6l12 12" /></svg>
            </button>

            <div className="p-8 pb-4 border-b border-zinc-100 flex-none">
              <h2 className="text-2xl font-bold text-zinc-800 tracking-tight">
                {modalStep === "WHO_SIGNS" ? "Who will sign this document?" :
                  modalStep === "SEVERAL_PEOPLE" ? "Create your signature request" :
                    "Set your signature details"}
              </h2>
            </div>

            <div className="flex-1 overflow-y-auto p-8 bg-zinc-50/50">
              {modalStep === "WHO_SIGNS" ? (
                // Step 1: Who Signs
                <>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-6">
                    {/* Only Me Card */}
                    <div
                      onClick={() => setModalStep("SIGN_DETAILS")}
                      className="bg-white rounded-xl p-8 flex flex-col items-center text-center cursor-pointer group hover:bg-blue-50/30 transition-all border border-zinc-200 hover:border-blue-200 shadow-sm"
                    >
                      <div className="w-56 h-48 mb-8 rounded-2xl flex items-center justify-center bg-blue-50 text-blue-500 group-hover:scale-105 transition-transform duration-300">
                        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                          <polyline points="14 2 14 8 20 8"></polyline>
                          <path d="M16 13H8"></path>
                          <path d="M16 17H8"></path>
                          <path d="M10 9H8"></path>
                          <path d="M18.5 15.5l-4.5 4.5"></path>
                          <path d="M14 20h4.5"></path>
                        </svg>
                      </div>
                      <button className="w-full max-w-[200px] py-3 bg-[#e53935] text-white rounded-lg font-semibold text-lg mb-3 hover:bg-[#d32f2f] transition-colors shadow-md shadow-red-500/10 active:scale-95 pointer-events-none">
                        Only me
                      </button>
                      <p className="text-zinc-500 text-sm">Sign this document</p>
                    </div>

                    {/* Several People Card */}
                    <div
                      onClick={() => setModalStep("SEVERAL_PEOPLE")}
                      className="bg-white rounded-xl p-8 flex flex-col items-center text-center cursor-pointer group hover:bg-blue-50/30 transition-all border border-zinc-200 hover:border-blue-200 shadow-sm"
                    >
                      <div className="w-56 h-48 mb-8 rounded-2xl flex items-center justify-center bg-blue-50 text-blue-500 group-hover:scale-105 transition-transform duration-300">
                        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                          <circle cx="9" cy="7" r="4"></circle>
                          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                      </div>
                      <button className="w-full max-w-[200px] py-3 bg-[#e53935] text-white rounded-lg font-semibold text-lg mb-3 hover:bg-[#d32f2f] transition-colors shadow-md shadow-red-500/10 active:scale-95 pointer-events-none">
                        Several people
                      </button>
                      <p className="text-zinc-500 text-sm">Invite others to sign</p>
                    </div>
                  </div>
                  <div className="text-center text-zinc-500 text-sm">
                    Uploaded documents: <span className="text-zinc-900 font-semibold">{selectedFile.name}</span>
                  </div>
                </>
              ) : modalStep === "SEVERAL_PEOPLE" ? (
                // Step 2b: Several People Options
                <div className="flex flex-col h-full space-y-8 animate-in slide-in-from-right-8 duration-300">
                  <p className="text-zinc-600 font-medium mb-2">Who will receive your document?</p>

                  <div className="border border-zinc-300 rounded-lg bg-zinc-50/50 overflow-hidden shadow-sm">
                    {receivers.map((receiver) => (
                      <div key={receiver.id} className="flex flex-col border-b border-zinc-200 last:border-b-0 bg-white">
                        <div className="flex items-center gap-3 p-4">
                          <div className="text-zinc-400 cursor-grab px-1 text-lg">↕</div>
                          <div className="w-8 h-8 rounded-full border border-black/10 shadow-sm shrink-0" style={{ backgroundColor: receiver.color }}></div>

                          <input type="text" placeholder="Name" className="w-[180px] border border-zinc-300 rounded p-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" value={receiver.name} onChange={(e) => updateReceiver(receiver.id, 'name', e.target.value)} />

                          <input type="email" placeholder="Email" className="w-[200px] border border-zinc-300 rounded p-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" value={receiver.email} onChange={(e) => updateReceiver(receiver.id, 'email', e.target.value)} />

                          <select className="border border-zinc-300 rounded p-2 text-sm bg-white cursor-pointer hover:border-zinc-400 outline-none w-[110px]" value={receiver.role} onChange={(e) => updateReceiver(receiver.id, 'role', e.target.value)}>
                            <option value="Signer">Signer</option>
                            <option value="Validator">Validator</option>
                            <option value="Witness">Witness</option>
                          </select>

                          <div className="flex items-center gap-3 ml-2 text-zinc-400 shrink-0">
                            <button onClick={() => updateReceiver(receiver.id, 'requirePassword', !receiver.requirePassword)} className={`w-7 h-7 rounded-full flex items-center justify-center transition-colors shadow-sm ${receiver.requirePassword ? "bg-blue-500 text-white" : "bg-zinc-200 text-zinc-500 hover:bg-zinc-300"}`}>
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" /></svg>
                            </button>
                            {receivers.length > 1 && (
                              <button onClick={() => removeReceiver(receiver.id)} className="hover:text-red-500 transition-colors"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M18 6L6 18M6 6l12 12" /></svg></button>
                            )}
                          </div>
                        </div>
                        {receiver.requirePassword && (
                          <div className="px-14 pb-4 pt-1 flex items-center gap-3">
                            <span className="text-sm text-zinc-500 font-medium">Access Password:</span>
                            <input type="text" placeholder="Enter password for this receiver" className="w-[300px] border border-zinc-300 rounded p-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" value={receiver.password || ''} onChange={(e) => updateReceiver(receiver.id, 'password', e.target.value)} />
                          </div>
                        )}
                      </div>
                    ))}

                    <button onClick={addReceiver} className="w-full py-4 text-sm font-semibold text-blue-500 hover:bg-blue-50/50 flex items-center justify-center gap-2 transition-colors uppercase tracking-wide">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="8.5" cy="7" r="4" /><line x1="20" y1="8" x2="20" y2="14" /><line x1="23" y1="11" x2="17" y2="11" /></svg>
                      ADD RECEIVER
                    </button>
                  </div>

                  <div className="mt-8">
                    <h3 className="text-xl font-bold text-zinc-800 mb-6 font-sans tracking-tight">Settings</h3>

                    <div className="space-y-6">
                      {/* Set the order of receivers */}
                      <div className="flex items-start gap-4">
                        <input type="checkbox" checked={setOrder} onChange={(e) => setSetOrder(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M7 11V7a5 5 0 0 1 10 0v4" /><rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M12 15v2" /></svg>
                            <span className={setOrder ? "text-zinc-800" : ""}>Set the order of receivers</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">Select this option to set a signing order. A signer won't receive a request until the previous person has completed their document.</p>
                        </div>
                      </div>

                      {/* Change expiration date */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={changeExpiration} onChange={(e) => setChangeExpiration(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" /></svg>
                            <span className={changeExpiration ? "text-zinc-800" : ""}>Change expiration date</span>
                          </div>
                          {changeExpiration ? (
                            <p className="text-[15px] text-zinc-700 mt-2 leading-relaxed">The document will expire in <span className="font-bold">15 days</span>.<br /><span className="text-sm text-zinc-500">Expires on: {new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toLocaleDateString()}</span></p>
                          ) : null}
                        </div>
                      </div>

                      {/* Multiple requests */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={multipleRequests} onChange={(e) => setMultipleRequests(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><path d="M16 13H8" /><path d="M16 17H8" /><path d="M10 9H8" /></svg>
                            <span className={multipleRequests ? "text-zinc-800" : ""}>Multiple requests</span>
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700 uppercase tracking-widest">Premium</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">This option will allow each signer to receive a unique and separate request to sign individually.</p>
                        </div>
                      </div>

                      {/* Enable email notifications */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={emailNotifications} onChange={(e) => setEmailNotifications(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" /><polyline points="22,6 12,13 2,6" /></svg>
                            <span className={emailNotifications ? "text-zinc-800" : ""}>Enable email notifications</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">You will receive an email notification when a receiver has completed their request.</p>
                        </div>
                      </div>

                      {/* Enable reminders */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={enableReminders} onChange={(e) => setEnableReminders(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></svg>
                            <span className={enableReminders ? "text-zinc-800" : ""}>Enable reminders</span>
                          </div>
                          <div className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl flex items-center gap-2">
                            Send a reminder to the participants every
                            {enableReminders ? (
                              <input type="number" min="1" className="w-16 border border-zinc-300 rounded p-1 text-center text-zinc-800 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" value={reminderDays} onChange={(e) => setReminderDays(parseInt(e.target.value) || 1)} />
                            ) : (
                              <span className="font-semibold">{reminderDays}</span>
                            )}
                            days.
                          </div>
                        </div>
                      </div>

                      {/* Digital Signature */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={digitalSignature} onChange={(e) => setDigitalSignature(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></svg>
                            <span className={digitalSignature ? "text-zinc-800" : ""}>Digital Signature</span>
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700 uppercase tracking-widest">Premium</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">A signed Certified Hash and a Qualified Timestamp is embedded to the signed documents, ensuring the future integrity of the document and signature. Certified signatures are eIDAS, ESIGN & UETA compliant.</p>
                        </div>
                      </div>

                      {/* Set language */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={setLanguageState} onChange={(e) => setSetLanguageState(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div className="flex-1">
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10" /><line x1="2" y1="12" x2="22" y2="12" /><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" /></svg>
                            <span className={setLanguageState ? "text-zinc-800" : ""}>Set language</span>
                          </div>
                          <div className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">
                            Notifications will be sent in
                            {setLanguageState ? (
                              <select className="ml-2 border border-zinc-300 rounded p-1 text-sm bg-white cursor-pointer hover:border-zinc-400 outline-none w-[120px] text-zinc-800" value={language} onChange={(e) => setLanguage(e.target.value)}>
                                <option value="English">English</option>
                                <option value="Spanish">Spanish</option>
                                <option value="French">French</option>
                                <option value="German">German</option>
                              </select>
                            ) : (
                              <span className="font-semibold ml-1">{language}.</span>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Customize the request email */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={customizeEmail} onChange={(e) => setCustomizeEmail(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                            <span className={customizeEmail ? "text-zinc-800" : ""}>Customize the request email</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">Edit the text you want to appear in the subject and body of the signature request email.</p>
                        </div>
                      </div>

                      {/* UUID */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={showUuid} onChange={(e) => setShowUuid(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" /></svg>
                            <span className={showUuid ? "text-zinc-800" : ""}>UUID (recommended)</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">Show the Unique Signer Identifier code that appears below the signatures to help validate the signature on the Audit Trail. It is recommended that you keep this activated, otherwise it lowers the legal weight of the end document.</p>
                        </div>
                      </div>

                      {/* Signature verification code */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={signatureVerificationCode} onChange={(e) => setSignatureVerificationCode(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" /><polyline points="22,6 12,13 2,6" /></svg>
                            <span className={signatureVerificationCode ? "text-zinc-800" : ""}>Signature verification code</span>
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-600 uppercase tracking-widest">Highly recommended</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">Digitally verify the integrity of the printed document using a QR code and a unique password that are provided in the Audit Trail.</p>
                        </div>
                      </div>

                      {/* Email branding */}
                      <div className="flex items-start gap-4 pt-6 border-t border-zinc-100">
                        <input type="checkbox" checked={emailBranding} onChange={(e) => setEmailBranding(e.target.checked)} className="mt-1 w-5 h-5 rounded border-zinc-300 text-blue-600 focus:ring-blue-500 cursor-pointer" />
                        <div>
                          <div className="flex items-center gap-3 font-semibold text-zinc-400">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2" /><circle cx="8.5" cy="8.5" r="1.5" /><polyline points="21 15 16 10 5 21" /></svg>
                            <span className={emailBranding ? "text-zinc-800" : ""}>Email branding</span>
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700 uppercase tracking-widest">Premium</span>
                          </div>
                          <p className="text-[15px] text-zinc-400 mt-2 leading-relaxed max-w-2xl">Include the company name and logo in the signature request email. Both are required to apply your settings.</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                // Step 2: Signature Details
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
                              // clear uploaded when switching
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
                                <div
                                  key={idx}
                                  onClick={() => setSelectedStyle(idx)}
                                  className={`p-4 border-b border-zinc-100 last:border-0 flex items-center gap-4 cursor-pointer hover:bg-zinc-50 transition-colors ${selectedStyle === idx ? 'bg-zinc-50' : ''}`}
                                >
                                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-none ${selectedStyle === idx ? 'border-emerald-500' : 'border-zinc-300'}`}>
                                    {selectedStyle === idx && <div className="w-2.5 h-2.5 rounded-full bg-emerald-500" />}
                                  </div>
                                  <div
                                    className="text-2xl pt-1 flex-1 overflow-hidden"
                                    style={{ fontFamily: font.name, color: selectedColor, textShadow: "0.5px 0.5px 1px rgba(0,0,0,0.05)" }}
                                  >
                                    {fullName || "Your name"}
                                  </div>
                                </div>
                              ))}
                            </div>

                            <div className="mt-5 flex items-center gap-3 pl-2 flex-none">
                              <span className="text-sm font-medium text-zinc-600">Color:</span>
                              <div className="flex gap-2">
                                {signatureColors.map(color => (
                                  <button
                                    key={color}
                                    onClick={() => setSelectedColor(color)}
                                    className={`w-5 h-5 rounded-full transition-transform ${selectedColor === color ? 'ring-2 ring-offset-2 ring-zinc-400 scale-110' : 'hover:scale-110'}`}
                                    style={{ backgroundColor: color }}
                                  />
                                ))}
                              </div>
                            </div>
                          </div>
                        )}

                        {activeSideTab === "Draw" && (
                          <div className="flex flex-col h-full animate-in fade-in duration-200">
                            <div className="flex-1 w-full bg-zinc-50/50 rounded-lg border border-zinc-200 overflow-hidden relative" style={{ touchAction: 'none' }}>
                              <canvas
                                ref={canvasRef}
                                className="absolute inset-0 w-full h-full cursor-crosshair bg-transparent"
                                onMouseDown={startDrawing}
                                onMouseMove={draw}
                                onMouseUp={stopDrawing}
                                onMouseOut={stopDrawing}
                                onTouchStart={startDrawing}
                                onTouchMove={draw}
                                onTouchEnd={stopDrawing}
                              />
                              {!isDrawing && !canvasRef.current?.getContext('2d')?.getImageData(0, 0, 1, 1) && (
                                <div className="absolute inset-0 flex items-center justify-center pointer-events-none text-zinc-300 select-none">
                                  Sign here
                                </div>
                              )}
                            </div>

                            <div className="mt-5 flex items-center justify-between px-2 flex-none">
                              <div className="flex items-center gap-3">
                                <span className="text-sm font-medium text-zinc-600">Color:</span>
                                <div className="flex gap-2">
                                  {signatureColors.map(color => (
                                    <button
                                      key={color}
                                      onClick={() => setSelectedColor(color)}
                                      className={`w-5 h-5 rounded-full transition-transform ${selectedColor === color ? 'ring-2 ring-offset-2 ring-zinc-400 scale-110' : 'hover:scale-110'}`}
                                      style={{ backgroundColor: color }}
                                    />
                                  ))}
                                </div>
                              </div>
                              <button
                                onClick={clearCanvas}
                                className="text-sm text-zinc-500 hover:text-red-500 transition-colors px-3 py-1 rounded-md hover:bg-red-50 font-medium"
                              >
                                Clear
                              </button>
                            </div>
                          </div>
                        )}

                        {activeSideTab === "Upload" && (
                          <div className="flex flex-col h-full justify-center items-center animate-in fade-in duration-200">
                            {!uploadedSigUrl ? (
                              <div
                                onClick={handleSigUploadClick}
                                className="w-full flex-1 max-h-[300px] border-2 border-dashed border-blue-200 bg-blue-50/30 hover:bg-blue-50 rounded-2xl flex flex-col items-center justify-center cursor-pointer transition-colors group"
                              >
                                <div className="w-16 h-16 bg-white rounded-full shadow-sm flex items-center justify-center text-blue-500 mb-4 group-hover:scale-110 transition-transform">
                                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="17 8 12 3 7 8" /><line x1="12" y1="3" x2="12" y2="15" /></svg>
                                </div>
                                <span className="text-lg font-semibold text-zinc-700">Browse your files</span>
                                <span className="text-sm text-zinc-500 mt-1">Upload a photo of your signature (PNG, JPG)</span>
                                <input
                                  type="file"
                                  className="hidden"
                                  ref={sigInputRef}
                                  accept="image/png, image/jpeg"
                                  onChange={handleSigFileChange}
                                />
                              </div>
                            ) : (
                              <div className="w-full flex-1 flex flex-col items-center justify-center space-y-4">
                                <div className="border border-zinc-200 rounded-xl p-4 bg-zinc-50 flex items-center justify-center max-w-full overflow-hidden" style={{ maxHeight: '200px' }}>
                                  <img src={uploadedSigUrl} alt="Uploaded Signature" className="max-h-full max-w-full object-contain mix-blend-multiply" />
                                </div>
                                <button
                                  onClick={() => setUploadedSigUrl(null)}
                                  className="text-zinc-500 hover:text-red-500 text-sm font-medium"
                                >
                                  Remove image
                                </button>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {(modalStep === "SIGN_DETAILS" || modalStep === "SEVERAL_PEOPLE") && (
              <div className="p-6 border-t border-zinc-100 flex justify-end gap-4 flex-none bg-white rounded-b-2xl z-10 w-full">
                {modalStep === "SEVERAL_PEOPLE" && (
                  <button onClick={closeModal} className="px-6 py-3 text-[#e53935] font-semibold hover:bg-red-50 rounded-lg transition-colors text-lg">
                    Cancel
                  </button>
                )}
                <button
                  onClick={executeUpload}
                  className="px-8 py-3 bg-[#e53935] text-white rounded-lg font-semibold hover:bg-[#d32f2f] transition-all shadow-md shadow-red-500/20 active:scale-95 text-lg"
                >
                  Apply
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}