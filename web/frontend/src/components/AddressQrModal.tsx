import React, { useEffect, useState, useRef } from "react";
import QRCode from "qrcode";

interface AddressQrModalProps {
  address: string;
  isOpen: boolean;
  onClose: () => void;
}

export function AddressQrModal({ address, isOpen, onClose }: AddressQrModalProps) {
  const [qrFormat, setQrFormat] = useState<"plain" | "eip681">("plain");
  const [svgContent, setSvgContent] = useState<string>("");
  const [dataUrl, setDataUrl] = useState<string>("");
  const [copied, setCopied] = useState<boolean>(false);
  const [shared, setShared] = useState<boolean>(false);
  const modalRef = useRef<HTMLDivElement>(null);

  // Compute text to encode: plain hex vs EIP-681 URI (standard for EVM mobile scanning)
  const encodedText = qrFormat === "eip681" 
    ? `ethereum:${address}@8453` 
    : address;

  useEffect(() => {
    if (!address || !isOpen) return;

    let isMounted = true;

    // Generate SVG string for crisp vector rendering
    QRCode.toString(encodedText, {
      type: "svg",
      margin: 2,
      color: {
        dark: "#000000",
        light: "#FFFFFF"
      },
      errorCorrectionLevel: "M"
    })
      .then((svg) => {
        if (isMounted) setSvgContent(svg);
      })
      .catch((err) => {
        console.error("Failed to generate SVG QR code", err);
      });

    // Also generate PNG DataURL for direct download
    QRCode.toDataURL(encodedText, {
      width: 400,
      margin: 2,
      color: {
        dark: "#000000",
        light: "#FFFFFF"
      },
      errorCorrectionLevel: "M"
    })
      .then((url) => {
        if (isMounted) setDataUrl(url);
      })
      .catch((err) => {
        console.error("Failed to generate PNG DataURL", err);
      });

    return () => {
      isMounted = false;
    };
  }, [encodedText, isOpen]);

  // Handle escape key to close
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isOpen) {
        onClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(address);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: "My Base Public Address",
          text: address,
          url: `https://basescan.org/address/${address}`
        });
        setShared(true);
        setTimeout(() => setShared(false), 2000);
      } catch (err: any) {
        if (err.name !== "AbortError") {
          handleCopy();
        }
      }
    } else {
      handleCopy();
    }
  };

  const handleDownload = () => {
    if (!dataUrl) return;
    const a = document.createElement("a");
    a.href = dataUrl;
    a.download = `base-wallet-${address.slice(0, 8)}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0, 0, 0, 0.78)",
        backdropFilter: "blur(6px)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 9999,
        padding: 16
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        ref={modalRef}
        className="card card-elev"
        style={{
          width: "100%",
          maxWidth: 440,
          background: "var(--card, #121824)",
          border: "1px solid rgba(0, 240, 255, 0.3)",
          boxShadow: "0 16px 40px rgba(0, 0, 0, 0.6), 0 0 25px rgba(0, 240, 255, 0.15)",
          borderRadius: 20,
          padding: 24,
          maxHeight: "90vh",
          overflowY: "auto",
          position: "relative"
        }}
      >
        {/* Header */}
        <div className="row between" style={{ marginBottom: 16 }}>
          <div className="row gap" style={{ alignItems: "center" }}>
            <span style={{ fontSize: 22 }}>📱</span>
            <div>
              <div className="bold" style={{ fontSize: 16, color: "var(--fg, #FFF)" }}>
                Wallet Public Address QR
              </div>
              <div className="tiny" style={{ color: "var(--cyan, #00f0ff)" }}>
                Base Mainnet · Chain ID 8453
              </div>
            </div>
          </div>
          <button
            onClick={onClose}
            aria-label="Close"
            style={{
              background: "rgba(255, 255, 255, 0.08)",
              border: "1px solid var(--border, rgba(255,255,255,0.1))",
              color: "var(--muted, #94a3b8)",
              width: 32,
              height: 32,
              borderRadius: "50%",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 16,
              transition: "all 0.2s"
            }}
          >
            ✕
          </button>
        </div>

        {/* Format Selector: Plain Address vs EIP-681 URI */}
        <div
          style={{
            display: "flex",
            background: "var(--surface, #0b0f19)",
            borderRadius: 10,
            padding: 3,
            marginBottom: 16,
            border: "1px solid var(--border, rgba(255,255,255,0.08))"
          }}
        >
          <button
            onClick={() => setQrFormat("plain")}
            style={{
              flex: 1,
              padding: "6px 10px",
              borderRadius: 8,
              border: "none",
              fontSize: 11,
              fontWeight: qrFormat === "plain" ? 700 : 500,
              background: qrFormat === "plain" ? "var(--cyan, #00f0ff)" : "transparent",
              color: qrFormat === "plain" ? "#000" : "var(--muted, #94a3b8)",
              cursor: "pointer",
              transition: "all 0.2s"
            }}
          >
            Plain Address
          </button>
          <button
            onClick={() => setQrFormat("eip681")}
            style={{
              flex: 1,
              padding: "6px 10px",
              borderRadius: 8,
              border: "none",
              fontSize: 11,
              fontWeight: qrFormat === "eip681" ? 700 : 500,
              background: qrFormat === "eip681" ? "var(--cyan, #00f0ff)" : "transparent",
              color: qrFormat === "eip681" ? "#000" : "var(--muted, #94a3b8)",
              cursor: "pointer",
              transition: "all 0.2s"
            }}
          >
            EIP-681 Web3 URI
          </button>
        </div>

        {/* High-Contrast Crisp QR Code Container */}
        <div
          style={{
            background: "#FFFFFF",
            borderRadius: 16,
            padding: 16,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            boxShadow: "0 8px 24px rgba(0, 0, 0, 0.35)",
            margin: "0 auto 16px auto",
            maxWidth: 240,
            width: "100%",
            aspectRatio: "1/1"
          }}
        >
          {svgContent ? (
            <div
              style={{
                width: "100%",
                height: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
              }}
              dangerouslySetInnerHTML={{ __html: svgContent }}
            />
          ) : (
            <div className="tiny muted" style={{ color: "#666" }}>
              Generating QR Code...
            </div>
          )}
        </div>

        <div style={{ textAlign: "center", marginBottom: 16 }}>
          <span
            className="pill"
            style={{
              background: "rgba(0, 82, 255, 0.15)",
              borderColor: "rgba(0, 82, 255, 0.4)",
              color: "#3b82f6",
              fontSize: 10,
              letterSpacing: 0.5
            }}
          >
            {qrFormat === "plain" ? "RAW HEX (0x...)" : "EIP-681 (ethereum:0x...@8453)"}
          </span>
        </div>

        {/* Public Address Display Card */}
        <div
          style={{
            background: "var(--surface, #0b0f19)",
            border: "1px solid var(--border, rgba(255,255,255,0.08))",
            borderRadius: 12,
            padding: 12,
            marginBottom: 16
          }}
        >
          <div className="tiny muted" style={{ marginBottom: 4, display: "flex", justifyContent: "space-between" }}>
            <span>Public Address</span>
            <span style={{ color: "var(--emerald, #10b981)" }}>Base L2 Compatible</span>
          </div>
          <div
            className="mono small"
            style={{
              color: "var(--cyan, #00f0ff)",
              wordBreak: "break-all",
              userSelect: "all",
              fontSize: 12,
              lineHeight: 1.4
            }}
          >
            {address}
          </div>
        </div>

        {/* Action Buttons */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 12 }}>
          <button
            className="btn"
            style={{
              background: copied ? "var(--emerald, #10b981)" : "var(--blue, #0052FF)",
              color: "#FFF",
              fontWeight: 700,
              fontSize: 12,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 6,
              transition: "background 0.2s"
            }}
            onClick={handleCopy}
          >
            <span>{copied ? "✓" : "📋"}</span>
            <span>{copied ? "Copied!" : "Copy Address"}</span>
          </button>

          <button
            className="btn"
            style={{
              background: "var(--surface, #0b0f19)",
              border: "1px solid var(--border, rgba(255,255,255,0.15))",
              color: "var(--fg, #FFF)",
              fontWeight: 600,
              fontSize: 12,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: 6
            }}
            onClick={handleShare}
          >
            <span>{shared ? "✓" : "🔗"}</span>
            <span>{shared ? "Shared!" : "Share Link"}</span>
          </button>
        </div>

        <button
          className="btn"
          style={{
            width: "100%",
            background: "transparent",
            border: "1px dashed rgba(0, 240, 255, 0.4)",
            color: "var(--cyan, #00f0ff)",
            fontSize: 11,
            fontWeight: 600,
            padding: "8px 12px",
            marginBottom: 14,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: 6
          }}
          onClick={handleDownload}
        >
          <span>💾</span>
          <span>Download QR Image (PNG)</span>
        </button>

        {/* Guidance / Security Notice */}
        <div
          className="tiny muted"
          style={{
            textAlign: "center",
            fontSize: 11,
            lineHeight: 1.4,
            padding: "0 6px"
          }}
        >
          Scan with your phone's camera or any mobile wallet (Coinbase Wallet, MetaMask, Rainbow) to receive ETH, AGL, and ERC-20 tokens on Base Mainnet.
        </div>
      </div>
    </div>
  );
}
