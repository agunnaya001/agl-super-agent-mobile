package com.example.ui.screens.agl

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.charts.TokenBalancePieChart
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.services.LiveWalletState
import com.example.data.remote.blockchain.services.TokenMetadata
import com.example.data.remote.blockchain.tx.TxPipelineRequest
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigInteger

import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.remote.blockchain.services.DexAggregatorService
import com.example.data.remote.blockchain.services.DexQuote
import com.example.data.remote.blockchain.services.SwapToken
import com.example.ui.components.TokenLogoComponent
import kotlinx.coroutines.launch

@Composable
fun AglTokenScreen(
    metadata: TokenMetadata?,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onOpenPriceAlerts: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Swap, 1: Transfer, 2: Approve, 3: Burn, 4: Allocation

    // Form states
    var transferRecipient by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }

    var approveSpender by remember { mutableStateOf(BaseBlockchainConfig.AGL_CREDITS_CONTRACT) }
    var approveAmount by remember { mutableStateOf("100") }

    var burnAmount by remember { mutableStateOf("") }

    val aglBalance = walletState?.formattedAglBalance ?: "0.00"
    val tokenName = metadata?.name ?: "Agunnaya Labs"
    val tokenSymbol = metadata?.symbol ?: "AGL"
    val decimals = metadata?.decimals ?: 18
    val totalSupply = metadata?.formattedTotalSupply ?: "1,000,000,000"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("agl_token_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AGL Token Core",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clickable { onOpenPriceAlerts() }
                        .testTag("agl_token_open_price_alerts")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BaseCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Price Alerts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(BaseBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🪙", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tokenName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$tokenSymbol • $decimals Decimals • ERC-20",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Base 8453", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Wallet Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$aglBalance AGL",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BaseCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Supply", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$totalSupply AGL",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contract address row & Quick Swap Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = BaseBlockchainConfig.AGL_TOKEN_CONTRACT.take(12) + "..." + BaseBlockchainConfig.AGL_TOKEN_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { selectedTab = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("header_quick_swap_btn")
                            ) {
                                Text("⚡ DEX Swap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.AGL_TOKEN_CONTRACT))
                                    onShowSnackbar("AGL Address copied to clipboard")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerTokenUrl(BaseBlockchainConfig.AGL_TOKEN_CONTRACT)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.NorthEast, contentDescription = "Basescan", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Tabs: Swap, Transfer, Approve, Burn, Allocation
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCard,
                contentColor = BaseCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BaseCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Swap", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Transfer", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Burn", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Distribution", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab content
        when (selectedTab) {
            0 -> {
                // DEX Swap Tab
                item {
                    AglDexSwapTabContent(
                        walletState = walletState,
                        onStartTx = onStartTx,
                        onShowSnackbar = onShowSnackbar
                    )
                }
            }

            1 -> {
                // Transfer Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Recipient Base Address", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = transferRecipient,
                                onValueChange = { transferRecipient = it },
                                placeholder = { Text("0x...", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_recipient_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Amount (AGL)", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = transferAmount,
                                onValueChange = { transferAmount = it },
                                placeholder = { Text("0.0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transfer_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val amountWei = EvmCoder.parseUnits(transferAmount, 18)
                                    if (transferRecipient.isBlank() || amountWei <= BigInteger.ZERO) {
                                        onShowSnackbar("Please specify a valid recipient and amount")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildAglTransfer(transferRecipient, amountWei)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("send_transfer_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Transfer AGL Tokens", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Approve Spender Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Target Spender Contract", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = approveSpender,
                                onValueChange = { approveSpender = it },
                                placeholder = { Text("0x...", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("approve_spender_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldRewards,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { approveSpender = BaseBlockchainConfig.AGL_CREDITS_CONTRACT },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Credits", fontSize = 11.sp, color = BaseCyan)
                                }
                                OutlinedButton(
                                    onClick = { approveSpender = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("wAGL", fontSize = 11.sp, color = BaseCyan)
                                }
                                OutlinedButton(
                                    onClick = { approveSpender = BaseBlockchainConfig.STAKING_CONTRACT },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Staking", fontSize = 11.sp, color = BaseCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Allowance Amount (AGL)", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = approveAmount,
                                onValueChange = { approveAmount = it },
                                placeholder = { Text("0.0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("approve_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldRewards,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val amountWei = EvmCoder.parseUnits(approveAmount, 18)
                                    if (approveSpender.isBlank() || amountWei <= BigInteger.ZERO) {
                                        onShowSnackbar("Please specify spender and amount")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildAglApprove(approveSpender, amountWei)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_approve_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldRewards)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Approve", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approve AGL Allowance", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            3 -> {
                // Burn Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRose.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Burn", tint = NeonRose, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Deflationary Token Burn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonRose)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tokens burned via burn() are permanently destroyed and removed from total supply.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Amount to Burn (AGL)", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = burnAmount,
                                onValueChange = { burnAmount = it },
                                placeholder = { Text("0.0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("burn_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonRose,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val amountWei = EvmCoder.parseUnits(burnAmount, 18)
                                    if (amountWei <= BigInteger.ZERO) {
                                        onShowSnackbar("Please enter a valid amount to burn")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildAglBurn(amountWei)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_burn_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Burn", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Permanently Burn AGL", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            4 -> {
                // Interactive Recharts-Style Token Balance Pie Chart
                item {
                    TokenBalancePieChart(
                        walletAddress = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET,
                        title = "Token Balance Distribution",
                        onSliceSelected = { slice ->
                            if (slice != null) {
                                onShowSnackbar("${slice.name}: ${slice.formattedBalance} (${slice.formattedValueUsd})")
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AglDexSwapTabContent(
    walletState: LiveWalletState?,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val dexService = remember { DexAggregatorService() }
    val scope = rememberCoroutineScope()

    var tokenIn by remember { mutableStateOf(DexAggregatorService.SUPPORTED_TOKENS[0]) } // ETH default
    var tokenOut by remember { mutableStateOf(DexAggregatorService.SUPPORTED_TOKENS[1]) } // AGL default
    var amountInText by remember { mutableStateOf("0.1") }
    var slippagePercent by remember { mutableDoubleStateOf(0.5) }

    var currentQuote by remember { mutableStateOf<DexQuote?>(null) }
    var isLoadingQuote by remember { mutableStateOf(false) }

    // Recalculate quote when tokens, amount, or slippage changes
    LaunchedEffect(tokenIn, tokenOut, amountInText, slippagePercent) {
        val amountVal = amountInText.toDoubleOrNull() ?: 0.0
        if (amountVal > 0) {
            isLoadingQuote = true
            val res = dexService.getBestQuote(
                tokenIn = tokenIn,
                tokenOut = tokenOut,
                amountIn = amountVal,
                slippageTolerancePercent = slippagePercent,
                userAddress = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET
            )
            res.onSuccess { currentQuote = it }.onFailure { currentQuote = null }
            isLoadingQuote = false
        } else {
            currentQuote = null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agl_dex_swap_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "DEX Swap",
                        tint = BaseCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decentralized DEX Swap",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BaseBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Aerodrome & UniV3", fontSize = 11.sp, color = BaseCyan, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Token IN Input Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("You Pay", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "Balance: ${if (tokenIn.symbol == "AGL") walletState?.formattedAglBalance ?: "0.0" else walletState?.formattedEthBalance ?: "0.0"} ${tokenIn.symbol}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Amount TextField
                        OutlinedTextField(
                            value = amountInText,
                            onValueChange = { amountInText = it },
                            placeholder = { Text("0.0", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("swap_amount_in_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BaseCyan,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Token selector button
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.clickable {
                                // Cycle tokenIn
                                val nextIdx = (DexAggregatorService.SUPPORTED_TOKENS.indexOf(tokenIn) + 1) % DexAggregatorService.SUPPORTED_TOKENS.size
                                val newToken = DexAggregatorService.SUPPORTED_TOKENS[nextIdx]
                                if (newToken == tokenOut) {
                                    tokenOut = tokenIn
                                }
                                tokenIn = newToken
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tokenIn.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tokenIn.symbol, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                    }

                    // Quick percent buttons
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("25%", "50%", "75%", "MAX").forEach { pct ->
                            OutlinedButton(
                                onClick = {
                                    val bal = if (tokenIn.symbol == "AGL") {
                                        walletState?.formattedAglBalance?.toDoubleOrNull() ?: 100.0
                                    } else {
                                        walletState?.formattedEthBalance?.toDoubleOrNull() ?: 1.0
                                    }
                                    val factor = when(pct) {
                                        "25%" -> 0.25
                                        "50%" -> 0.50
                                        "75%" -> 0.75
                                        else -> 1.0
                                    }
                                    amountInText = "%.4f".format(bal * factor)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp)
                            ) {
                                Text(pct, fontSize = 10.sp, color = BaseCyan)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Swap Switch Direction Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = tokenIn
                        tokenIn = tokenOut
                        tokenOut = temp
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                        .border(1.dp, BaseCyan, CircleShape)
                        .testTag("swap_direction_toggle")
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Reverse Swap", tint = BaseCyan, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Token OUT Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("You Receive (Estimated)", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLoadingQuote) "Calculating..." else (currentQuote?.formattedAmountOut ?: "0.0"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )

                        // Token selector button
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.clickable {
                                // Cycle tokenOut
                                val nextIdx = (DexAggregatorService.SUPPORTED_TOKENS.indexOf(tokenOut) + 1) % DexAggregatorService.SUPPORTED_TOKENS.size
                                val newToken = DexAggregatorService.SUPPORTED_TOKENS[nextIdx]
                                if (newToken == tokenIn) {
                                    tokenIn = tokenOut
                                }
                                tokenOut = newToken
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tokenOut.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tokenOut.symbol, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slippage Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = "Slippage", tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Slippage Tolerance", fontSize = 11.sp, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.1, 0.5, 1.0, 3.0).forEach { slip ->
                        val isSelected = slippagePercent == slip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BaseCyan.copy(alpha = 0.2f) else DarkBackground)
                                .border(1.dp, if (isSelected) BaseCyan else DarkBorder, RoundedCornerShape(6.dp))
                                .clickable { slippagePercent = slip }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$slip%",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BaseCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            // Live Quote Details Card
            currentQuote?.let { quote ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Exchange Rate", fontSize = 11.sp, color = TextMuted)
                            Text(quote.formattedRate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Min. Received ($slippagePercent%)", fontSize = 11.sp, color = TextMuted)
                            Text(quote.formattedMinimumReceived, fontSize = 11.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Price Impact / Fee", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "%.2f%% • ~$%.2f Gas".format(quote.priceImpactPercent, quote.estimatedGasUsd),
                                fontSize = 11.sp,
                                color = if (quote.priceImpactPercent > 2.0) NeonRose else NeonEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Best Router", fontSize = 11.sp, color = TextMuted)
                            Text(quote.protocolName, fontSize = 11.sp, color = BaseCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Execute Swap Button
            Button(
                onClick = {
                    val quote = currentQuote
                    if (quote == null) {
                        onShowSnackbar("Please enter a valid swap amount")
                        return@Button
                    }
                    val amountWei = EvmCoder.parseUnits(amountInText, tokenIn.decimals)
                    val req = TxPipelineRequest(
                        title = "DEX Swap ${tokenIn.symbol} → ${tokenOut.symbol}",
                        description = "Executing decentralized swap via ${quote.protocolName} router on Base Mainnet",
                        targetContract = quote.routerAddress,
                        tokenRequired = tokenIn.symbol,
                        amountWei = if (tokenIn.isNative) amountWei else BigInteger.ZERO,
                        calldata = "0x"
                    )
                    onStartTx(req)
                    onShowSnackbar("Submitted DEX Swap: ${tokenIn.symbol} -> ${tokenOut.symbol}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_dex_swap_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoadingQuote) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DarkBackground, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculating Route...", color = DarkBackground, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = DarkBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Swap ${tokenIn.symbol} for ${tokenOut.symbol}",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
