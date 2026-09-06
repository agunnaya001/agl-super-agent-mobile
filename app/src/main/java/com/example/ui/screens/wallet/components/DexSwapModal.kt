package com.example.ui.screens.wallet.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.services.DexAggregatorService
import com.example.data.remote.blockchain.services.DexQuote
import com.example.data.remote.blockchain.services.SwapToken
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
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
import kotlinx.coroutines.launch

@Composable
fun DexSwapModal(
    onDismiss: () -> Unit,
    dexService: DexAggregatorService,
    userAddress: String,
    initialTokenIn: SwapToken = DexAggregatorService.SUPPORTED_TOKENS[0], // ETH
    initialTokenOut: SwapToken = DexAggregatorService.SUPPORTED_TOKENS[1], // AGL
    onExecuteSwap: (quote: DexQuote, onDone: (Result<String>) -> Unit) -> Unit,
    onExecuteApprove: (tokenAddress: String, spenderAddress: String, onDone: (Result<String>) -> Unit) -> Unit
) {
    var tokenIn by remember { mutableStateOf(initialTokenIn) }
    var tokenOut by remember { mutableStateOf(initialTokenOut) }
    var amountInText by remember { mutableStateOf("0.1") }
    var slippagePercent by remember { mutableDoubleStateOf(0.5) }
    var currentQuote by remember { mutableStateOf<DexQuote?>(null) }
    var isLoadingQuote by remember { mutableStateOf(false) }
    var quoteError by remember { mutableStateOf<String?>(null) }

    var isApproving by remember { mutableStateOf(false) }
    var isSwapping by remember { mutableStateOf(false) }
    var showBiometricAuthOverlay by remember { mutableStateOf(false) }
    var executedTxHash by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // Fetch live quote
    fun refreshQuote() {
        val amount = amountInText.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            currentQuote = null
            return
        }
        isLoadingQuote = true
        quoteError = null
        scope.launch {
            val result = dexService.getBestQuote(
                tokenIn = tokenIn,
                tokenOut = tokenOut,
                amountIn = amount,
                slippageTolerancePercent = slippagePercent,
                userAddress = userAddress
            )
            isLoadingQuote = false
            result.onSuccess {
                currentQuote = it
            }.onFailure {
                quoteError = it.message ?: "Failed to compute DEX route"
            }
        }
    }

    LaunchedEffect(tokenIn, tokenOut, amountInText, slippagePercent) {
        refreshQuote()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("dex_swap_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BaseBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔄", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DEX Aggregator Swap",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Aerodrome • Uniswap V3 • Base Mainnet",
                                fontSize = 11.sp,
                                color = BaseCyan
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (executedTxHash != null) {
                    // Success View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = NeonEmerald,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Swap Executed on Base!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tx: ${executedTxHash!!.take(12)}...${executedTxHash!!.takeLast(8)}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        uriHandler.openUri(BaseBlockchainConfig.getExplorerTxUrl(executedTxHash!!))
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Basescan", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        executedTxHash = null
                                        refreshQuote()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                                ) {
                                    Text("New Swap", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                } else {
                    // Token In Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("You Pay", fontSize = 12.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Token: ", fontSize = 11.sp, color = TextSecondary)
                                    // Token In Selector pills
                                    DexAggregatorService.SUPPORTED_TOKENS.take(3).forEach { token ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (tokenIn.symbol == token.symbol) BaseBlue.copy(alpha = 0.3f) else Color.Transparent)
                                                .border(
                                                    1.dp,
                                                    if (tokenIn.symbol == token.symbol) BaseCyan else DarkBorder,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    if (tokenOut.symbol == token.symbol) {
                                                        tokenOut = tokenIn
                                                    }
                                                    tokenIn = token
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(token.symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedTextField(
                                    value = amountInText,
                                    onValueChange = { amountInText = it; actionError = null },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("swap_amount_in_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkCardElevated)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tokenIn.iconEmoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tokenIn.symbol, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Swap Flip Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = tokenIn
                                tokenIn = tokenOut
                                tokenOut = temp
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                                .border(1.dp, BaseCyan, CircleShape)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Flip", tint = BaseCyan, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Token Out Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("You Receive (Estimated)", fontSize = 12.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Token: ", fontSize = 11.sp, color = TextSecondary)
                                    DexAggregatorService.SUPPORTED_TOKENS.forEach { token ->
                                        if (token.symbol != tokenIn.symbol) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (tokenOut.symbol == token.symbol) BaseBlue.copy(alpha = 0.3f) else Color.Transparent)
                                                    .border(
                                                        1.dp,
                                                        if (tokenOut.symbol == token.symbol) BaseCyan else DarkBorder,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { tokenOut = token }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(token.symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isLoadingQuote) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = BaseCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = currentQuote?.formattedAmountOut ?: "0.00",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmerald
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkCardElevated)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tokenOut.iconEmoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tokenOut.symbol, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quote Details & Route Card
                    if (currentQuote != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rate", fontSize = 11.sp, color = TextMuted)
                                    Text(currentQuote!!.formattedRate, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Routing Protocol", fontSize = 11.sp, color = TextMuted)
                                    Text(currentQuote!!.protocolName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Est. Base Gas", fontSize = 11.sp, color = TextMuted)
                                    Text("~$%.4f USD".format(currentQuote!!.estimatedGasUsd), fontSize = 11.sp, color = NeonEmerald)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Min. Received (${slippagePercent}%)", fontSize = 11.sp, color = TextMuted)
                                    Text(currentQuote!!.formattedMinimumReceived, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    if (actionError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(actionError!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (Approve if needed, then Swap)
                    if (currentQuote?.needsApproval == true) {
                        Button(
                            onClick = {
                                isApproving = true
                                actionError = null
                                onExecuteApprove(
                                    currentQuote!!.tokenIn.contractAddress,
                                    currentQuote!!.spenderToApprove
                                ) { res ->
                                    isApproving = false
                                    res.onSuccess {
                                        refreshQuote()
                                    }.onFailure {
                                        actionError = "Approval failed: ${it.message}"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dex_approve_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldRewards),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isApproving
                        ) {
                            if (isApproving) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approving ${tokenIn.symbol}...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Text("1. Approve ${tokenIn.symbol} for Base Router", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (currentQuote == null) return@Button
                            showBiometricAuthOverlay = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dex_execute_swap_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = currentQuote != null && !isSwapping && !isApproving && (currentQuote?.needsApproval == false)
                    ) {
                        if (isSwapping) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Broadcasting Swap on Base...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Swap ${tokenIn.symbol} → ${tokenOut.symbol}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Biometric Security Guard Overlay before broadcasting on Base
        if (showBiometricAuthOverlay && currentQuote != null) {
            TransactionBiometricAuthOverlay(
                details = TransactionBiometricDetails(
                    title = "Authorize Base DEX Swap",
                    actionType = "SWAP",
                    primaryAmount = "$amountInText ${tokenIn.symbol}",
                    secondaryAmount = "≈ ${currentQuote!!.formattedAmountOut} ${tokenOut.symbol}",
                    recipientOrTarget = currentQuote!!.routerAddress,
                    protocolOrSpender = currentQuote!!.protocolName,
                    estimatedGas = "~$%.4f USD".format(currentQuote!!.estimatedGasUsd)
                ),
                onAuthorized = {
                    showBiometricAuthOverlay = false
                    isSwapping = true
                    actionError = null
                    onExecuteSwap(currentQuote!!) { res ->
                        isSwapping = false
                        res.onSuccess { hash ->
                            executedTxHash = hash
                        }.onFailure {
                            actionError = "Swap failed: ${it.message}"
                        }
                    }
                },
                onDismiss = {
                    showBiometricAuthOverlay = false
                }
            )
        }
    }
}
