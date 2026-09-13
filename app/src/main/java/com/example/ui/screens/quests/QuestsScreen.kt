package com.example.ui.screens.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.LeaderboardUser
import com.example.data.model.LearningLesson
import com.example.data.model.QuestCategory
import com.example.data.model.QuestItem
import com.example.data.model.UserProfile
import com.example.ui.components.QuestsXpProgressBadgeComponent
import com.example.ui.screens.home.QuestItemRow
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkNav
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuestsSubTab

@Composable
fun QuestsScreen(
    currentSubTab: QuestsSubTab,
    onSubTabSelected: (QuestsSubTab) -> Unit,
    quests: List<QuestItem>,
    selectedCategory: QuestCategory?,
    onSelectCategory: (QuestCategory?) -> Unit,
    onClaimQuest: (String) -> Unit,
    // Learning props
    lessons: List<LearningLesson>,
    completedLessonIdsCsv: String,
    onSelectLesson: (LearningLesson) -> Unit,
    // Leaderboard props
    leaderboardUsers: List<LeaderboardUser>,
    selectedLeaderboardTimeframe: LeaderboardTimeframe,
    onSelectLeaderboardTimeframe: (LeaderboardTimeframe) -> Unit,
    // Rewards props
    userProfile: UserProfile?,
    rewardsHistory: List<RewardHistoryEntity>,
    onDailyCheckIn: () -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Sub-tabs
        TabRow(
            selectedTabIndex = currentSubTab.ordinal,
            containerColor = DarkNav,
            contentColor = BaseCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                    color = BaseCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = currentSubTab == QuestsSubTab.MISSIONS,
                onClick = { onSubTabSelected(QuestsSubTab.MISSIONS) },
                text = {
                    Text(
                        text = "🎯 Missions",
                        fontWeight = if (currentSubTab == QuestsSubTab.MISSIONS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("tab_missions")
            )
            Tab(
                selected = currentSubTab == QuestsSubTab.LEARNING,
                onClick = { onSubTabSelected(QuestsSubTab.LEARNING) },
                text = {
                    Text(
                        text = "📚 Learn",
                        fontWeight = if (currentSubTab == QuestsSubTab.LEARNING) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("tab_learning")
            )
            Tab(
                selected = currentSubTab == QuestsSubTab.LEADERBOARD,
                onClick = { onSubTabSelected(QuestsSubTab.LEADERBOARD) },
                text = {
                    Text(
                        text = "🏆 Ranks",
                        fontWeight = if (currentSubTab == QuestsSubTab.LEADERBOARD) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("tab_leaderboard")
            )
            Tab(
                selected = currentSubTab == QuestsSubTab.REWARDS,
                onClick = { onSubTabSelected(QuestsSubTab.REWARDS) },
                text = {
                    Text(
                        text = "🎁 Vault",
                        fontWeight = if (currentSubTab == QuestsSubTab.REWARDS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("tab_rewards")
            )
        }

        when (currentSubTab) {
            QuestsSubTab.MISSIONS -> {
                MissionsListContent(
                    quests = quests,
                    selectedCategory = selectedCategory,
                    onSelectCategory = onSelectCategory,
                    onClaimQuest = onClaimQuest,
                    userProfile = userProfile,
                    onNavigateToLearn = { onSubTabSelected(QuestsSubTab.LEARNING) },
                    onShowSnackbar = onShowSnackbar
                )
            }
            QuestsSubTab.LEARNING -> {
                LearningCenterScreen(
                    lessons = lessons,
                    completedLessonIdsCsv = completedLessonIdsCsv,
                    onSelectLesson = onSelectLesson
                )
            }
            QuestsSubTab.LEADERBOARD -> {
                LeaderboardScreen(
                    users = leaderboardUsers,
                    selectedTimeframe = selectedLeaderboardTimeframe,
                    onSelectTimeframe = onSelectLeaderboardTimeframe
                )
            }
            QuestsSubTab.REWARDS -> {
                RewardsCenterScreen(
                    userProfile = userProfile,
                    rewardsHistory = rewardsHistory,
                    onDailyCheckIn = onDailyCheckIn
                )
            }
        }
    }
}

@Composable
fun MissionsListContent(
    quests: List<QuestItem>,
    selectedCategory: QuestCategory?,
    onSelectCategory: (QuestCategory?) -> Unit,
    onClaimQuest: (String) -> Unit,
    userProfile: UserProfile? = null,
    onNavigateToLearn: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    val filteredQuests = if (selectedCategory == null) {
        quests
    } else {
        quests.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Visual Progress Bar & Badge Display Component for XP Accumulation and Upcoming Reward Milestones
            QuestsXpProgressBadgeComponent(
                userProfile = userProfile,
                onNavigateToLearn = onNavigateToLearn,
                onShowSnackbar = onShowSnackbar
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(
                text = "Super Agent Missions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Complete on-chain challenges to earn XP, level up, and unlock AGL tokens.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaseBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedCategory == QuestCategory.DAILY,
                    onClick = { onSelectCategory(QuestCategory.DAILY) },
                    label = { Text("Daily", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaseBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedCategory == QuestCategory.WEEKLY,
                    onClick = { onSelectCategory(QuestCategory.WEEKLY) },
                    label = { Text("Weekly", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaseBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedCategory == QuestCategory.SECURITY_CHALLENGE,
                    onClick = { onSelectCategory(QuestCategory.SECURITY_CHALLENGE) },
                    label = { Text("Security", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaseBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(filteredQuests) { quest ->
            QuestItemRow(
                quest = quest,
                onClaim = { onClaimQuest(quest.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
