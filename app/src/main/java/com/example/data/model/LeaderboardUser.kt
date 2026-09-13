package com.example.data.model

data class LeaderboardUser(
    val rank: Int,
    val address: String,
    val username: String,
    val xp: Long,
    val level: Int,
    val tier: SuperAgentTier,
    val questsCompleted: Int,
    val isCurrentUser: Boolean = false,
    val avatarEmoji: String = "⚡"
)

enum class LeaderboardTimeframe {
    DAILY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}

enum class SuperAgentTier(val displayName: String, val colorHex: Long) {
    BRONZE("Bronze Agent", 0xFFCD7F32),
    SILVER("Silver Agent", 0xFFC0C0C0),
    GOLD("Gold Super Agent", 0xFFFFD700),
    DIAMOND("Diamond Archon", 0xFF00F0FF)
}

data class UserProfile(
    val walletAddress: String,
    val totalXp: Long,
    val level: Int,
    val tier: SuperAgentTier,
    val questsCompletedCount: Int,
    val lessonsCompletedCount: Int,
    val contractsAnalyzedCount: Int,
    val totalRewardsClaimedAgl: Double,
    val consecutiveStreakDays: Int,
    val badges: List<AchievementBadge>
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)
