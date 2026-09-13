package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AchievementBadge
import com.example.data.model.SuperAgentTier
import com.example.data.model.UserProfile
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseNeonCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a reward milestone reached by accumulating XP on Base.
 */
data class XpMilestone(
    val id: String,
    val title: String,
    val targetXp: Long,
    val targetLevel: Int,
    val iconEmoji: String,
    val aglReward: Double,
    val creditsReward: Long,
    val perkTitle: String,
    val perkDescription: String
)

/**
 * Standard milestones for Agunnaya Labs ecosystem agents.
 */
val defaultEcosystemMilestones = listOf(
    XpMilestone(
        id = "milestone_lvl25",
        title = "Level 25: Master Sentinel",
        targetXp = 12500L,
        targetLevel = 25,
        iconEmoji = "🛡️",
        aglReward = 50.0,
        creditsReward = 100L,
        perkTitle = "Priority Gas Rebate",
        perkDescription = "Receive 5% rebate on all on-chain Base L2 contract interactions."
    ),
    XpMilestone(
        id = "milestone_lvl26",
        title = "Level 26: Protocol Archon",
        targetXp = 13000L,
        targetLevel = 26,
        iconEmoji = "⚡",
        aglReward = 100.0,
        creditsReward = 250L,
        perkTitle = "Autopilot Subagent Slot 2",
        perkDescription = "Unlocks second parallel AI security agent watcher."
    ),
    XpMilestone(
        id = "milestone_lvl28",
        title = "Level 28: Security Sentinel",
        targetXp = 14000L,
        targetLevel = 28,
        iconEmoji = "🔍",
        aglReward = 150.0,
        creditsReward = 500L,
        perkTitle = "Zero-Latency Scans",
        perkDescription = "Real-time bytecode heuristic audits on unverified bytecode."
    ),
    XpMilestone(
        id = "milestone_diamond",
        title = "Level 40: Diamond Archon",
        targetXp = 20000L,
        targetLevel = 40,
        iconEmoji = "👑",
        aglReward = 500.0,
        creditsReward = 2500L,
        perkTitle = "1.5x DAO Voting Multiplier",
        perkDescription = "Direct vote weight boost on the Agunnaya Governor contract."
    )
)

/**
 * Visual Progress Bar and Badge Display component for the Quests screen.
 * Displays XP accumulation, level progression, upcoming reward milestones, and interactive badges.
 */
