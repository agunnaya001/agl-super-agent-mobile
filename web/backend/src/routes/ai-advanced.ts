import { Router } from "express";
import { callGemini, AGENT_SYSTEM_PROMPT } from "./ai.js";

export const aiAdvancedRouter = Router();

function notConfigured(res: any, feature: string) {
  res.status(503).json({
    error: "GEMINI_API_KEY_NOT_CONFIGURED",
    reply: `⚠️ The Gemini API key is not configured. Add your GEMINI_API_KEY in the Secrets panel to enable ${feature}.`,
  });
}

// AI Portfolio Health Report
aiAdvancedRouter.post("/portfolio-report", async (req, res) => {
  const { address, portfolio } = req.body as { address?: string; portfolio?: any };
  if (!address) { res.status(400).json({ error: "Wallet address required" }); return; }
  const prompt = `Generate a concise portfolio health report for the Base Mainnet wallet ${address}.
Portfolio data: ${JSON.stringify(portfolio || {})}

Analyze concentration risk, stale positions, gas-spending patterns, and suggest rebalances.
Format as markdown:
### 📊 Portfolio Health Report
**Wallet**: \`${address}\`
#### 1. 🏥 Overall Health Score [0-100]
#### 2. ⚠️ Risk Factors
#### 3. 💡 Recommendations
Keep it under 300 words.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, address });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "portfolio reports");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("portfolio report", e)}` });
  }
});

// AI Transaction Batch Explainer
aiAdvancedRouter.post("/explain-tx", async (req, res) => {
  const { txHash, description } = req.body as { txHash?: string; description?: string };
  const target = txHash || description;
  if (!target || !target.trim()) { res.status(400).json({ error: "Transaction hash or description required" }); return; }
  const prompt = `Explain this Base Mainnet transaction in plain English:
${target}

Break down every internal call, token flow, and estimated dollar value.
Format as markdown:
### 📝 Transaction Breakdown
#### 1. 📋 Summary
#### 2. 🔄 Call-by-Call Analysis
#### 3. 💰 Token Flows
#### 4. 💡 Key Insights
Keep it concise and actionable.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, target });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "transaction explanations");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("transaction explainer", e)}` });
  }
});

// AI Smart-Contract Diff Auditor
aiAdvancedRouter.post("/contract-diff", async (req, res) => {
  const { addressA, addressB } = req.body as { addressA?: string; addressB?: string };
  if (!addressA || !addressB) { res.status(400).json({ error: "Both contract addresses required" }); return; }
  const prompt = `Compare two Base Mainnet smart contracts and highlight what changed and any new risk surface.
Contract A: ${addressA}
Contract B: ${addressB}

Format as markdown:
### 📝 Contract Diff Audit
#### 1. 🔍 Differences Detected
#### 2. ⚠️ New Risk Surface
#### 3. ✅ Security Assessment
Be concise.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, addressA, addressB });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "contract diff audits");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("contract diff", e)}` });
  }
});

// AI Gas-Optimization Coach
aiAdvancedRouter.post("/gas-optimizer", async (req, res) => {
  const { address } = req.body as { address?: string };
  if (!address) { res.status(400).json({ error: "Contract address required" }); return; }
  const prompt = `Analyze the Base Mainnet smart contract at ${address} and suggest gas-saving patterns with estimated savings.
Format as markdown:
### ⛽ Gas Optimization Report
**Contract**: \`${address}\`
#### 1. 📊 Current Gas Profile
#### 2. 💡 Optimization Suggestions (with estimated % savings)
#### 3. 🎯 Priority Ranking
Be concise and specific.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, address });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "gas optimization coaching");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("gas optimizer", e)}` });
  }
});

// AI Address Risk Scoring
aiAdvancedRouter.post("/address-risk", async (req, res) => {
  const { address } = req.body as { address?: string };
  if (!address || !address.trim()) { res.status(400).json({ error: "Address required" }); return; }
  const trimmed = address.trim();
  const prompt = `Assess the risk of the Base Mainnet address ${trimmed}.
Combine on-chain heuristics (honeypot, mintable, proxy, ownership) with AI analysis.
Format as markdown:
### 🛡️ Address Risk Assessment
**Address**: \`${trimmed}\`
#### 1. 📊 Risk Score [0-100] (0=safe, 100=dangerous)
#### 2. 🚩 Risk Flags
- Honeypot: [Yes/No/Unknown]
- Mintable: [Yes/No/Unknown]
- Proxy: [Yes/No/Unknown]
- Ownership: [Renounced/Multi-sig/Single EOA/Unknown]
#### 3. 💡 Recommendation
Be concise.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, address: trimmed });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "address risk scoring");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("address risk scoring", e)}` });
  }
});

// AI Phishing/Similarity Detector
aiAdvancedRouter.post("/phishing-check", async (req, res) => {
  const { target } = req.body as { target?: string };
  if (!target || !target.trim()) { res.status(400).json({ error: "Address or contract name required" }); return; }
  const trimmed = target.trim();
  const prompt = `Check if "${trimmed}" on Base Mainnet is a phishing attempt or impersonation of a known legitimate contract.
Flag addresses or contract names that mimic known legitimate ones (e.g., AGL Token, wAGL, Aerodrome, Coinbase).
Format as markdown:
### 🎣 Phishing & Impersonation Check
**Target**: \`${trimmed}\`
#### 1. ⚠️ Phishing Risk [Safe/Suspicious/Dangerous]
#### 2. 🔍 Similarity to Known Contracts
#### 3. 💡 Recommendation
Be concise.`;
  try {
    const reply = await callGemini(AGENT_SYSTEM_PROMPT, [], prompt);
    res.json({ reply, target: trimmed });
  } catch (e) {
    if ((e as Error).message === "GEMINI_API_KEY_NOT_CONFIGURED") return notConfigured(res, "phishing detection");
    res.status(502).json({ error: (e as Error).message, reply: `⚠️ ${featureFallback("phishing detection", e)}` });
  }
});

function featureFallback(name: string, e: unknown): string {
  return `${name.charAt(0).toUpperCase() + name.slice(1)} failed: ${(e as Error).message}`;
}
