package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.tx.TxExecutionResult
import com.example.data.remote.blockchain.tx.TxPipelineRequest
import com.example.data.remote.blockchain.tx.TxStatus
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TxPipelineDialog(
    request: TxPipelineRequest?,
    status: TxStatus,
    executionResult: TxExecutionResult?,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onConfirm: () -> Unit
) {
    if (request == null) return
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("tx_pipeline_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BaseBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (request.isEmergency) Icons.Default.Warning else Icons.Default.Security,
                                contentDescription = "Security",
                                tint = if (request.isEmergency) NeonRose else BaseCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = request.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = request.description,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Target Contract:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = request.targetContract,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // State-specific layout
                when (status) {
                    TxStatus.CHECKING_ALLOWANCE -> {
                        CircularProgressIndicator(
                            color = BaseCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Verifying on-chain allowance on Base...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    TxStatus.NEEDS_APPROVAL -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldRewards.copy(alpha = 0.1f))
                                .border(1.dp, GoldRewards.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Approval required",
                                    tint = GoldRewards,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ERC-20 Approval Required",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldRewards
                                    )
                                    Text(
                                        text = "Target spender needs permission to transfer required AGL tokens.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onApprove,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approve_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldRewards)
                        ) {
                            Text(
                                text = "Approve AGL Token Allowance",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TxStatus.APPROVING -> {
                        CircularProgressIndicator(
                            color = GoldRewards,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Submitting ERC-20 approval transaction...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    TxStatus.PREPARING_EXECUTION -> {
                        CircularProgressIndicator(
                            color = BaseCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Simulating & broadcasting to Base L2...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    TxStatus.PENDING_CONFIRMATION -> {
                        // Details Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Network", fontSize = 12.sp, color = TextMuted)
                            Text("Base Mainnet (8453)", fontSize = 12.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Gas Limit", fontSize = 12.sp, color = TextMuted)
                            Text("${request.estimatedGas} gas", fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_tx_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (request.isEmergency) NeonRose else BaseBlue
                            )
                        ) {
                            Text(
                                text = if (request.isEmergency) "Confirm Emergency Withdraw" else "Sign & Broadcast Transaction",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TxStatus.CONFIRMED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NeonEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Transaction Confirmed!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                        val hash = executionResult?.transactionHash ?: ""
                        if (hash.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tx: ${hash.take(16)}...",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerTxUrl(hash)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("View on Basescan", color = BaseCyan)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.NorthEast,
                                        contentDescription = "Open",
                                        tint = BaseCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                        ) {
                            Text("Done", color = Color.White)
                        }
                    }

                    TxStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = NeonRose,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Transaction Failed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonRose
                        )
                        Text(
                            text = executionResult?.errorMessage ?: "Reverted on Base RPC simulation",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                        ) {
                            Text("Close", color = TextPrimary)
                        }
                    }

                    TxStatus.IDLE -> {}
                }
            }
        }
    }
}
