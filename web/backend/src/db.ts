import pg from "pg";

const { Pool } = pg;

export const pool = process.env.DATABASE_URL
  ? new Pool({ connectionString: process.env.DATABASE_URL, max: 5, ssl: { rejectUnauthorized: false } })
  : null;

export async function saveConversationMessage(sessionKey: string, role: "user" | "assistant" | "system", message: string) {
  if (!pool) return;
  await pool.query(
    "INSERT INTO ai_conversations (session_key, role, message) VALUES ($1, $2, $3)",
    [sessionKey, role, message],
  );
}

export async function getConversationHistory(sessionKey: string) {
  if (!pool) return [];
  const result = await pool.query(
    "SELECT id, role, message, created_at FROM ai_conversations WHERE session_key = $1 ORDER BY created_at ASC LIMIT 100",
    [sessionKey],
  );
  return result.rows;
}
