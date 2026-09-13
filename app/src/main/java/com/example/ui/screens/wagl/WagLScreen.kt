package com.example.ui.screens.wagl

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.remote.blockchain.services.WagLAccountInfo
import com.example.data.remote.blockchain.tx.TxPipelineRequest
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
import java.math.BigInteger

@Composable
fun WagLScreen(
    wAglInfo: WagLAccountInfo?,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var wrapAmount by remember { mutableStateOf("50") }
    var unwrapAmount by remember { mutableStateOf("25") }
    var delegateeAddress by remember { mutableStateOf(walletState?.address ?: "") }

    val aglBalance = walletState?.formattedAglBalance ?: "0.00"
    val wAglBalance = walletState?.formattedWAglBalance ?: "0.00"
    val votingPower = walletState?.formattedVotingPower ?: "0.00"
    val currentDelegate = wAglInfo?.delegatee ?: walletState?.address ?: "Self"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("wagl_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "wAGL Voting Wrapper",
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
                                Icon(Icons.Default.HowToVote, contentDescription = "wAGL", tint = BaseCyan, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Wrapped AGL (wAGL)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ERC20Votes Governance Wrapper",
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
                            Text("1:1 Peg", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("wAGL Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$wAglBalance wAGL",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BaseCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Voting Power", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$votingPower Votes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Delegate:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = if (currentDelegate.length > 14) currentDelegate.take(8) + "..." + currentDelegate.takeLast(6) else currentDelegate,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = BaseCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                            text = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT.take(12) + "..." + BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT))
                                    onShowSnackbar("wAGL contract address copied")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerTokenUrl(BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.NorthEast, contentDescription = "Basescan", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs: Wrap, Unwrap, Delegate
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
                    text = { Text("Wrap", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Unwrap", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Delegate", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab views
        when (selectedTab) {
            0 -> {
                // Wrap Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wrap AGL -> wAGL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Available: $aglBalance AGL", fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = wrapAmount,
                                onValueChange = { wrapAmount = it },
                                placeholder = { Text("0.0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wrap_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val amountWei = EvmCoder.parseUnits(wrapAmount, 18)
                                    val userAddr = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET
                                    if (amountWei <= BigInteger.ZERO) {
                                        onShowSnackbar("Please enter a valid amount")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildWrapAgl(userAddr, amountWei)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_wrap_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Wrap", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wrap to wAGL (Approval Pipeline)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Unwrap Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unwrap wAGL -> AGL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Available: $wAglBalance wAGL", fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = unwrapAmount,
                                onValueChange = { unwrapAmount = it },
                                placeholder = { Text("0.0", color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unwrap_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val amountWei = EvmCoder.parseUnits(unwrapAmount, 18)
                                    val userAddr = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET
                                    if (amountWei <= BigInteger.ZERO) {
                                        onShowSnackbar("Please enter a valid amount")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildUnwrapWAgl(userAddr, amountWei)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_unwrap_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan)
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Unwrap", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unwrap to Liquid AGL", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Delegate Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Delegate Governance Voting Power", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "wAGL requires explicit delegation to activate voting power for DAO proposals. You can delegate to yourself or another community member.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Delegatee Address", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = delegateeAddress,
                                onValueChange = { delegateeAddress = it },
                                placeholder = { Text("0x...", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delegatee_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    delegateeAddress = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                            ) {
                                Text("Delegate to Self", color = BaseCyan)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (delegateeAddress.isBlank()) {
                                        onShowSnackbar("Please specify a delegatee address")
                                        return@Button
                                    }
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildDelegateWAgl(delegateeAddress)
                                    onStartTx(req)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_delegate_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                            ) {
                                Icon(Icons.Default.HowToVote, contentDescription = "Delegate", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delegate Votes on Base", fontWeight = FontWeight.Bold)
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
