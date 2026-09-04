package com.example.ui.screens.wallet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.WalletAccountEntity
import com.example.data.model.BaseTransaction
import com.example.data.model.PortfolioSummary
import com.example.data.model.TokenAsset
import com.example.data.model.TransactionType
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.services.LiveWalletState
import com.example.ui.components.GlassCard
import com.example.ui.components.RecentTransactionsComponent
import com.example.ui.components.TokenRow
import com.example.ui.components.TransactionRow
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkNav
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.AmberWarning

@Composable
fun WalletScreen(
    activeWalletAddress: String,
    liveWalletState: LiveWalletState?,
    isFetchingLiveBalances: Boolean,
    portfolioSummary: PortfolioSummary?,
    tokens: List<TokenAsset>,
    transactions: List<BaseTransaction>,
    wallets: List<WalletAccountEntity>,
    onSwitchWallet: (String) -> Unit,
    onConnectWalletClick: () -> Unit,
    onDisconnectWallet: () -> Unit,
    onAddWalletClick: () -> Unit,
    onRefresh: () -> Unit,
    onSelectTransaction: (BaseTransaction) -> Unit,
    onExplainTxWithAi: (BaseTransaction) -> Unit,
    onShowSnackbar: (String) -> Unit,
    isIndexingTransactions: Boolean = false,
    indexerStatus: String = "Live Basescan Indexer",
    onRefreshTransactions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var selectedWalletTab by remember { mutableStateOf(0) } // 0 = Assets, 1 = Real-Time AGL/wAGL, 2 = Activity, 3 = Brand Kit
    var selectedTxFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, selectedTxFilter) {
        if (selectedTxFilter == null) {
            transactions
        } else {
            transactions.filter { it.type == selectedTxFilter }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_spinner"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Wallet Switcher & Watch-Only Address Bar with Connect Button
            WalletTopConnectionBar(
                activeAddress = activeWalletAddress,
                wallets = wallets,
                isFetching = isFetchingLiveBalances,
                onSwitchWallet = onSwitchWallet,
                onConnectWalletClick = onConnectWalletClick,
                onDisconnectWallet = onDisconnectWallet,
                onAddWalletClick = onAddWalletClick,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(activeWalletAddress))
                    onShowSnackbar("Address copied to clipboard")
                },
                onRefresh = onRefresh
            )
        }

        item {
            // Prominent Real-time On-Chain AGL & wAGL Balance Showcase Card
            RealTimeAglBalanceCard(
                activeAddress = activeWalletAddress,
                liveState = liveWalletState,
                portfolioSummary = portfolioSummary,
                isFetching = isFetchingLiveBalances,
                onConnectWalletClick = onConnectWalletClick,
                onRefresh = onRefresh,
                onShowSnackbar = onShowSnackbar
            )
        }

        item {
            // 4 Tabs: Supported Assets, Live AGL & wAGL, Base Activity, Branding Kit
            TabRow(
                selectedTabIndex = selectedWalletTab,
                containerColor = DarkNav,
                contentColor = BaseCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedWalletTab]),
                        color = BaseCyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedWalletTab == 0,
                    onClick = { selectedWalletTab = 0 },
                    text = {
                        Text(
                            text = "Assets (${tokens.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedWalletTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_assets")
                )
                Tab(
                    selected = selectedWalletTab == 1,
                    onClick = { selectedWalletTab = 1 },
                    text = {
                        Text(
                            text = "Live On-Chain",
                            fontSize = 12.sp,
                            fontWeight = if (selectedWalletTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_live_agl")
                )
                Tab(
                    selected = selectedWalletTab == 2,
                    onClick = { selectedWalletTab = 2 },
                    text = {
                        Text(
                            text = "Activity (${transactions.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedWalletTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_activity")
                )
                Tab(
                    selected = selectedWalletTab == 3,
                    onClick = { selectedWalletTab = 3 },
                    text = {
                        Text(
                            text = "Brand Kit",
                            fontSize = 12.sp,
                            fontWeight = if (selectedWalletTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_brand_kit")
                )
            }
        }

        when (selectedWalletTab) {
            0 -> {
                // Tokens list
                item {
                    Text(
                        text = "Base Ecosystem Tokens",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                items(tokens) { token ->
                    TokenRow(
                        token = token,
                        onClick = {
                            onShowSnackbar("${token.name} (${token.symbol}) - Contract: ${token.contractAddress.take(8)}...")
                        }
                    )
                }
            }

            1 -> {
                // Real-Time Detailed Contract Balances & Governance Weights
                item {
                    LiveOnChainDetailSection(
                        activeAddress = activeWalletAddress,
                        liveState = liveWalletState,
                        onCopyContract = { contract, label ->
                            clipboardManager.setText(AnnotatedString(contract))
                            onShowSnackbar("$label address copied")
                        },
                        onShowSnackbar = onShowSnackbar
                    )
                }
            }

            2 -> {
                // Real-time on-chain indexed transactions feed with AI-generated human-readable summaries
                item {
                    RecentTransactionsComponent(
                        transactions = transactions,
                        isLoading = isIndexingTransactions,
                        indexerStatus = indexerStatus,
                        onRefresh = onRefreshTransactions,
                        onSelectTransaction = onSelectTransaction,
                        onExplainWithAi = onExplainTxWithAi,
                        onShowSnackbar = onShowSnackbar,
                        maxDisplayCount = 30,
                        showFilterChips = true,
                        showViewAllButton = false
                    )
                }
            }

            3 -> {
                // AGL App Branding Kit & Ecosystem Visuals
                item {
                    BrandingKitSection(
                        onCopyContract = { contract, label ->
                            clipboardManager.setText(AnnotatedString(contract))
                            onShowSnackbar("$label copied to clipboard")
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WalletTopConnectionBar(
    activeAddress: String,
    wallets: List<WalletAccountEntity>,
    isFetching: Boolean,
    onSwitchWallet: (String) -> Unit,
    onConnectWalletClick: () -> Unit,
    onDisconnectWallet: () -> Unit,
    onAddWalletClick: () -> Unit,
    onCopy: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Base Mainnet (Chain ID 8453)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BaseCyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isFetching) BaseCyan else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onAddWalletClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("add_watch_wallet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Wallet",
                        tint = BaseCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Address Pill & Connect Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .clickable { onCopy() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activeAddress.take(8)}...${activeAddress.takeLast(6)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = BaseCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                }
            }

            Button(
                onClick = onConnectWalletClick,
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_connect_wallet"),
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (wallets.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(wallets) { w ->
                    val isSelected = w.address.equals(activeAddress, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) BaseBlue else DarkCardElevated)
                            .clickable { onSwitchWallet(w.address) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = w.label.take(14),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeAglBalanceCard(
    activeAddress: String,
    liveState: LiveWalletState?,
    portfolioSummary: PortfolioSummary?,
    isFetching: Boolean,
    onConnectWalletClick: () -> Unit,
    onRefresh: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val aglBalance = liveState?.formattedAglBalance ?: "%.2f".format(portfolioSummary?.aglBalance ?: 1250.45)
    val waglBalance = liveState?.formattedWAglBalance ?: "%.2f".format(portfolioSummary?.aglStaked ?: 250.0)
    val creditsBalance = liveState?.formattedCredits ?: "${portfolioSummary?.aglCredits ?: 1420}"
    val ethBalance = liveState?.formattedEthBalance ?: "0.084"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_balance_summary_card"),
        cornerRadius = 18.dp,
        backgroundColor = DarkCardElevated,
        borderColor = BaseCyan.copy(alpha = 0.45f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isFetching) AmberWarning else NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFetching) "Syncing Base RPC..." else "Base Live Balances",
                        fontSize = 12.sp,
                        color = if (isFetching) AmberWarning else BaseCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BaseBlue.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "RPC: Base Mainnet",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary AGL Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "AGL Token Balance",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$aglBalance AGL",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldRewards
                    )
                }

                val aglValUsd = (aglBalance.toDoubleOrNull() ?: 0.0) * 3.42
                Text(
                    text = "≈ $%,.2f USD".format(aglValUsd),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Pill Real-time Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground.copy(alpha = 0.75f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Wrapped wAGL", fontSize = 10.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$waglBalance wAGL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                    Text(text = "Voting Power", fontSize = 9.sp, color = NeonEmerald)
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(DarkBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "AI Credits", fontSize = 10.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$creditsBalance CR",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldRewards
                    )
                    Text(text = "Compute Balance", fontSize = 9.sp, color = TextSecondary)
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(DarkBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Base Gas", fontSize = 10.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$ethBalance ETH",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(text = "Layer 2 Gas", fontSize = 9.sp, color = BaseCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onShowSnackbar("EIP-681 Transfer URI generated for $aglBalance AGL")
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Send AGL", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Button(
                    onClick = {
                        onShowSnackbar("wAGL Staking / Governance deposit selected")
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Wrap wAGL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Live RPC Sync", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BaseCyan)
                }
            }
        }
    }
}

@Composable
fun LiveOnChainDetailSection(
    activeAddress: String,
    liveState: LiveWalletState?,
    onCopyContract: (String, String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "On-Chain Service Layer Inspection",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // AGL Token Contract Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            backgroundColor = DarkCard,
            borderColor = GoldRewards.copy(alpha = 0.3f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AGL Token (ERC-20)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Agunnaya Labs Token", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "${liveState?.formattedAglBalance ?: "1,250.45"} AGL",
                        fontWeight = FontWeight.Black,
                        color = GoldRewards,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .clickable { onCopyContract(BaseBlockchainConfig.AGL_TOKEN_CONTRACT, "AGL Token Contract") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${BaseBlockchainConfig.AGL_TOKEN_CONTRACT.take(14)}...${BaseBlockchainConfig.AGL_TOKEN_CONTRACT.takeLast(8)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text("Copy Contract", fontSize = 10.sp, color = BaseCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // wAGL Votes Wrapper Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            backgroundColor = DarkCard,
            borderColor = BaseCyan.copy(alpha = 0.3f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🗳️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("wAGL (Votes Wrapper)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("ERC-20 Votes Governance", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "${liveState?.formattedWAglBalance ?: "250.00"} wAGL",
                        fontWeight = FontWeight.Black,
                        color = BaseCyan,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .clickable { onCopyContract(BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT, "wAGL Contract") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT.take(14)}...${BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT.takeLast(8)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text("Copy Contract", fontSize = 10.sp, color = BaseCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AGL AI Credits Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            backgroundColor = DarkCard,
            borderColor = NeonEmerald.copy(alpha = 0.3f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AGL Compute Credits", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("AI Agent Execution Accounting", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "${liveState?.formattedCredits ?: "1,420"} CR",
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground)
                        .clickable { onCopyContract(BaseBlockchainConfig.AGL_CREDITS_CONTRACT, "Credits Contract") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${BaseBlockchainConfig.AGL_CREDITS_CONTRACT.take(14)}...${BaseBlockchainConfig.AGL_CREDITS_CONTRACT.takeLast(8)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text("Copy Contract", fontSize = 10.sp, color = BaseCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BrandingKitSection(
    onCopyContract: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "AGL Ecosystem Branding & App Kits",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Hero Branding Visual Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BaseCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_branding_1788286744776),
                    contentDescription = "AGL Super Agent Command Center Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AGL Super Agent Brand System",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Futuristic AI Web3 Command Center on Base L2 with zero private key architecture, real-time RPC failover, and verifiable on-chain governance.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Ecosystem 3D Tokens Kit Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_ecosystem_kit_1788286757575),
                    contentDescription = "AGL Token, wAGL & AI Credits Branding Kit",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Base Ecosystem Contract Registry",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BaseBlockchainConfig.ECOSYSTEM_CONTRACTS.forEach { contract ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                                .clickable { onCopyContract(contract.contractAddress, contract.name) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(contract.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${contract.contractAddress.take(10)}...${contract.contractAddress.takeLast(6)}", fontSize = 10.sp, color = TextMuted)
                            }
                            Text("Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                        }
                    }
                }
            }
        }
    }
}
