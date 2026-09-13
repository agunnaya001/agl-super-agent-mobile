package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.viewmodel.AppScreen

data class WalkthroughStep(
    val id: String,
    val badge: String,
    val title: String,
    val headline: String,
    val icon: ImageVector,
    val iconColor: Color,
    val description: String,
    val bullets: List<String>,
    val targetScreen: AppScreen? = null,
    val targetScreenLabel: String = ""
)

@Composable
fun WalkthroughHelpModal(
    onDismiss: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            WalkthroughStep(
                id = "welcome",
                badge = "Welcome to AGL Super Agent",
                title = "Your Base Mainnet Co-Pilot",
                headline = "Autonomous intelligence for decentralized finance",
                icon = Icons.Default.AutoAwesome,
                iconColor = BaseCyan,
                description = "AGL Super Agent combines Gemini AI reasoning with real-time Base blockchain execution. The agent protects your assets, maximizes staking yields, and simplifies on-chain actions.",
                bullets = listOf(
                    "Non-custodial smart portfolio tracking on Base (Chain 8453)",
                    "Multi-RPC failover with real-time node diagnostics",
                    "Automated yield vaults and token-bound agent accounts"
                )
            ),
            WalkthroughStep(
                id = "ai_agent",
                badge = "Key Feature: AI Agent",
                title = "Security Audits & Smart Assistant",
                headline = "Autonomous vigilance powered by Gemini",
                icon = Icons.Default.Security,
                iconColor = RadiantPurple,
                description = "The AI Agent inspects verified smart contracts on Base for security vulnerabilities, evaluates reentrancy risks, and provides tailored rebalancing advice.",
                bullets = listOf(
                    "Contract Auditor: Instant security assessment and honeypot detection",
                    "Portfolio Radar: Automated suggestions for yield optimization and gas savings",
                    "Web3 Chat Assistant: Conversational explanations for any token, protocol, or transaction"
                ),
                targetScreen = AppScreen.AI_ASSISTANT,
                targetScreenLabel = "Open AI Assistant"
            ),
            WalkthroughStep(
                id = "wallet_analytics",
                badge = "Key Feature: Wallet Analytics",
                title = "30-Day Balance Trends & Telemetry",
                headline = "Visual trajectory and on-chain holdings",
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = NeonEmerald,
                description = "Monitor your wallet net worth with visual 30-day balance trend lines, comprehensive token allocations (AGL, ETH, USDC), and live transaction indexing.",
                bullets = listOf(
                    "Visual 30-Day Trend Line: Interactive trajectory of your balance over time",
                    "Multi-Asset Breakdown: Distinct views for liquid AGL, staked wAGL, and vault positions",
                    "Real-Time Indexer: Live transaction status tracking directly from Base Mainnet"
                ),
                targetScreen = AppScreen.WALLET,
                targetScreenLabel = "View Wallet"
            ),
            WalkthroughStep(
                id = "dashboard_monitor",
                badge = "Key Feature: Monitor & Vaults",
                title = "Node Latency & Aerodrome Yield",
                headline = "Full ecosystem telemetry and ERC-6551 accounts",
                icon = Icons.Default.Dashboard,
                iconColor = BaseCyan,
                description = "Benchmark live Base node latencies, monitor block production in real time, and deploy capital into Aerodrome concentrated liquidity yield vaults.",
                bullets = listOf(
                    "Network Diagnostics: Live ping and latency checks across Base RPC endpoints",
                    "Aerodrome Vaults: Automated yield optimization with auto-compounding",
                    "ERC-6551 Agent Accounts: Smart accounts governed by your agent"
                ),
                targetScreen = AppScreen.DASHBOARD,
                targetScreenLabel = "Open Dashboard"
            ),
            WalkthroughStep(
                id = "quests_rewards",
                badge = "Key Feature: Quests & Staking",
                title = "Daily Missions & Governance",
                headline = "Level up, earn XP, and shape the protocol",
                icon = Icons.Default.EmojiEvents,
                iconColor = GoldRewards,
                description = "Complete daily quests, check in to claim rewards, stake AGL for APR yields, and vote on decentralized governance proposals.",
                bullets = listOf(
                    "Daily Check-In: Build your streak and claim gas credits",
                    "Staking APR: Lock AGL into wAGL to earn passive rewards",
                    "On-Chain Governance: Submit and vote on AGL improvement proposals"
                ),
                targetScreen = AppScreen.QUESTS,
                targetScreenLabel = "Explore Quests"
            )
        )
    }

    val currentStep = steps[currentStepIndex]
    val isLast = currentStepIndex == steps.size - 1

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .border(1.dp, BaseCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("walkthrough_help_modal"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Bar: Step indicators and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress Bar Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            steps.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                idx == currentStepIndex -> BaseCyan
                                                idx < currentStepIndex -> NeonEmerald
                                                else -> DarkBorderSubtle
                                            }
                                        )
                                        .clickable { currentStepIndex = idx }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_walkthrough_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Walkthrough",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Counter & Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BaseCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = currentStep.badge,
                                color = BaseCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Step ${currentStepIndex + 1} of ${steps.size}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Step Content
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        },
                        label = "WalkthroughStepTransition"
                    ) { step ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Icon + Title Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(step.iconColor.copy(alpha = 0.15f))
                                        .border(1.dp, step.iconColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = null,
                                        tint = step.iconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = step.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = step.headline,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonEmerald
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = step.description,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bullet Highlights Box
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = DarkBackground.copy(alpha = 0.7f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "KEY CAPABILITIES:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BaseCyan,
                                        letterSpacing = 0.5.sp
                                    )

                                    step.bullets.forEach { bullet ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = NeonEmerald,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(top = 2.dp)
                                            )
                                            Text(
                                                text = bullet,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text("Skip Tour", fontSize = 12.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (currentStep.targetScreen != null) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onNavigate(currentStep.targetScreen)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RadiantPurple),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(currentStep.targetScreenLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isLast) {
                                        onDismiss()
                                    } else {
                                        currentStepIndex++
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isLast) "Finish ✨" else "Next",
                                    color = DarkBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (!isLast) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = DarkBackground,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