@Composable
fun QuestsXpProgressBadgeComponent(
    userProfile: UserProfile?,
    milestones: List<XpMilestone> = defaultEcosystemMilestones,
    onNavigateToLearn: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalXp = userProfile?.totalXp ?: 12450L
    val level = userProfile?.level ?: ((totalXp / 500).toInt() + 1)
    val tier = userProfile?.tier ?: SuperAgentTier.GOLD
    val badges = userProfile?.badges ?: emptyList()

    // Level math: each level corresponds to 500 XP steps
    val currentLevelStartXp = (level - 1) * 500L
    val nextLevelTargetXp = level * 500L
    val xpInCurrentLevel = (totalXp - currentLevelStartXp).coerceAtLeast(0L)
    val xpNeededForNextLevel = (nextLevelTargetXp - totalXp).coerceAtLeast(0L)
    val levelProgressRatio = (xpInCurrentLevel.toFloat() / 500f).coerceIn(0f, 1f)

    // Animated progress for smooth visual feedback
    val animatedProgress by animateFloatAsState(
        targetValue = levelProgressRatio,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    // Shimmer effect for progress bar glow
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    // Interactive dialog states
    var selectedMilestone by remember { mutableStateOf<XpMilestone?>(null) }
    var selectedBadge by remember { mutableStateOf<AchievementBadge?>(null) }
    var badgeFilter by remember { mutableStateOf(BadgeFilter.ALL) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quests_xp_progress_badge_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. XP Accumulation & Progress Hero Card
        // -------------------------------------------------------------
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quests_xp_progress_card"),
            cornerRadius = 16.dp,
            borderColor = BaseBlue.copy(alpha = 0.45f),
            backgroundColor = DarkCardElevated
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tier & Level Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level Badge with glowing backdrop
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(BaseBlue, RadiantPurple)
                                    )
                                )
                                .border(1.5.dp, BaseCyan.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L$level",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tier.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(tier.colorHex)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = Color(tier.colorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Base Mainnet Super Agent",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Total XP Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BaseBlue.copy(alpha = 0.2f))
                            .border(1.dp, BaseCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GoldRewards,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%,d XP".format(totalXp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Level $level Progress",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$xpInCurrentLevel",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                            Text(
                                text = " / 500 XP",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Remaining to next level badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (xpNeededForNextLevel <= 100) NeonEmerald.copy(alpha = 0.18f) else DarkBackground)
                            .border(
                                1.dp,
                                if (xpNeededForNextLevel <= 100) NeonEmerald.copy(alpha = 0.5f) else DarkBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (xpNeededForNextLevel <= 100) "🔥 $xpNeededForNextLevel XP to Level ${level + 1}!" else "$xpNeededForNextLevel XP to Level ${level + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (xpNeededForNextLevel <= 100) NeonEmerald else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Visual Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .testTag("xp_progress_bar")
                ) {
                    // Animated Gradient Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BaseBlue,
                                        BaseCyan,
                                        NeonEmerald.copy(alpha = shimmerAlpha)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Percentage and next tier caption
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%.0f%% Completed".format(levelProgressRatio * 100f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = BaseCyan
                    )
                    Text(
                        text = "Next Tier: ${getNextTier(tier).displayName}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 2. Upcoming Reward Milestones Section
        // -------------------------------------------------------------
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldRewards,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Upcoming Reward Milestones",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Tap for Details",
                    fontSize = 11.sp,
                    color = BaseCyan,
                    modifier = Modifier.testTag("milestones_info_hint")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Milestones horizontal carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("milestones_carousel")
            ) {
                items(milestones) { milestone ->
                    MilestoneCard(
                        milestone = milestone,
                        currentXp = totalXp,
                        onClick = { selectedMilestone = milestone }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // 3. Agent Achievement Badges Display Section
        // -------------------------------------------------------------
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BaseCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Agent Badges",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                val unlockedCount = badges.count { it.isUnlocked }
                Text(
                    text = "$unlockedCount / ${badges.size} Unlocked",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonEmerald
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badge Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgeFilter.values().forEach { filter ->
                    val count = when (filter) {
                        BadgeFilter.ALL -> badges.size
                        BadgeFilter.UNLOCKED -> badges.count { it.isUnlocked }
                        BadgeFilter.LOCKED -> badges.count { !it.isUnlocked }
                    }
                    FilterChip(
                        selected = badgeFilter == filter,
                        onClick = { badgeFilter = filter },
                        label = { Text("${filter.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BaseBlue,
                            selectedLabelColor = Color.White,
                            containerColor = DarkCardElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = badgeFilter == filter,
                            borderColor = DarkBorder,
                            selectedBorderColor = BaseCyan
                        ),
                        modifier = Modifier.testTag("badge_filter_${filter.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges Horizontal Carousel / Grid
            val filteredBadges = when (badgeFilter) {
                BadgeFilter.ALL -> badges
                BadgeFilter.UNLOCKED -> badges.filter { it.isUnlocked }
                BadgeFilter.LOCKED -> badges.filter { !it.isUnlocked }
            }

            if (filteredBadges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No badges in this category.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.testTag("badges_carousel")
                ) {
                    items(filteredBadges) { badge ->
                        AchievementBadgeCard(
                            badge = badge,
                            onClick = { selectedBadge = badge }
                        )
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Milestone Detail Dialog
    // -----------------------------------------------------------------
    selectedMilestone?.let { milestone ->
        MilestoneDetailDialog(
            milestone = milestone,
            currentXp = totalXp,
            onDismiss = { selectedMilestone = null },
            onLearnMore = {
                selectedMilestone = null
                onNavigateToLearn()
            }
        )
    }

    // -----------------------------------------------------------------
    // Badge Detail Dialog
    // -----------------------------------------------------------------
    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadge = null },
            onActionClick = {
                selectedBadge = null
                if (!badge.isUnlocked) {
                    onNavigateToLearn()
                } else {
                    onShowSnackbar("Badge '${badge.title}' verified on Base.")
                }
            }
        )
    }
}

/**
 * Filter categories for achievement badges.
 */
enum class BadgeFilter(val label: String) {
    ALL("All Badges"),
    UNLOCKED("Unlocked"),
    LOCKED("Locked")
}

/**
 * Visual card for displaying an upcoming milestone in the carousel.
 */
@Composable
fun MilestoneCard(
    milestone: XpMilestone,
    currentXp: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = currentXp >= milestone.targetXp
    val xpRemaining = (milestone.targetXp - currentXp).coerceAtLeast(0L)
    val progressRatio = (currentXp.toFloat() / milestone.targetXp.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCompleted) DarkCardElevated else DarkCard)
            .border(
                1.dp,
                if (isCompleted) NeonEmerald.copy(alpha = 0.6f)
                else if (xpRemaining <= 500) GoldRewards.copy(alpha = 0.5f)
                else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("milestone_card_${milestone.id}")
    ) {
        Column {
            // Milestone Header & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) NeonEmerald.copy(alpha = 0.2f) else DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = milestone.iconEmoji, fontSize = 16.sp)
                }

                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("UNLOCKED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                    }
                } else if (xpRemaining <= 500) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldRewards.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ALMOST THERE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = GoldRewards)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = milestone.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Target: %,d XP".format(milestone.targetXp),
                fontSize = 10.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressRatio)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isCompleted) NeonEmerald else BaseCyan)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rewards Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "+%.0f AGL".format(milestone.aglReward),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldRewards
                )
                Text(
                    text = "+${milestone.creditsReward} Credits",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BaseCyan
                )
            }
        }
    }
}

