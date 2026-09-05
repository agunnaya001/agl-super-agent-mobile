import { Router } from "express";

export const aiRouter = Router();

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_MODEL = process.env.GEMINI_MODEL || "gemini-3.6-flash";
const GEMINI_BASE = "https://generativelanguage.googleapis.com";

const AGENT_SYSTEM_PROMPT = `You are the AGL Super Agent, an AI Web3 intelligence command center for Base Mainnet (Chain ID 8453).
You help users understand transactions, analyze smart contracts, audit security risks, explain DeFi/yield strategies, and guide Web3 learning.
You operate in a watch-only / zero-private-key security model: never ask for or suggest sharing private keys or seed phrases.
Be concise, clear, and actionable. Use markdown formatting with short paragraphs and bullet points where helpful.
When discussing the AGL ecosystem, reference its contracts: AGL Token (ERC-20), wAGL (ERC-20Votes governance wrapper), AGL Compute Credits, AGL Staking, DAO Governor, and Timelock Controller.`;

interface GeminiContent { role?: string; parts: { text: string }[]; }
interface GeminiRequest { contents: GeminiContent[]; systemInstruction?: GeminiContent; }

async function callGemini(systemPrompt: string, history: { role: string; text: string }[], userPrompt: string): Promise<string> {
  if (!GEMINI_API_KEY || GEMINI_API_KEY === "MY_GEMINI_API_KEY") {
    throw new Error("GEMINI_API_KEY_NOT_CONFIGURED");
  }
  const contents: GeminiContent[] = history.slice(-6).map((m) => ({
    role: m.role === "user" ? "user" : "model",
    parts: [{ text: m.text }],
  }));
  contents.push({ role: "user", parts: [{ text: userPrompt }] });

  const body: GeminiRequest = {
    contents,
    systemInstruction: { parts: [{ text: systemPrompt }] },
  };

  const url = `${GEMINI_BASE}/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;
  let resp: Response;
  let attempt = 0;
  do {
    resp = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (resp.ok || resp.status !== 503) break;
    attempt++;
    if (attempt < 3) await new Promise((r) => setTimeout(r, 800));
  } while (attempt < 3);
  if (!resp.ok) {
    const errText = await resp.text();
    throw new Error(`Gemini API error ${resp.status}: ${errText.slice(0, 300)}`);
  }
  const data = await resp.json() as { candidates?: { content?: { parts?: { text?: string }[] } }[] };
  const reply = data.candidates?.[0]?.content?.parts?.[0]?.text;
  return reply || "No response received from the AI Super Agent.";
}

aiRouter.post("/chat", async (req, res) => {
  const { message, history } = req.body as { message?: string; history?: { role: string; text: string }[] };
  if (!message || !message.trim()) { res.status(400).json({ error: "Message required" }); return; }
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, history ?? [], message);
    res.json({ reply, sender: "AGENT" });
  } catch (e) {
    const msg = (e as Error).message;
    if (msg === "GEMINI_API_KEY_NOT_CONFIGURED") {
      res.status(503).json({ error: "Gemini API key not configured. Set GEMINI_API_KEY in your secrets.", reply: "⚠️ The Gemini API key is not configured. Add your GEMINI_API_KEY in the Secrets panel to enable AI features." });
    } else {
      res.status(502).json({ error: msg, reply: `⚠️ AI request failed: ${msg}` });
    }
  }
});

aiRouter.post("/analyze-contract", async (req, res) => {
  const { address } = req.body as { address?: string };
  if (!address || !address.trim()) { res.status(400).json({ error: "Contract address required" }); return; }
  const prompt = `Analyze the Base Mainnet smart contract at address ${address}.
Provide a structured analysis covering:
1. **Overview** — likely contract type and purpose
2. **Read Functions** — key view functions and what they return
3. **Write Functions** — state-changing functions and access controls
4. **Security Assessment** — risk level (Low/Review/High) with reasons
5. **Summary** — a plain-English explanation
Be concise. If this is an AGL ecosystem contract, note that.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, address });
  } catch (e) {
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ Analysis failed: ${(e as Error).message}` });
  }
});

aiRouter.post("/audit-security", async (req, res) => {
  const { target } = req.body as { target?: string };
  if (!target || !target.trim()) { res.status(400).json({ error: "Target required" }); return; }
  const prompt = `Perform a Web3 security audit on "${target}" (could be a wallet address, contract address, or transaction hash) on Base Mainnet.
Provide:
1. **Risk Level** — Low / Review / High Concern
2. **Risk Score** — 0-100
3. **Summary**
4. **Risk Flags** — specific concerns (reentrancy, front-running, malicious approvals, owner backdoors, fee taxes)
5. **Recommendations** — actionable security steps
Be concise and use markdown.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, target });
  } catch (e) {
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ Audit failed: ${(e as Error).message}` });
  }
});
