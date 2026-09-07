package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AglEcosystemContract
import com.example.data.model.AglEcosystemStats
import com.example.data.model.AiSuggestion
import com.example.data.model.AiSuggestionCategory
import com.example.data.model.BaseTransaction
import com.example.data.model.PortfolioSummary
import com.example.data.model.QuestItem
import com.example.ui.components.AiSuggestionsSection
import com.example.ui.components.GlassCard
import com.example.ui.components.RecentTransactionsComponent
import com.example.ui.components.StatCard
import com.example.ui.components.TransactionRow
import com.example.ui.components.WalkthroughHelpModal
import com.example.ui.components.Web3PortfolioChart
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AiSubTab
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.QuestsSubTab

@Composable
fun HomeScreen(
    portfolioSummary: PortfolioSummary?,
    transactions: List<BaseTransaction>,
    quests: List<QuestItem>,
    ecosystemStats: AglEcosystemStats,
    ecosystemContracts: List<AglEcosystemContract>,
    onNavigate: (AppScreen) -> Unit,
    onNavigateAiTab: (AiSubTab) -> Unit,
    onNavigateQuestsTab: (QuestsSubTab) -> Unit,
    onSelectTransaction: (BaseTransaction) -> Unit,
    onClaimQuest: (String) -> Unit,
    onDailyCheckIn: () -> Unit,
    aiSuggestions: List<AiSuggestion> = emptyList(),
    selectedAiCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    isRefreshingSuggestions: Boolean = false,
    onSelectAiCategory: (AiSuggestionCategory) -> Unit = {},
    onRefreshSuggestions: () -> Unit = {},
    onApplySuggestion: (AiSuggestion) -> Unit = {},
    isIndexingTransactions: Boolean = false,
    indexerStatus: String = "Live Basescan Indexer",
    onRefreshTransactions: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showWalkthroughModal by remember { mutableStateOf(false) }

    if (showWalkthroughModal) {
        WalkthroughHelpModal(
            onDismiss = { showWalkthroughModal = false },
            onNavigate = { screen ->
                showWalkthroughModal = false
                onNavigate(screen)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // New User Walkthrough / Guide Banner
            NewUserWalkthroughBanner(
                onStartTour = { showWalkthroughModal = true }
            )
        }

        item {
            // Hero Portfolio Card
            HeroPortfolioSection(
                portfolioSummary = portfolioSummary,
                onDailyCheckIn = onDailyCheckIn,
                onViewWallet = { onNavigate(AppScreen.WALLET) }
            )
        }

        item {
            // AGL Oracle Price Alert Ticker Banner
            AglPriceAlertTickerBanner(
                price = ecosystemStats.currentPriceUsd,
                onClick = { onNavigate(AppScreen.PRICE_ALERTS) }
            )
        }

        item {
            // Web3 Command Quick Action Grid
            QuickActionsSection(
                onAskAi = {
                    onNavigateAiTab(AiSubTab.CHAT)
                    onNavigate(AppScreen.AI_ASSISTANT)
                },
                onScanContract = {
                    onNavigateAiTab(AiSubTab.CONTRACT_ANALYZER)
                    onNavigate(AppScreen.AI_ASSISTANT)
                },
                onSecurityAudit = {
                    onNavigateAiTab(AiSubTab.SECURITY_AUDIT)
                    onNavigate(AppScreen.AI_ASSISTANT)
                },
                onLearnWeb3 = {
                    onNavigateQuestsTab(QuestsSubTab.LEARNING)
                    onNavigate(AppScreen.QUESTS)
                },
                onClaimRewards = {
                    onNavigateQuestsTab(QuestsSubTab.REWARDS)
                    onNavigate(AppScreen.QUESTS)
                }
            )
        }

        item {
            // AI Suggestions & Real-Time Intelligence
            AiSuggestionsSection(
                suggestions = aiSuggestions,
                selectedCategory = selectedAiCategory,
                isRefreshing = isRefreshingSuggestions,
                onSelectCategory = onSelectAiCategory,
                onRefresh = onRefreshSuggestions,
                onApplySuggestion = onApplySuggestion
            )
        }

        item {
            // AGL Ecosystem Live Intelligence Banner
            AglEcosystemBanner(
                stats = ecosystemStats,
                contracts = ecosystemContracts,
                onExploreContract = { contract ->
                    when (contract.id) {
                        "agl_token" -> onNavigate(AppScreen.AGL_TOKEN)
                        "agl_credits" -> onNavigate(AppScreen.CREDITS)
                        "agl_votes" -> onNavigate(AppScreen.WAGL)
                        "agl_staking" -> onNavigate(AppScreen.STAKING)
                        "agl_governor" -> onNavigate(AppScreen.GOVERNANCE)
                        "agl_timelock" -> onNavigate(AppScreen.TIMELOCK)
                        else -> {
                            onNavigateAiTab(AiSubTab.CONTRACT_ANALYZER)
                            onNavigate(AppScreen.AI_ASSISTANT)
                        }
                    }
                },
                onOpenDiagnostics = {
                    onNavigate(AppScreen.DIAGNOSTICS)
                }
            )
        }

        item {
            // Daily Quests & Missions Tracker
            ActiveQuestsSection(
                quests = quests.take(3),
                onClaimQuest = onClaimQuest,
                onViewAllQuests = {
                    onNavigateQuestsTab(QuestsSubTab.MISSIONS)
                    onNavigate(AppScreen.QUESTS)
                }
            )
        }

        item {
            // Real-Time On-Chain Recent Transactions Feed with AI Summaries
            RecentTransactionsComponent(
                transactions = transactions,
                isLoading = isIndexingTransactions,
                indexerStatus = indexerStatus,
                onRefresh = onRefreshTransactions,
                onSelectTransaction = onSelectTransaction,
                onExplainWithAi = { tx ->
                    onSelectTransaction(tx)
                    onNavigateAiTab(AiSubTab.CHAT)
                    onNavigate(AppScreen.AI_ASSISTANT)
                },
                onShowSnackbar = onShowSnackbar,
                maxDisplayCount = 5,
                showFilterChips = true,
                showViewAllButton = true,
                onViewAllClick = { onNavigate(AppScreen.WALLET) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NewUserWalkthroughBanner(
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStartTour() }
            .testTag("walkthrough_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCardElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(BaseCyan.copy(alpha = 0.6f), RadiantPurple.copy(alpha = 0.6f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BaseCyan.copy(alpha = 0.15f))
                        .border(1.dp, BaseCyan.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BaseCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "New User Guide",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BaseCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "1-MIN TOUR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BaseCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Learn about AI Agent, Wallet Analytics & 30-Day Trends",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            Button(
                onClick = onStartTour,
                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("start_tour_button")
            ) {
                Text(
                    text = "Start Tour 🚀",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                )
            }
        }
    }
}

@Composable
fun HeroPortfolioSection(
    portfolioSummary: PortfolioSummary?,
    onDailyCheckIn: () -> Unit,
    onViewWallet: () -> Unit
) {
    val balance = portfolioSummary?.totalBalanceUsd ?: 6850.20
    val aglBalance = portfolioSummary?.aglBalance ?: 1250.45
    val stakedAgl = portfolioSummary?.aglStaked ?: 250.0

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_portfolio_card"),
        cornerRadius = 20.dp,
        backgroundColor = DarkCardElevated,
        borderColor = BaseBlue.copy(alpha = 0.4f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Portfolio Balance",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$%,.2f".format(balance),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                // 24h PnL badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonEmerald.copy(alpha = 0.15f))
                        .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+6.42% (24h)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Sparkline
            Web3PortfolioChart(
                points = listOf(6100f, 6250f, 6180f, 6380f, 6420f, 6720f, 6850.2f),
                lineColor = BaseCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AGL Token & Staking Micro-strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground.copy(alpha = 0.7f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AGL Holdings",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "%.2f AGL".format(aglBalance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldRewards
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(DarkBorder)
                )

                Column {
                    Text(
                        text = "Staked (18.5% APR)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "%.2f AGL".format(stakedAgl),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                }

                Button(
                    onClick = onDailyCheckIn,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("checkin_bonus_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Text(
                        text = "⚡ Check-In",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onAskAi: () -> Unit,
    onScanContract: () -> Unit,
    onSecurityAudit: () -> Unit,
    onLearnWeb3: () -> Unit,
    onClaimRewards: () -> Unit
) {
    Column {
        Text(
            text = "Web3 Command Center",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionItem(
                title = "AI Assistant",
                icon = Icons.Default.AutoAwesome,
                tint = BaseCyan,
                modifier = Modifier.weight(1f),
                onClick = onAskAi,
                testTag = "action_ask_ai"
            )
            QuickActionItem(
                title = "Scan Contract",
                icon = Icons.Default.QrCodeScanner,
                tint = BaseBlue,
                modifier = Modifier.weight(1f),
                onClick = onScanContract,
                testTag = "action_scan_contract"
            )
            QuickActionItem(
                title = "Security Audit",
                icon = Icons.Default.Shield,
                tint = NeonEmerald,
                modifier = Modifier.weight(1f),
                onClick = onSecurityAudit,
                testTag = "action_security_audit"
            )
            QuickActionItem(
                title = "Learn & Earn",
                icon = Icons.Default.EmojiEvents,
                tint = GoldRewards,
                modifier = Modifier.weight(1f),
                onClick = onLearnWeb3,
                testTag = "action_learn_web3"
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun AglEcosystemBanner(
    stats: AglEcosystemStats,
    contracts: List<AglEcosystemContract>,
    onExploreContract: (AglEcosystemContract) -> Unit,
    onOpenDiagnostics: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AGL Base Ecosystem",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BaseBlue.copy(alpha = 0.25f))
                        .border(1.dp, BaseCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable { onOpenDiagnostics() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚡ Diagnostics",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chain 8453",
                    fontSize = 11.sp,
                    color = NeonEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "AGL Token Price",
                value = "$${stats.currentPriceUsd}",
                subtitle = "+8.65% (Base)",
                isPositive = true,
                iconEmoji = "🪙",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "AI Compute Staking",
                value = "${stats.stakingAprPercent}% APR",
                subtitle = stats.totalStakedAgl,
                isPositive = true,
                iconEmoji = "🔒",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Verified Ecosystem Contracts Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contracts) { contract ->
                ContractChip(
                    contract = contract,
                    onClick = { onExploreContract(contract) }
                )
            }
        }
    }
}

@Composable
fun ContractChip(
    contract: AglEcosystemContract,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCardElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = contract.iconEmoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contract.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = NeonEmerald,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = "${contract.contractAddress.take(6)}...${contract.contractAddress.takeLast(4)}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ActiveQuestsSection(
    quests: List<QuestItem>,
    onClaimQuest: (String) -> Unit,
    onViewAllQuests: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Missions & Quests",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "View All →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BaseCyan,
                modifier = Modifier
                    .clickable { onViewAllQuests() }
                    .testTag("view_all_quests_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quests.forEach { quest ->
                QuestItemRow(
                    quest = quest,
                    onClaim = { onClaimQuest(quest.id) }
                )
            }
        }
    }
}

@Composable
fun QuestItemRow(
    quest: QuestItem,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, if (quest.isCompleted && !quest.isClaimed) GoldRewards.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = quest.iconEmoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { quest.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (quest.isCompleted) NeonEmerald else BaseCyan,
                    trackColor = DarkBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+${quest.xpReward} XP  •  +${quest.aglReward} AGL",
                    fontSize = 11.sp,
                    color = GoldRewards,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (quest.isClaimed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Claimed ✓",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (quest.isCompleted) {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = GoldRewards),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("claim_quest_${quest.id}")
            ) {
                Text(
                    text = "Claim",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                )
            }
        } else {
            Text(
                text = "${quest.currentProgress}/${quest.maxProgress}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<BaseTransaction>,
    onSelectTransaction: (BaseTransaction) -> Unit,
    onViewAll: () -> Unit,
    onExplainWithAi: (BaseTransaction) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Base Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "All Activity →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BaseCyan,
                modifier = Modifier
                    .clickable { onViewAll() }
                    .testTag("view_all_transactions_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent transactions found on Base.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { tx ->
                    TransactionRow(
                        transaction = tx,
                        onClick = { onSelectTransaction(tx) }
                    )
                }
            }
        }
    }
}

@Composable
fun AglPriceAlertTickerBanner(
    price: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("agl_price_alert_ticker_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(BaseCyan.copy(alpha = 0.5f), DarkBorder))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = DarkCardElevated,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BaseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AGL Oracle: $${"%.3f".format(price)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                    }
                    Text(
                        text = "Chainlink Feed • Tap to set price thresholds",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = DarkBorderSubtle,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alerts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = BaseCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
