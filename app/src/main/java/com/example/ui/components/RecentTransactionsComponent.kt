package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.TransactionType
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
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

enum class TransactionFilterCategory(val label: String, val emoji: String) {
    ALL("All", "⚡"),
    TRANSFERS("Transfers", "💸"),
    STAKING("Staking", "🔒"),
    SWAPS("Swaps", "🔄"),
    CALLS("Contracts", "⚙️"),
    REWARDS("Rewards", "🎁")
}

/**
 * RecentTransactionsComponent provides a comprehensive, real-time UI feed
 * of wallet activity fetched via on-chain indexing (Basescan / Base RPC).
 * Every transaction showcases a descriptive, AI-generated human-readable summary.
 */
@Composable
fun RecentTransactionsComponent(
    transactions: List<BaseTransaction>,
    isLoading: Boolean = false,
    indexerStatus: String = "Live Basescan Indexer",
    onRefresh: () -> Unit = {},
    onSelectTransaction: (BaseTransaction) -> Unit = {},
    onExplainWithAi: (BaseTransaction) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    maxDisplayCount: Int = 10,
    showFilterChips: Boolean = true,
    showViewAllButton: Boolean = true,
    onViewAllClick: () -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf(TransactionFilterCategory.ALL) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        val list = when (selectedFilter) {
            TransactionFilterCategory.ALL -> transactions
            TransactionFilterCategory.TRANSFERS -> transactions.filter {
                it.type == TransactionType.TRANSFER_IN || it.type == TransactionType.TRANSFER_OUT
            }
            TransactionFilterCategory.STAKING -> transactions.filter {
                it.type == TransactionType.STAKE_AGL
            }
            TransactionFilterCategory.SWAPS -> transactions.filter {
                it.type == TransactionType.SWAP
            }
            TransactionFilterCategory.CALLS -> transactions.filter {
                it.type == TransactionType.CONTRACT_CALL || it.type == TransactionType.MINT
            }
            TransactionFilterCategory.REWARDS -> transactions.filter {
                it.type == TransactionType.CLAIM_REWARD
            }
        }
        list.take(maxDisplayCount)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_transactions_component")
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Pulse live indexer badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonEmerald.copy(alpha = 0.12f))
                            .border(1.dp, NeonEmerald.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = indexerStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonEmerald
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("refresh_tx_indexer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh on-chain activity",
                        tint = BaseCyan,
                        modifier = if (isLoading) Modifier.rotate(rotation) else Modifier
                    )
                }

                if (showViewAllButton) {
                    Text(
                        text = "View All →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan,
                        modifier = Modifier
                            .clickable { onViewAllClick() }
                            .padding(start = 4.dp)
                            .testTag("view_all_transactions_header_button")
                    )
                }
            }
        }

        // Filter chips
        if (showFilterChips) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilterCategory.entries.forEach { category ->
                    val isSelected = selectedFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = category },
                        label = {
                            Text(
                                text = "${category.emoji} ${category.label}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCard,
                            selectedContainerColor = BaseBlue.copy(alpha = 0.25f),
                            labelColor = TextSecondary,
                            selectedLabelColor = BaseCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) BaseCyan.copy(alpha = 0.6f) else DarkBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_tx_${category.name.lowercase()}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content: Loading, Empty, or Transaction Cards
        when {
            isLoading && transactions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = BaseCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Querying Base Mainnet & Basescan indexer...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            filteredTransactions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions found for this filter.",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Switch filter or refresh from Base on-chain indexer.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = BaseCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sync Indexer", fontSize = 11.sp, color = BaseCyan)
                        }
                    }
                }
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredTransactions.forEach { tx ->
                        EnhancedTransactionItemCard(
                            transaction = tx,
                            onClick = { onSelectTransaction(tx) },
                            onExplainWithAi = { onExplainWithAi(tx) },
                            onCopyHash = { hash ->
                                clipboardManager.setText(AnnotatedString(hash))
                                onShowSnackbar("Transaction hash copied")
                            },
                            onCopyExplorerUrl = { hash ->
                                val explorerUrl = "${BaseBlockchainConfig.EXPLORER_BASE_URL}/tx/$hash"
                                clipboardManager.setText(AnnotatedString(explorerUrl))
                                onShowSnackbar("Basescan link copied to clipboard")
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * EnhancedTransactionItemCard presents the transaction with its AI-generated human-readable summary,
 * metadata badges, gas telemetry, and quick action buttons.
 */
@Composable
fun EnhancedTransactionItemCard(
    transaction: BaseTransaction,
    onClick: () -> Unit,
    onExplainWithAi: () -> Unit,
    onCopyHash: (String) -> Unit,
    onCopyExplorerUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeLabel, iconEmoji, accentColor, prefix) = when (transaction.type) {
        TransactionType.TRANSFER_IN -> Quadruple("Received", "📥", NeonEmerald, "+")
        TransactionType.TRANSFER_OUT -> Quadruple("Sent", "📤", TextSecondary, "-")
        TransactionType.SWAP -> Quadruple("DEX Swap", "🔄", BaseCyan, "~")
        TransactionType.STAKE_AGL -> Quadruple("Staked wAGL", "🔒", GoldRewards, "🔒")
        TransactionType.CLAIM_REWARD -> Quadruple("Claimed Credits", "🎁", NeonEmerald, "+")
        TransactionType.MINT -> Quadruple("Minted Asset", "✨", BaseBlue, "+")
        TransactionType.CONTRACT_CALL -> Quadruple("Contract Call", "⚙️", BaseCyan, "⚡")
    }

    val timeAgo = remember(transaction.timestamp) {
        formatRelativeTime(transaction.timestamp)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("tx_card_${transaction.hash.take(10)}"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Type badge, Value, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = iconEmoji, fontSize = 17.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = typeLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            if (transaction.status != TransactionStatus.SUCCESS) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${transaction.status.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transaction.status == TransactionStatus.FAILED) DangerCrimson else GoldRewards
                                )
                            }
                        }
                        Text(
                            text = timeAgo,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Amount and token symbol
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$prefix${transaction.value} ${transaction.tokenSymbol}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = accentColor
                    )
                    Text(
                        text = "Block #${transaction.blockNumber}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI-Generated Human-Readable Summary Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BaseBlue.copy(alpha = 0.09f),
                                DarkCardElevated
                            )
                        )
                    )
                    .border(1.dp, BaseBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BaseCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "AI Explanation",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                        }

                        // Chain badge
                        Text(
                            text = "Base L2",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val summaryText = transaction.simpleExplanation
                        ?: "Verified on-chain transaction executed on Base Mainnet. Token balances updated with sub-second finality."

                    Text(
                        text = summaryText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = TextPrimary.copy(alpha = 0.95f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer telemetry and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Method called or gas cost
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!transaction.methodCalled.isNullOrBlank()) {
                        val displayMethod = transaction.methodCalled.substringBefore("(")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = displayMethod,
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "Gas: $${String.format(Locale.US, "%.3f", transaction.gasFeeUsd)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Right: Explorer link and Ask AI buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onCopyHash(transaction.hash) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_tx_hash_${transaction.hash.take(8)}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy hash",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCopyExplorerUrl(transaction.hash) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("open_basescan_${transaction.hash.take(8)}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "View on Basescan",
                            tint = BaseCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BaseBlue.copy(alpha = 0.15f))
                            .border(1.dp, BaseBlue.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable { onExplainWithAi() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("ask_ai_tx_${transaction.hash.take(8)}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BaseCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ask AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatRelativeTime(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            formatter.format(Date(timestampMs))
        }
    }
}
