const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://signflow-qzv8.onrender.com";

export const loginUser = async (email: string, password: string) => {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });

  if (!res.ok) throw new Error("Login failed");
  return res.json();
};

export const registerUser = async (
  username: string,
  email: string,
  password: string
) => {
  const res = await fetch(`${BASE_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password })
  });

  if (!res.ok) throw new Error("Register failed");
  return res.json();
};

export interface DocumentDto {
  id: number;
  filename: string;
  originalFilename: string;
  fileSize: number;
  mimeType: string;
  uploadDate: string;
  isSigned: boolean;
  fileStatus: string;
}

export interface DocumentDetailDto extends DocumentDto {
  filePath?: string;
  description?: string;
  isSignatureRequired?: boolean;
  uploadedByUsername?: string;
}

// ... existing code ...
export const getDocuments = async (token: string): Promise<DocumentDto[]> => {
  const res = await fetch(`${BASE_URL}/api/documents`, {
    headers: { Authorization: `Bearer ${token}` }
  });

  if (!res.ok) throw new Error("Failed to fetch docs");
  return res.json();
};

export const getDocumentById = async (id: string | number, token: string): Promise<DocumentDetailDto> => {
  const res = await fetch(`${BASE_URL}/api/documents/${id}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) throw new Error("Failed to fetch document details");
  return res.json();
};

export const uploadDocument = async (file: File, title: string, token: string): Promise<DocumentDto> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("title", title);

  const res = await fetch(`${BASE_URL}/api/documents/upload`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: formData
  });
  if (!res.ok) throw new Error("Upload failed");
  return res.json();
};

export interface SignaturePosition {
  pageNumber: number;
  xPercent: number;
  yPercent: number;
  width: number;
  height: number;
}

export const finalizeSignature = async (
  id: string | number,
  signatureBase64: string,
  positions: SignaturePosition[],
  token: string
) => {
  const res = await fetch(`${BASE_URL}/api/documents/${id}/finalize-signature`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ signatureBase64, positions })
  });
  if (!res.ok) throw new Error("Signature finalization failed");
  return res.text();
};

export const sendSignatureRequest = async (
  id: string | number,
  requestData: any,
  token: string
) => {
  const res = await fetch(`${BASE_URL}/api/documents/${id}/send-request`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(requestData)
  });
  if (!res.ok) throw new Error("Sending signature request failed");
  return res.text();
};

export const downloadSignedDocument = async (id: string | number, token: string) => {
  const res = await fetch(`${BASE_URL}/api/documents/${id}/download-signed`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) throw new Error("Download failed");

  // Create blob and trigger download
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `signed_document_${id}.pdf`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};