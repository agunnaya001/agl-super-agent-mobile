import { useEffect, useState } from "react";
import { api } from "../api";
import { tierPill } from "../ui";

export function QuestsScreen() {
  const [tab, setTab] = useState<"MISSIONS" | "LEARNING" | "LEADERBOARD" | "REWARDS">("MISSIONS");
  return (
    <div>
      <div className="tabs">
        <button className={`tab ${tab === "MISSIONS" ? "active" : ""}`} onClick={() => setTab("MISSIONS")}>🎯 Missions</button>
        <button className={`tab ${tab === "LEARNING" ? "active" : ""}`} onClick={() => setTab("LEARNING")}>📚 Learning</button>
        <button className={`tab ${tab === "LEADERBOARD" ? "active" : ""}`} onClick={() => setTab("LEADERBOARD")}>🏆 Leaderboard</button>
        <button className={`tab ${tab === "REWARDS" ? "active" : ""}`} onClick={() => setTab("REWARDS")}>🎁 Rewards</button>
      </div>
      {tab === "MISSIONS" && <MissionsTab />}
      {tab === "LEARNING" && <LearningTab />}
      {tab === "LEADERBOARD" && <LeaderboardTab />}
      {tab === "REWARDS" && <RewardsTab />}
    </div>
  );
}

