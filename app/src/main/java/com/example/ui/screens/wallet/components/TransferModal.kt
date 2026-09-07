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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalClipboardManager
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
import com.example.data.remote.blockchain.services.SwapToken
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TransferModal(
    onDismiss: () -> Unit,
    senderAddress: String,
    onExecuteTransfer: (token: SwapToken, recipient: String, amount: Double, onDone: (Result<String>) -> Unit) -> Unit
) {
    var selectedToken by remember { mutableStateOf(DexAggregatorService.SUPPORTED_TOKENS[1]) } // AGL default
    var recipientAddress by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("10.0") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showBiometricAuthOverlay by remember { mutableStateOf(false) }
    var executedTxHash by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("transfer_token_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = BaseCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Transfer Tokens",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Base Mainnet L2 • Low Gas",
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
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = NeonEmerald, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Transfer Sent on Base!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                                    onClick = { executedTxHash = null },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                                ) {
                                    Text("Send More", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                } else {
                    // Token Selector Row
                    Text("Select Asset", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DexAggregatorService.SUPPORTED_TOKENS.forEach { token ->
                            val isSelected = selectedToken.symbol == token.symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BaseBlue.copy(alpha = 0.25f) else DarkBackground)
                                    .border(1.dp, if (isSelected) BaseCyan else DarkBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedToken = token }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(token.iconEmoji, fontSize = 16.sp)
                                    Text(token.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val clipboardManager = LocalClipboardManager.current

                    // Recipient Address Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recipient Base Address or Basename", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "0xD034...27C8",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BaseBlue.copy(alpha = 0.2f))
                                .clickable {
                                    recipientAddress = "0xD034E94465Db1669f80D817c66e58cF194d027C8"
                                    errorMessage = null
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = recipientAddress,
                        onValueChange = { recipientAddress = it; errorMessage = null },
                        placeholder = { Text("0x... or user.base.eth", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_recipient_input"),
                        trailingIcon = {
                            Button(
                                onClick = {
                                    val text = clipboardManager.getText()?.text
                                    if (!text.isNullOrBlank()) {
                                        recipientAddress = text.trim()
                                        errorMessage = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue.copy(alpha = 0.35f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = BaseCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PASTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Input
                    Text("Amount to Transfer", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorMessage = null },
                        placeholder = { Text("0.00", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            Text(
                                text = selectedToken.symbol,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gas Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated Network Fee", fontSize = 11.sp, color = TextMuted)
                            Text("< $0.005 USD (Base L2)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    val currentAmt = amountText.toDoubleOrNull() ?: 0.0
                    val currentEstUsd = currentAmt * selectedToken.basePriceUsd
                    val isCurrentHighValue = currentEstUsd >= 100.0 || (currentAmt >= 0.05 && selectedToken.symbol == "ETH") || (currentAmt >= 100.0 && selectedToken.symbol == "AGL")

                    if (isCurrentHighValue && currentAmt > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("transfer_high_value_pill"),
                            colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "High-Value Transfer (≈ $%.2f USD): Secondary Biometrics Required".format(currentEstUsd),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val recip = recipientAddress.trim()
                            if (recip.isBlank()) {
                                errorMessage = "Please enter a recipient address"
                                return@Button
                            }
                            if (amt <= 0.0) {
                                errorMessage = "Please enter a valid transfer amount"
                                return@Button
                            }

                            errorMessage = null
                            showBiometricAuthOverlay = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("execute_transfer_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Broadcasting on Base...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Send ${selectedToken.symbol}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // Biometric Security Guard Overlay before broadcasting transfer on Base
        if (showBiometricAuthOverlay) {
            val amt = amountText.toDoubleOrNull() ?: 0.0
            val recip = recipientAddress.trim()
            val estUsdValue = amt * selectedToken.basePriceUsd
            val isHighValue = estUsdValue >= 100.0 || (amt >= 0.05 && selectedToken.symbol == "ETH") || (amt >= 100.0 && selectedToken.symbol == "AGL")
            TransactionBiometricAuthOverlay(
                details = TransactionBiometricDetails(
                    title = if (isHighValue) "Authorize High-Value Transfer" else "Authorize Token Transfer",
                    actionType = "TRANSFER",
                    primaryAmount = "$amountText ${selectedToken.symbol}",
                    secondaryAmount = "≈ $%.2f USD".format(estUsdValue),
                    recipientOrTarget = recip,
                    estimatedGas = "< $0.005 USD (Base L2)",
                    isHighValue = isHighValue,
                    highValueWarning = if (isHighValue) "Transaction value exceeds $100 USD threshold (≈ $%.2f USD). Secondary biometric authentication is required before on-chain signing.".format(estUsdValue) else null
                ),
                onAuthorized = {
                    showBiometricAuthOverlay = false
                    isSubmitting = true
                    errorMessage = null
                    onExecuteTransfer(selectedToken, recip, amt) { res ->
                        isSubmitting = false
                        res.onSuccess { hash ->
                            executedTxHash = hash
                        }.onFailure {
                            errorMessage = "Transfer failed: ${it.message}"
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
