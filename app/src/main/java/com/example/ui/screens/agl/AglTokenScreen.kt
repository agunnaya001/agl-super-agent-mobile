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

@Composable
fun AglTokenScreen(
    metadata: TokenMetadata?,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                    // Contract address row
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
                            text = BaseBlockchainConfig.AGL_TOKEN_CONTRACT.take(14) + "..." + BaseBlockchainConfig.AGL_TOKEN_CONTRACT.takeLast(8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Row {
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

        // Action Tabs: Transfer, Approve, Burn
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
                    text = { Text("Transfer", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Approve", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Burn", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab content
        when (selectedTab) {
            0 -> {
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

            1 -> {
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

            2 -> {
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
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
