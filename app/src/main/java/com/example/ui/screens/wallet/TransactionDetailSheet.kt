package com.example.ui.screens.wallet

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BaseTransaction
import com.example.data.model.TransactionStatus
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: BaseTransaction,
    onDismiss: () -> Unit,
    onExplainWithAi: (BaseTransaction) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Base Transaction Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Amount Transferred",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${transaction.value} ${transaction.tokenSymbol}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (transaction.status) {
                                    TransactionStatus.SUCCESS -> NeonEmerald.copy(alpha = 0.2f)
                                    TransactionStatus.PENDING -> GoldRewards.copy(alpha = 0.2f)
                                    TransactionStatus.FAILED -> DangerCrimson.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = transaction.status.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (transaction.status) {
                                TransactionStatus.SUCCESS -> NeonEmerald
                                TransactionStatus.PENDING -> GoldRewards
                                TransactionStatus.FAILED -> DangerCrimson
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Plain English Summary
            if (!transaction.simpleExplanation.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BaseBlue.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BaseCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Simple Explanation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = transaction.simpleExplanation,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // On-Chain Parameters Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(
                    label = "Transaction Hash",
                    value = "${transaction.hash.take(12)}...${transaction.hash.takeLast(10)}",
                    copyValue = transaction.hash,
                    onCopy = { onShowSnackbar("Hash copied") },
                    clipboardManager = clipboardManager
                )
                DetailRow(
                    label = "From",
                    value = "${transaction.fromAddress.take(8)}...${transaction.fromAddress.takeLast(6)}",
                    copyValue = transaction.fromAddress,
                    onCopy = { onShowSnackbar("From address copied") },
                    clipboardManager = clipboardManager
                )
                DetailRow(
                    label = "To / Contract",
                    value = "${transaction.toAddress.take(8)}...${transaction.toAddress.takeLast(6)}",
                    copyValue = transaction.toAddress,
                    onCopy = { onShowSnackbar("To address copied") },
                    clipboardManager = clipboardManager
                )
                DetailRow(
                    label = "Base Block",
                    value = "#${transaction.blockNumber}"
                )
                DetailRow(
                    label = "Gas Used / Fee",
                    value = "%.4f Gwei ($%.4f USD)".format(transaction.gasUsedGwei, transaction.gasFeeUsd)
                )
                DetailRow(
                    label = "Timestamp",
                    value = dateFormatter.format(Date(transaction.timestamp))
                )
                if (transaction.methodCalled != null) {
                    DetailRow(
                        label = "Method Signature",
                        value = transaction.methodCalled,
                        isCode = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button: Ask AI to Explain
            Button(
                onClick = {
                    onDismiss()
                    onExplainWithAi(transaction)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("explain_tx_with_ai_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ask AI to Explain in Simple English",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    copyValue: String? = null,
    onCopy: (() -> Unit)? = null,
    clipboardManager: ClipboardManager? = null,
    isCode: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = if (isCode) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default
            )
            if (copyValue != null && clipboardManager != null && onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(copyValue))
                        onCopy()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = BaseCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