function MissionsTab() {
  const [quests, setQuests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => { api.getQuests().then((q) => { setQuests(q); setLoading(false); }).catch(() => setLoading(false)); }, []);
  if (loading) return <div className="center"><div className="loader" /></div>;
  return (
    <div>
      {quests.map((q) => (
        <div key={q.id} className="card">
          <div className="row between">
            <div className="row gap"><span style={{ fontSize: 22 }}>{q.iconEmoji}</span><span className="bold">{q.title}</span></div>
            <span className="pill gold">+{q.xpReward} XP</span>
          </div>
          <div className="small muted" style={{ marginTop: 6 }}>{q.description}</div>
          <div style={{ height: 10 }} />
          <div className="progress-track"><div className="progress-fill" style={{ width: `${Math.min(100, (q.currentProgress / q.maxProgress) * 100)}%` }} /></div>
          <div className="row between" style={{ marginTop: 6 }}>
            <span className="tiny muted">{q.currentProgress}/{q.maxProgress} · {q.category}</span>
            <span className="tiny" style={{ color: "var(--gold)" }}>{q.aglReward} AGL</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function LearningTab() {
  const [lessons, setLessons] = useState<any[]>([]);
  const [selected, setSelected] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => { api.getLessons().then((l) => { setLessons(l); setLoading(false); }).catch(() => setLoading(false)); }, []);
  if (loading) return <div className="center"><div className="loader" /></div>;

  if (selected) {
    return (
      <div>
        <button className="tab" onClick={() => setSelected(null)}>← Back</button>
        <div className="card">
          <div className="row between">
            <div className="row gap"><span style={{ fontSize: 24 }}>{selected.iconEmoji}</span><span style={{ fontSize: 18, fontWeight: 800 }}>{selected.title}</span></div>
            <span className="pill cyan">+{selected.xpReward} XP</span>
          </div>
          <div className="small muted" style={{ marginTop: 6 }}>{selected.category} · {selected.durationMinutes} min</div>
          <div style={{ height: 12 }} />
          <div className="md" style={{ whiteSpace: "pre-wrap" }}>{selected.contentMarkdown}</div>
          <div style={{ height: 14 }} />
          <div className="bold">Key Takeaways</div>
          <ul>{selected.keyTakeaways.map((k: string, i: number) => <li key={i} className="small">{k}</li>)}</ul>
        </div>
        {selected.quiz.map((q: any, i: number) => (
          <div key={q.id} className="card">
            <div className="bold small" style={{ marginBottom: 8 }}>Quiz {i + 1}: {q.question}</div>
            {q.options.map((o: any) => (
              <div key={o.id} className="list-row" style={{ marginBottom: 6, padding: "10px 12px" }}>
                <span className="small">{o.text}</span>
              </div>
            ))}
            <div className="tiny muted" style={{ marginTop: 8 }}>✅ Correct: {q.options.find((o: any) => o.id === q.correctOptionId)?.text}</div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div>
      {lessons.map((l) => (
        <div key={l.id} className="card" style={{ cursor: "pointer" }} onClick={() => setSelected(l)}>
          <div className="row between">
            <div className="row gap"><span style={{ fontSize: 22 }}>{l.iconEmoji}</span>
              <div><div className="bold">{l.title}</div><div className="tiny muted">{l.category} · {l.durationMinutes} min</div></div>
            </div>
            <span className="pill cyan">+{l.xpReward} XP</span>
          </div>
          <div className="small muted" style={{ marginTop: 6 }}>{l.shortSummary}</div>
        </div>
      ))}
    </div>
  );
}

function LeaderboardTab() {
  const [tf, setTf] = useState("WEEKLY");
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => { setLoading(true); api.getLeaderboard(tf).then((u) => { setUsers(u); setLoading(false); }).catch(() => setLoading(false)); }, [tf]);
  return (
    <div>
      <div className="tabs">
        {["DAILY", "WEEKLY", "MONTHLY", "ALL_TIME"].map((t) => (
          <button key={t} className={`tab ${tf === t ? "active" : ""}`} onClick={() => setTf(t)}>{t.replace("_", " ")}</button>
        ))}
      </div>
      {loading ? <div className="center"><div className="loader" /></div> : users.map((u) => (
        <div key={u.rank} className="list-row" style={u.isCurrentUser ? { borderColor: "var(--cyan)" } : {}}>
          <div className="avatar-circle" style={{ background: "var(--surface)" }}>{u.avatarEmoji}</div>
          <div className="col" style={{ flex: 1 }}>
            <div className="row gap"><span className="bold">#{u.rank}</span><span>{u.username}</span></div>
            <div className="tiny muted">{u.xp.toLocaleString()} XP · Lv {u.level}</div>
          </div>
          {tierPill(u.tier)}
        </div>
      ))}
    </div>
  );
}

function RewardsTab() {
  return (
    <div>
      <div className="card card-elev">
        <div className="row between">
          <div className="row gap"><span style={{ fontSize: 24 }}>🎁</span><span className="bold">Unclaimed Rewards</span></div>
          <span className="pill gold">45 AGL</span>
        </div>
        <div className="small muted" style={{ marginTop: 8 }}>Complete quests and check in daily to earn AGL bounties.</div>
        <div style={{ height: 12 }} />
        <button className="btn">Claim All Rewards</button>
      </div>
      <div className="card">
        <div className="bold">Daily Check-In Streak</div>
        <div style={{ fontSize: 28, fontWeight: 800, color: "var(--gold)" }}>7 days 🔥</div>
        <div className="small muted">Check in daily to extend your streak and earn multiplier bonuses.</div>
      </div>
      <div className="card">
        <div className="bold">Reward History</div>
        <div className="list-row" style={{ marginTop: 8 }}>
          <div className="avatar-circle" style={{ background: "rgba(255,215,0,0.15)" }}>🔥</div>
          <div className="col" style={{ flex: 1 }}><div className="bold small">Daily Check-In Streak (Day 7)</div><div className="tiny muted">+15 AGL · +250 XP</div></div>
          <span className="pill green">CLAIMED</span>
        </div>
        <div className="list-row">
          <div className="avatar-circle" style={{ background: "rgba(0,82,255,0.15)" }}>🧭</div>
          <div className="col" style={{ flex: 1 }}><div className="bold small">Base L2 Explorer Bounty</div><div className="tiny muted">+30 AGL · +400 XP</div></div>
          <span className="pill green">CLAIMED</span>
        </div>
      </div>
    </div>
  );
}