/**
 * Visual card for displaying an achievement badge in the carousel.
 */
@Composable
fun AchievementBadgeCard(
    badge: AchievementBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Box(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (badge.isUnlocked) DarkCardElevated else DarkCard.copy(alpha = 0.6f))
            .border(
                1.dp,
                if (badge.isUnlocked) BaseCyan.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emblem Icon with status ring
            val emblemBgModifier = if (badge.isUnlocked) {
                Modifier.background(Brush.linearGradient(listOf(BaseBlue.copy(alpha = 0.3f), BaseCyan.copy(alpha = 0.2f))))
            } else {
                Modifier.background(DarkBackground)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .then(emblemBgModifier)
                    .border(
                        1.5.dp,
                        if (badge.isUnlocked) BaseCyan else DarkBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) TextPrimary else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (badge.isUnlocked) {
                    badge.unlockedAt?.let { "Earned ${dateFormatter.format(Date(it))}" } ?: "Unlocked"
                } else {
                    "In Progress"
                },
                fontSize = 9.sp,
                color = if (badge.isUnlocked) NeonEmerald else TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Detailed information dialog when a milestone is tapped.
 */
@Composable
fun MilestoneDetailDialog(
    milestone: XpMilestone,
    currentXp: Long,
    onDismiss: () -> Unit,
    onLearnMore: () -> Unit
) {
    val isCompleted = currentXp >= milestone.targetXp
    val xpRemaining = (milestone.targetXp - currentXp).coerceAtLeast(0L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reward Milestone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milestone Emblem
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BaseBlue.copy(alpha = 0.4f), RadiantPurple.copy(alpha = 0.3f))
                            )
                        )
                        .border(1.5.dp, GoldRewards, CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = milestone.iconEmoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = milestone.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Requires %,d Total XP".format(milestone.targetXp),
                    fontSize = 12.sp,
                    color = BaseCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress status card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBackground)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = if (isCompleted) "Completed & Claimable" else "In Progress",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) NeonEmerald else GoldRewards
                            )
                        }
                        if (!isCompleted) {
                            Text(
                                text = "%,d XP remaining".format(xpRemaining),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unlocked Rewards list
                Text(
                    text = "Milestone Bounties & Perks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RewardPill(label = "+%.0f AGL".format(milestone.aglReward), color = GoldRewards)
                    RewardPill(label = "+${milestone.creditsReward} Credits", color = BaseCyan)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Perk Description
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "🎁 ${milestone.perkTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseNeonCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = milestone.perkDescription,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLearnMore,
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("milestone_learn_more_btn")
                ) {
                    Text("Earn XP with Missions & Lessons", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Detailed information dialog when an achievement badge is tapped.
 */
@Composable
fun BadgeDetailDialog(
    badge: AchievementBadge,
    onDismiss: () -> Unit,
    onActionClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (badge.isUnlocked) BaseCyan else DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievement Badge",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val dialogEmblemBgModifier = if (badge.isUnlocked) {
                    Modifier.background(Brush.linearGradient(listOf(BaseBlue.copy(alpha = 0.4f), BaseCyan.copy(alpha = 0.3f))))
                } else {
                    Modifier.background(DarkBackground)
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .then(dialogEmblemBgModifier)
                        .border(
                            1.5.dp,
                            if (badge.isUnlocked) BaseCyan else DarkBorder,
                            CircleShape
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = badge.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (badge.isUnlocked) NeonEmerald.copy(alpha = 0.12f) else DarkBackground)
                        .border(
                            1.dp,
                            if (badge.isUnlocked) NeonEmerald.copy(alpha = 0.4f) else DarkBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) NeonEmerald else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (badge.isUnlocked) "Verified Agent Credential on Base" else "Locked: Complete prerequisite quests to unlock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (badge.isUnlocked) NeonEmerald else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (badge.isUnlocked) BaseBlue else BaseCyan.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("badge_dialog_action_btn")
                ) {
                    Text(
                        text = if (badge.isUnlocked) "Verified on Base Explorer" else "Go to Learning Center",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RewardPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Token,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun getNextTier(currentTier: SuperAgentTier): SuperAgentTier {
    return when (currentTier) {
        SuperAgentTier.BRONZE -> SuperAgentTier.SILVER
        SuperAgentTier.SILVER -> SuperAgentTier.GOLD
        SuperAgentTier.GOLD -> SuperAgentTier.DIAMOND
        SuperAgentTier.DIAMOND -> SuperAgentTier.DIAMOND
    }
}
