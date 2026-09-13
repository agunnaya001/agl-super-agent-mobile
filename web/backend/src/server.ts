import express from "express";
import cors from "cors";
import { blockchainRouter } from "./routes/blockchain.js";
import { aiRouter } from "./routes/ai.js";

const app = express();
const allowedOrigins = (process.env.ALLOWED_ORIGINS || "http://localhost:3000,http://localhost:5173").split(",").map((origin) => origin.trim()).filter(Boolean);
app.use(cors({ origin: (origin, callback) => {
  if (!origin || allowedOrigins.includes(origin)) return callback(null, true);
  callback(new Error("Origin not allowed"));
} }));
app.use(express.json({ limit: "256kb" }));
app.disable("x-powered-by");
app.use((_req, res, next) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  next();
});

app.get("/api/health", (_req, res) => res.json({ ok: true, service: "agl-super-agent-api" }));

app.use("/api", blockchainRouter);
app.use("/api/ai", aiRouter);

const PORT = Number(process.env.PORT) || 8000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`AGL Super Agent API listening on :${PORT}`);
});
