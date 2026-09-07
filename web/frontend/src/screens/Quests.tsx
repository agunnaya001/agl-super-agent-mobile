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
    return <LessonDetail lesson={selected} onBack={() => setSelected(null)} />;
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

function LessonDetail({ lesson, onBack }: { lesson: any; onBack: () => void }) {
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [submitted, setSubmitted] = useState(false);

  const selectAnswer = (qId: string, optId: string) => {
    if (submitted) return;
    setAnswers((prev) => ({ ...prev, [qId]: optId }));
  };

  const correctCount = lesson.quiz.filter((q: any) => answers[q.id] === q.correctOptionId).length;
  const totalQuestions = lesson.quiz.length;
  const scorePct = totalQuestions > 0 ? Math.round((correctCount / totalQuestions) * 100) : 0;
  const passed = scorePct >= 70;

  return (
    <div>
      <button className="tab" onClick={onBack}>← Back</button>
      <div className="card">
        <div className="row between">
          <div className="row gap"><span style={{ fontSize: 24 }}>{lesson.iconEmoji}</span><span style={{ fontSize: 18, fontWeight: 800 }}>{lesson.title}</span></div>
          <span className="pill cyan">+{lesson.xpReward} XP</span>
        </div>
        <div className="small muted" style={{ marginTop: 6 }}>{lesson.category} · {lesson.durationMinutes} min</div>
        <div style={{ height: 12 }} />
        <div className="md" style={{ whiteSpace: "pre-wrap" }}>{lesson.contentMarkdown}</div>
        <div style={{ height: 14 }} />
        <div className="bold">Key Takeaways</div>
        <ul>{lesson.keyTakeaways.map((k: string, i: number) => <li key={i} className="small">{k}</li>)}</ul>
      </div>

      <div className="section-title">Interactive Quiz</div>
      {lesson.quiz.map((q: any, i: number) => {
        const userAnswer = answers[q.id];
        const isCorrect = submitted && userAnswer === q.correctOptionId;
        const isWrong = submitted && userAnswer && userAnswer !== q.correctOptionId;
        return (
          <div key={q.id} className="card">
            <div className="bold small" style={{ marginBottom: 8 }}>Quiz {i + 1}: {q.question}</div>
            {q.options.map((o: any) => {
              const selected = userAnswer === o.id;
              const showCorrect = submitted && o.id === q.correctOptionId;
              const showWrong = submitted && selected && o.id !== q.correctOptionId;
              return (
                <div
                  key={o.id}
                  className="list-row"
                  style={{
                    marginBottom: 6, padding: "10px 12px", cursor: submitted ? "default" : "pointer",
                    borderColor: showCorrect ? "var(--neon)" : showWrong ? "var(--rose)" : selected ? "var(--cyan)" : "var(--border)",
                    background: showCorrect ? "rgba(0,230,153,0.1)" : showWrong ? "rgba(255,51,102,0.1)" : selected ? "rgba(0,212,255,0.08)" : "var(--card)",
                  }}
                  onClick={() => selectAnswer(q.id, o.id)}
                >
                  <span className="small">{o.text}</span>
                  {showCorrect && <span className="pill green" style={{ marginLeft: "auto" }}>✓</span>}
                  {showWrong && <span className="pill rose" style={{ marginLeft: "auto" }}>✗</span>}
                </div>
              );
            })}
            {submitted && isWrong && (
              <div className="tiny muted" style={{ marginTop: 6 }}>✅ Correct: {q.options.find((o: any) => o.id === q.correctOptionId)?.text}</div>
            )}
          </div>
        );
      })}

      {!submitted ? (
        <button className="btn" disabled={Object.keys(answers).length < totalQuestions} onClick={() => setSubmitted(true)}>
          Submit Quiz ({Object.keys(answers).length}/{totalQuestions} answered)
        </button>
      ) : (
        <div className="card card-elev" style={{ borderColor: passed ? "rgba(0,230,153,0.4)" : "rgba(255,51,102,0.4)" }}>
          <div style={{ textAlign: "center" }}>
            <div style={{ fontSize: 32 }}>{passed ? "🎉" : "📚"}</div>
            <div style={{ fontSize: 24, fontWeight: 800, color: passed ? "var(--neon)" : "var(--rose)" }}>{scorePct}%</div>
            <div className="small muted">{correctCount}/{totalQuestions} correct</div>
            <div className="small" style={{ marginTop: 6 }}>
              {passed ? `Passed! +${lesson.xpReward} XP earned` : "Score 70%+ to pass. Review the lesson and try again."}
            </div>
          </div>
          <div style={{ height: 12 }} />
          <button className="btn ghost" onClick={() => { setAnswers({}); setSubmitted(false); }}>↻ Retry Quiz</button>
        </div>
      )}
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
  const STREAK_KEY = "agl-streak";
  const [streak, setStreak] = useState(() => {
    try { return JSON.parse(localStorage.getItem(STREAK_KEY) || '{"days":0,"lastCheckIn":0,"totalCheckIns":0}'); }
    catch { return { days: 0, lastCheckIn: 0, totalCheckIns: 0 }; }
  });
  const [checkedIn, setCheckedIn] = useState(false);

  const today = new Date().setHours(0, 0, 0, 0);
  const lastDay = new Date(streak.lastCheckIn).setHours(0, 0, 0, 0);
  const alreadyCheckedIn = today === lastDay;
  const streakBroken = streak.lastCheckIn > 0 && (today - lastDay) > 86400000;

  const checkIn = () => {
    if (alreadyCheckedIn) return;
    const newStreak = {
      days: streakBroken ? 1 : streak.days + 1,
      lastCheckIn: Date.now(),
      totalCheckIns: streak.totalCheckIns + 1,
    };
    localStorage.setItem(STREAK_KEY, JSON.stringify(newStreak));
    setStreak(newStreak);
    setCheckedIn(true);
  };

  const bonusAgl = 15 + Math.min(streak.days, 30) * 2;
  const bonusXp = 250 + Math.min(streak.days, 30) * 50;

  return (
    <div>
      <div className="card card-elev">
        <div className="row between">
          <div className="row gap"><span style={{ fontSize: 24 }}>🎁</span><span className="bold">Unclaimed Rewards</span></div>
          <span className="pill gold">{15 + streak.days * 5} AGL</span>
        </div>
        <div className="small muted" style={{ marginTop: 8 }}>Complete quests and check in daily to earn AGL bounties.</div>
        <div style={{ height: 12 }} />
        <button className="btn">Claim All Rewards</button>
      </div>
      <div className="card">
        <div className="bold">Daily Check-In Streak</div>
        <div style={{ fontSize: 28, fontWeight: 800, color: "var(--gold)" }}>{streak.days} days 🔥</div>
        <div className="small muted">
          {alreadyCheckedIn
            ? "✅ Already checked in today — come back tomorrow!"
            : streakBroken
              ? "⚠️ Streak was broken. Check in to start a new streak!"
              : streak.days === 0
                ? "Check in to start your streak and earn daily bonuses."
                : `Check in now to extend your streak to ${streak.days + 1} days!`}
        </div>
        <div style={{ height: 10 }} />
        <div className="row between">
          <span className="tiny muted">Today's bonus</span>
          <span className="bold small" style={{ color: "var(--gold)" }}>+{bonusAgl} AGL · +{bonusXp} XP</span>
        </div>
        <div style={{ height: 10 }} />
        <button className="btn" disabled={alreadyCheckedIn} onClick={checkIn}>
          {alreadyCheckedIn ? "✅ Checked In" : checkedIn ? "🎉 Streak Extended!" : "🔥 Check In Today"}
        </button>
        <div className="tiny muted" style={{ marginTop: 8 }}>Total check-ins: {streak.totalCheckIns} · Streak multiplier: {Math.min(streak.days, 30)}x</div>
      </div>
      <div className="card">
        <div className="bold">Reward History</div>
        <div className="list-row" style={{ marginTop: 8 }}>
          <div className="avatar-circle" style={{ background: "rgba(255,215,0,0.15)" }}>🔥</div>
          <div className="col" style={{ flex: 1 }}><div className="bold small">Daily Check-In Streak (Day {Math.max(streak.days, 1)})</div><div className="tiny muted">+{bonusAgl} AGL · +{bonusXp} XP</div></div>
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
