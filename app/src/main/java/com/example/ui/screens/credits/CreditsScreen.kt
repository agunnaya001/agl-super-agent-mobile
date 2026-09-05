package com.example.ui.screens.credits

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.remote.blockchain.services.AglCreditsInfo
import com.example.data.remote.blockchain.services.LiveWalletState
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
fun CreditsScreen(
    creditsInfo: AglCreditsInfo?,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var depositAmountAgl by remember { mutableStateOf("10") }

    val rate = creditsInfo?.creditsPerAgl ?: BigInteger.valueOf(100)
    val parsedAgl = depositAmountAgl.toDoubleOrNull() ?: 0.0
    val previewCreditsEst = (parsedAgl * rate.toDouble()).toInt()

    val aglBalance = walletState?.formattedAglBalance ?: "0.00"
    val userCreditsPurchased = creditsInfo?.formattedUserCredits ?: "0.00"
    val totalAglBurned = creditsInfo?.formattedTotalAglBurned ?: "0.00 AGL"
    val userAglBurned = creditsInfo?.formattedUserAglBurned ?: "0.00 AGL"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("credits_screen")
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
                    text = "AGL Compute Credits",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Overview Card
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
                                    .background(BaseCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = "Credits", tint = BaseCyan, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Compute Credits",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Fixed Rate: $rate Credits per AGL",
                                    fontSize = 12.sp,
                                    color = BaseCyan
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("User Credits Purchased", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = userCreditsPurchased,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Available AGL", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$aglBalance AGL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Burn Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Burned", tint = NeonRose, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Total Protocol Burned", fontSize = 10.sp, color = TextMuted)
                                Text(totalAglBurned, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("You Burned", fontSize = 10.sp, color = TextMuted)
                            Text(userAglBurned, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonRose)
                        }
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
                            text = BaseBlockchainConfig.AGL_CREDITS_CONTRACT.take(12) + "..." + BaseBlockchainConfig.AGL_CREDITS_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.AGL_CREDITS_CONTRACT))
                                    onShowSnackbar("Credits contract address copied")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerAddressUrl(BaseBlockchainConfig.AGL_CREDITS_CONTRACT)
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

        // Purchase Credits Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Purchase", tint = BaseCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Purchase Compute Credits",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deposits AGL to purchase compute credits. The contract automatically burns or routes AGL per tokenomics.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("AGL to Deposit", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = depositAmountAgl,
                        onValueChange = { depositAmountAgl = it },
                        placeholder = { Text("0.0", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_agl_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BaseBlue.copy(alpha = 0.1f))
                            .border(1.dp, BaseBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preview Output:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "+$previewCreditsEst Credits",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountWei = EvmCoder.parseUnits(depositAmountAgl, 18)
                            if (amountWei <= BigInteger.ZERO) {
                                onShowSnackbar("Please enter a valid amount of AGL")
                                return@Button
                            }
                            val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                .buildPurchaseCredits(amountWei)
                            onStartTx(req)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purchase_credits_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Purchase", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Purchase Credits (Approval Pipeline)", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
