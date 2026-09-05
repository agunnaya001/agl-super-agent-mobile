import express from "express";
import cors from "cors";
import { blockchainRouter } from "./routes/blockchain.js";
import { aiRouter } from "./routes/ai.js";

const app = express();
app.use(cors());
app.use(express.json({ limit: "2mb" }));

app.get("/api/health", (_req, res) => res.json({ ok: true, service: "agl-super-agent-api" }));

app.use("/api", blockchainRouter);
app.use("/api/ai", aiRouter);

const PORT = Number(process.env.PORT) || 8000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`AGL Super Agent API listening on :${PORT}`);
});
