package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
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

data class TutorialStep(
    val id: Int,
    val title: String,
    val category: String,
    val duration: String,
    val iconEmoji: String,
    val summary: String,
    val concepts: List<Pair<String, String>>,
    val protocolSpec: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

private val TUTORIAL_STEPS = listOf(
    TutorialStep(
        id = 1,
        title = "Connecting to Base Mainnet",
        category = "Network Basics",
        duration = "2 min",
        iconEmoji = "🔵",
        summary = "Base is an Ethereum Layer 2 (L2) developed on the OP Stack. To interact with decentralized apps (dApps), your wallet communicates with Base sequencers via RPC endpoints.",
        concepts = listOf(
            "Chain ID 8453" to "Identifies Base Mainnet uniquely across all EVM blockchains to prevent transaction replay.",
            "Native ETH for Gas" to "Base uses native ETH to pay transaction execution gas, maintaining direct Ethereum standard compatibility.",
            "Non-Custodial Architecture" to "Your private keys remain solely on your device. Protocols only inspect your public cryptographic address."
        ),
        protocolSpec = "RPC: https://mainnet.base.org | Chain ID: 8453 | Currency: ETH",
        quizQuestion = "What native currency pays for transaction gas fees on Base?",
        quizOptions = listOf("USDC", "BaseCoin", "ETH", "AGL"),
        correctOptionIndex = 2,
        explanation = "Base is an Ethereum Layer 2 and uses native ETH for all transaction and computation gas fees."
    ),
    TutorialStep(
        id = 2,
        title = "Gas Fees & Optimistic Rollups",
        category = "Execution & Cost",
        duration = "3 min",
        iconEmoji = "⚡",
        summary = "Understand how Base achieves sub-cent transaction fees through Optimistic Rollups and Ethereum EIP-4844 data blobs.",
        concepts = listOf(
            "Off-Chain Execution" to "Transactions execute on L2 at sub-second speeds, compressing state changes before publishing to Ethereum L1.",
            "EIP-4844 Blobs" to "Temporary data blobs significantly lower the cost of posting transaction batches to Ethereum Mainnet.",
            "Two-Part Fee Model" to "Base fees consist of L2 execution gas plus a tiny L1 data availability fee (usually <$0.01 total)."
        ),
        protocolSpec = "Typical Swap Fee: Ethereum L1 ~$4.50 vs Base L2 ~$0.008",
        quizQuestion = "How does Base achieve gas fees that are a fraction of a cent?",
        quizOptions = listOf(
            "By removing cryptography entirely",
            "By bundling compressed transactions into EIP-4844 blobs posted to L1",
            "By charging a monthly flat subscription",
            "By running on centralized bank servers"
        ),
        correctOptionIndex = 1,
        explanation = "Base executes transactions off-chain and bundles thousands into compressed EIP-4844 blobs posted to Ethereum."
    ),
    TutorialStep(
        id = 3,
        title = "Token Approvals & Smart Allowances",
        category = "DeFi Security",
        duration = "4 min",
        iconEmoji = "🔐",
        summary = "Before any decentralized protocol can transfer or stake your ERC-20 tokens, you must grant an approval. Managing allowances is vital to securing your capital.",
        concepts = listOf(
            "transfer() vs approve()" to "transfer moves your tokens directly; approve delegates a maximum spend limit to a smart contract (e.g. DEX router).",
            "Infinite Allowance Risk" to "Approving max uint256 gives a contract permanent access. If that contract is exploited later, tokens can be drained.",
            "Exact Allowances" to "Best practice is to approve only the exact token amount required for your immediate transaction."
        ),
        protocolSpec = "AGL Token Contract: 0xEA1221B4d80A89BD8C75248Fae7c176BD1854698",
        quizQuestion = "Why can granting 'unlimited / infinite' token approval be dangerous?",
        quizOptions = listOf(
            "It triples the gas fee",
            "Your transaction will be delayed by 24 hours",
            "If that contract has a future vulnerability, an attacker could drain your approved tokens",
            "It turns your wallet into a read-only account"
        ),
        correctOptionIndex = 2,
        explanation = "An infinite approval gives the contract indefinite access to pull tokens. Any future exploit in the protocol puts your funds at risk."
    ),
    TutorialStep(
        id = 4,
        title = "DEXs & Automated Market Makers",
        category = "Protocols",
        duration = "4 min",
        iconEmoji = "🔄",
        summary = "Discover how Automated Market Makers like Aerodrome execute decentralized swaps on Base using liquidity pools rather than centralized order books.",
        concepts = listOf(
            "Constant Product Pools (x * y = k)" to "Pools maintain paired asset balances. Trades alter the reserve ratio, automatically recalculating the execution price.",
            "Slippage Tolerance" to "Sets the maximum acceptable price deviation between quotation and execution to protect against front-running MEV bots.",
            "Aerodrome on Base" to "Aerodrome is Base's central liquidity hub, featuring low-slippage trading and concentrated liquidity pools."
        ),
        protocolSpec = "Aerodrome Router on Base: 0xcF664087a5bB0237a0BAd6742852ec6c8d58504e",
        quizQuestion = "What does setting a 0.5% slippage tolerance accomplish?",
        quizOptions = listOf(
            "Guarantees a 0.5% discount on the swap",
            "Automatically cancels/reverts the swap if the executed price moves worse by more than 0.5%",
            "Locks your tokens for 5 days",
            "Sends 0.5% to charity"
        ),
        correctOptionIndex = 1,
        explanation = "Slippage tolerance protects swappers: if market depth changes unfavorably by more than 0.5%, the transaction safely reverts."
    ),
    TutorialStep(
        id = 5,
        title = "Liquidity Provision & Staking",
        category = "DeFi Yield",
        duration = "4 min",
        iconEmoji = "📈",
        summary = "Learn how Liquidity Providers (LPs) earn swap fees, and how staking tokens like AGL generates governance power (wAGL) and protocol yields on Base.",
        concepts = listOf(
            "LP Tokens" to "Depositing paired tokens yields LP tokens representing your proportional ownership of the liquidity pool.",
            "Fee APR & Rewards" to "Every trade across the pool yields a protocol swap fee distributed proportionally to active LPs.",
            "Impermanent Loss" to "Occurs when paired token prices diverge after deposit, making individual holding temporarily more profitable than the pool."
        ),
        protocolSpec = "wAGL Governance Wrapper: 0x356AbeDE92d53D9Fe5165d21A2eEB6c321CEa7b4",
        quizQuestion = "How do Liquidity Providers (LPs) earn yield in decentralized pools?",
        quizOptions = listOf(
            "From central bank bailouts",
            "By collecting a proportional share of trading fees generated by users swapping through the pool",
            "By mining physical Bitcoin",
            "Through government grants"
        ),
        correctOptionIndex = 1,
        explanation = "LPs earn trading fees paid by users who swap through the pool, supplemented by protocol liquidity incentives."
    ),
    TutorialStep(
        id = 6,
        title = "Security Hygiene & AI Contract Audits",
        category = "Safety Masterclass",
        duration = "3 min",
        iconEmoji = "🛡️",
        summary = "Master essential security habits on Base: verifying contract source code, spotting honeypots, and leveraging the AGL AI Agent for pre-flight audits.",
        concepts = listOf(
            "Basescan Verification" to "Ensure contracts are verified and open-source on Basescan to inspect logic before approving transactions.",
            "Counterfeit Token Detection" to "Anyone can deploy a token with identical symbols. Always verify the hexadecimal contract address against official sources.",
            "Pre-Flight AI Audits" to "Run target contracts through AGL's AI security analyzer to catch reentrancy, privileged owner backdoors, and hidden fee traps."
        ),
        protocolSpec = "AGL Super Agent AI Auditor: Real-time static bytecode & heuristic analysis.",
        quizQuestion = "What is the most reliable way to ensure a token on Base is legitimate?",
        quizOptions = listOf(
            "Check if the token name looks familiar on social media",
            "Verify the exact contract hexadecimal address against official documentation or verified registries",
            "Assume all tokens with the same symbol are authentic",
            "Ask a stranger in a chat room"
        ),
        correctOptionIndex = 1,
        explanation = "Because token symbols can be freely duplicated by scammers, verifying the exact contract address is the only foolproof verification method."
    )
)

@Composable
fun Web3LearningContent(
    onJumpToAudit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val completedSteps = remember { mutableStateListOf<Int>() }

    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember { mutableStateOf(false) }

    // Sandbox interactive states
    var simNetConnected by remember { mutableStateOf(false) }
    var simEthAmount by remember { mutableStateOf("0.05") }
    var simSlippage by remember { mutableStateOf("0.5") }
    var simApprovalExact by remember { mutableStateOf(true) }
    var simApprovalDone by remember { mutableStateOf(false) }
    var simSwapDone by remember { mutableStateOf(false) }
    var simScanning by remember { mutableStateOf(false) }
    var simScanResult by remember { mutableStateOf<String?>(null) }

    val step = TUTORIAL_STEPS[currentStepIndex]
    val isStepCompleted = completedSteps.contains(step.id)
    val progressPercent = (completedSteps.size.toFloat() / TUTORIAL_STEPS.size.toFloat())
    val totalXpEarned = completedSteps.size * 50

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("learning_hero_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(BaseBlue.copy(alpha = 0.5f), BaseCyan.copy(alpha = 0.5f)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎓", fontSize = 22.sp)
                            Text(
                                text = "Web3 Learning Academy",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Interactive step-by-step masterclass for interacting with decentralized protocols on Base.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldRewards.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GoldRewards.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "⚡ $totalXpEarned / 300 XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldRewards,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress: ${completedSteps.size} of ${TUTORIAL_STEPS.size} Completed",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BaseCyan,
                        trackColor = DarkBackground
                    )
                }

                // Step Selector Pills (Horizontally Scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TUTORIAL_STEPS.forEachIndexed { idx, s ->
                        val isCurrent = idx == currentStepIndex
                        val isDone = completedSteps.contains(s.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isCurrent -> BaseBlue
                                isDone -> NeonEmerald.copy(alpha = 0.15f)
                                else -> DarkBackground
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCurrent -> BaseCyan
                                    isDone -> NeonEmerald.copy(alpha = 0.3f)
                                    else -> DarkBorder
                                }
                            ),
                            modifier = Modifier
                                .clickable {
                                    currentStepIndex = idx
                                    selectedOptionIndex = null
                                    quizSubmitted = false
                                }
                                .testTag("step_pill_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDone) "✓" else "${s.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else if (isDone) NeonEmerald else TextSecondary
                                )
                                Text(
                                    text = s.title.split(" ").firstOrNull() ?: "",
                                    fontSize = 11.sp,
                                    color = if (isCurrent) Color.White else if (isDone) NeonEmerald else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Tutorial Step Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_step_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(
                1.dp,
                if (isStepCompleted) NeonEmerald.copy(alpha = 0.4f) else DarkBorder
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step Title & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = step.iconEmoji, fontSize = 24.sp)
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "STEP ${step.id} • ${step.category.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan
                                )
                                if (isStepCompleted) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NeonEmerald.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "COMPLETED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeonEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = step.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RadiantPurple.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+50 XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RadiantPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Summary Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkBackground,
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Text(
                        text = step.summary,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Key Concepts List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "KEY ARCHITECTURAL CONCEPTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    step.concepts.forEachIndexed { cIdx, (conceptTitle, conceptDetail) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkCardElevated,
                            border = BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${cIdx + 1}. $conceptTitle",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan
                                )
                                Text(
                                    text = conceptDetail,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Protocol Spec Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BaseBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Base Reference: ${step.protocolSpec}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BaseCyan,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Interactive Protocol Sandbox
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_sandbox_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    border = BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🛠️", fontSize = 16.sp)
                                Text(
                                    text = "Interactive Protocol Sandbox",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan
                                )
                            }
                            Text(
                                text = "Base Mainnet",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        when (step.id) {
                            1 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Simulate your wallet switching to Base parameters:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkBackground,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Target Network:", fontSize = 11.sp, color = TextMuted)
                                            Text(text = "Base Mainnet (8453)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                        }
                                    }
                                    Button(
                                        onClick = { simNetConnected = !simNetConnected },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (simNetConnected) NeonEmerald else BaseBlue
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (simNetConnected) "🟢 Connected to Base Sequencer (Click to Reset)" else "Simulate Switch to Base Mainnet →",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Live Execution Fee Comparison (150k gas swap):",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkBackground,
                                            border = BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = "Ethereum L1", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "~$4.50", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DangerCrimson)
                                                Text(text = "High Calldata", fontSize = 10.sp, color = TextMuted)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkBackground,
                                            border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = "Base L2 (Blobs)", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "~$0.008", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                                Text(text = "99.8% Savings", fontSize = 10.sp, color = NeonEmerald)
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select token approval policy for Aerodrome router:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { simApprovalExact = true; simApprovalDone = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (simApprovalExact) NeonEmerald else DarkBorder)
                                        ) {
                                            Text(
                                                text = "🛡️ Exact (100 AGL)",
                                                fontSize = 11.sp,
                                                color = if (simApprovalExact) NeonEmerald else TextSecondary
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { simApprovalExact = false; simApprovalDone = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (!simApprovalExact) DangerCrimson else DarkBorder)
                                        ) {
                                            Text(
                                                text = "⚠️ Infinite (2^256-1)",
                                                fontSize = 11.sp,
                                                color = if (!simApprovalExact) DangerCrimson else TextSecondary
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (simApprovalExact) NeonEmerald.copy(alpha = 0.1f) else DangerCrimson.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (simApprovalExact)
                                                "✓ Safe Policy: Contract can only spend 100 AGL. Remaining tokens are shielded from exploit risks."
                                            else
                                                "⚠️ Risky Policy: Contract can pull any future AGL balance in your wallet without prompt.",
                                            fontSize = 11.sp,
                                            color = if (simApprovalExact) NeonEmerald else DangerCrimson,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            simApprovalDone = true
                                            if (!completedSteps.contains(step.id)) completedSteps.add(step.id)
                                        },
                                        enabled = !simApprovalDone,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (simApprovalDone) "✓ Allowance Approved (Simulated)" else "Simulate approve(spender, amount)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            4 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Simulate DEX AMM Swap on Base Aerodrome:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkBackground,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = "Pay: 0.05 ETH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text(text = "Est. Gas: $0.007 ETH", fontSize = 10.sp, color = NeonEmerald)
                                            }
                                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = BaseCyan)
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "Receive: ~49.27 AGL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                                Text(text = "Slippage: 0.5%", fontSize = 10.sp, color = TextMuted)
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { simSwapDone = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (simSwapDone) "✓ Swap Executed on Base Block #1849204" else "Execute Simulated Swap →",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkBackground
                                        )
                                    }
                                }
                            }
                            5 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Yield Vault & Staking APR Calculator:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkBackground
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(text = "AGL Staking APR", fontSize = 10.sp, color = TextMuted)
                                                Text(text = "18.4% APR", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldRewards)
                                                Text(text = "Auto-compounding", fontSize = 9.sp, color = TextMuted)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkBackground
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(text = "wAGL Voting", fontSize = 10.sp, color = TextMuted)
                                                Text(text = "1 : 1 Power", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RadiantPurple)
                                                Text(text = "DAO Governance", fontSize = 9.sp, color = TextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                            6 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Simulate AI Pre-Flight Security Audit:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkBackground,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698 (AGL Token)",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = BaseCyan,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            simScanning = true
                                            simScanResult = null
                                            simScanning = false
                                            simScanResult = "✅ Verified Basescan Source • No Reentrancy Flaws • Safe Allowance Policy"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "Run AI Security Pre-Flight Scan 🛡️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    simScanResult?.let { res ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = NeonEmerald.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = res, fontSize = 11.sp, color = NeonEmerald, modifier = Modifier.padding(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Knowledge Check Quiz
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_quiz_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🎯", fontSize = 16.sp)
                            Text(
                                text = "Step ${step.id} Knowledge Check",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = step.quizQuestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        step.quizOptions.forEachIndexed { optIdx, optText ->
                            val isSelected = selectedOptionIndex == optIdx
                            val isCorrect = optIdx == step.correctOptionIndex

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    quizSubmitted && isCorrect -> NeonEmerald.copy(alpha = 0.18f)
                                    quizSubmitted && isSelected && !isCorrect -> DangerCrimson.copy(alpha = 0.18f)
                                    isSelected -> BaseBlue.copy(alpha = 0.3f)
                                    else -> DarkCard
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        quizSubmitted && isCorrect -> NeonEmerald
                                        quizSubmitted && isSelected && !isCorrect -> DangerCrimson
                                        isSelected -> BaseCyan
                                        else -> DarkBorder
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOptionIndex = optIdx
                                        quizSubmitted = true
                                        if (isCorrect && !completedSteps.contains(step.id)) {
                                            completedSteps.add(step.id)
                                        }
                                    }
                                    .testTag("quiz_opt_$optIdx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(DarkBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + optIdx).toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = optText,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (quizSubmitted && isCorrect) {
                                        Text(text = "✅", fontSize = 12.sp)
                                    } else if (quizSubmitted && isSelected && !isCorrect) {
                                        Text(text = "❌", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (quizSubmitted) {
                            val correct = selectedOptionIndex == step.correctOptionIndex
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (correct) NeonEmerald.copy(alpha = 0.1f) else AmberWarning.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, if (correct) NeonEmerald.copy(alpha = 0.3f) else AmberWarning.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = if (correct) "🎉 Correct! (+50 XP)" else "💡 Hint & Explanation:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (correct) NeonEmerald else AmberWarning
                                    )
                                    Text(
                                        text = step.explanation,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Step Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex -= 1
                                selectedOptionIndex = null
                                quizSubmitted = false
                            }
                        },
                        enabled = currentStepIndex > 0,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "← Previous", fontSize = 12.sp)
                    }

                    if (currentStepIndex < TUTORIAL_STEPS.size - 1) {
                        Button(
                            onClick = {
                                currentStepIndex += 1
                                selectedOptionIndex = null
                                quizSubmitted = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                        ) {
                            Text(text = "Next Step (${currentStepIndex + 2}/${TUTORIAL_STEPS.size}) →", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onJumpToAudit,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                        ) {
                            Text(
                                text = "🛡️ Audit Real Contracts →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBackground
                            )
                        }
                    }
                }
            }
        }

        // Completion Trophy Box
        if (completedSteps.size == TUTORIAL_STEPS.size) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("completion_trophy_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, NeonEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🏆", fontSize = 36.sp)
                    Text(
                        text = "Base Web3 Academy Certified!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                    Text(
                        text = "You have completed all 6 interactive modules. You now understand Base network parameters, EIP-4844 gas advantages, safe ERC-20 allowances, AMM liquidity swaps, and AI contract auditing.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Button(
                        onClick = onJumpToAudit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "Launch AI Security Auditor 🛡️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBackground
                        )
                    }
                }
            }
        }
    }
}
