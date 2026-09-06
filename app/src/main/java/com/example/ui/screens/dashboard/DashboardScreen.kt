package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.model.AglEcosystemStats
import com.example.data.model.AglOraclePriceData
import com.example.data.model.BaseTransaction
import com.example.data.model.TransactionStatus
import com.example.data.remote.blockchain.diagnostics.NetworkDiagnosticReport
import com.example.ui.components.charts.D3WalletBalanceChart
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    activeWalletAddress: String,
    isConnected: Boolean,
    networkDiagnostics: NetworkDiagnosticReport?,
    isRunningDiagnostics: Boolean,
    oraclePriceData: AglOraclePriceData,
    isRefreshingOracle: Boolean,
    ecosystemStats: AglEcosystemStats,
    isAiThinking: Boolean,
    isIndexingTransactions: Boolean,
    indexerStatusMessage: String,
    transactions: List<BaseTransaction>,
    notifications: List<NotificationEntity>,
    userProgress: UserProgressEntity?,
    onRefresh: () -> Unit,
    onRunDiagnostics: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen")
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BaseBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = BaseCyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Agent Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = { onRefresh() }, modifier = Modifier.testTag("dashboard_refresh")) {
                    if (isRefreshingOracle) {
                        CircularProgressIndicator(color = BaseCyan, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BaseCyan)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Agent Status Card
        item {
            val networkHealthy = networkDiagnostics?.allRelationshipsVerified ?: true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (networkHealthy) NeonEmerald.copy(alpha = 0.5f) else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing status dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isConnected) "Agent Online" else "Demo Mode",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isAiThinking) "AI processing…" else "Idle — monitoring Base Mainnet",
                                    fontSize = 12.sp,
                                    color = if (isAiThinking) GoldRewards else BaseCyan
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("OPERATIONAL", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    DashboardStatRow(
                        label = "Connected Wallet",
                        value = "${activeWalletAddress.take(6)}…${activeWalletAddress.takeLast(4)}",
                        mono = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DashboardStatRow(
                        label = "Network",
                        value = "Base Mainnet • Chain ${networkDiagnostics?.chainId ?: 8453L}"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DashboardStatRow(
                        label = "Latest Block",
                        value = "#${networkDiagnostics?.currentBlock ?: 50741280L}"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DashboardStatRow(
                        label = "RPC Latency",
                        value = "${networkDiagnostics?.rpcLatencyMs ?: 48L} ms",
                        valueColor = NeonEmerald
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = indexerStatusMessage,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isIndexingTransactions) {
                            CircularProgressIndicator(color = BaseCyan, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BaseBlue.copy(alpha = 0.15f))
                            .clickable(enabled = !isRunningDiagnostics) { onRunDiagnostics() }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("dashboard_run_diagnostics"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isRunningDiagnostics) {
                            CircularProgressIndicator(color = BaseCyan, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking Base nodes…", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-Run Network Diagnostics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 30-Day D3 Wallet Balance Line Chart
        item {
            D3WalletBalanceChart(walletAddress = activeWalletAddress)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    iconTint = NeonEmerald,
                    label = "AGL Price",
                    value = "$${"%.2f".format(oraclePriceData.currentPriceUsd)}",
                    subValue = "${if (oraclePriceData.change24hPercent >= 0) "+" else ""}${"%.1f".format(oraclePriceData.change24hPercent)}% 24h",
                    subValueColor = if (oraclePriceData.change24hPercent >= 0) NeonEmerald else NeonRose
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AutoAwesome,
                    iconTint = RadiantPurple,
                    label = "Active Agents",
                    value = formatCount(ecosystemStats.totalActiveAgents),
                    subValue = "online globally",
                    subValueColor = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    iconTint = BaseCyan,
                    label = "Contract Audits",
                    value = formatCount(ecosystemStats.totalContractAuditsCompleted),
                    subValue = "completed",
                    subValueColor = TextSecondary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Bolt,
                    iconTint = GoldRewards,
                    label = "Agent Level",
                    value = "Lv ${userProgress?.level ?: 24}",
                    subValue = "${userProgress?.totalXp ?: 12450} XP",
                    subValueColor = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Activity — Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "View Wallet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BaseCyan,
                    modifier = Modifier
                        .clickable { onNavigate(AppScreen.WALLET) }
                        .testTag("dashboard_view_wallet")
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val recentTx = transactions.take(5)
        if (recentTx.isEmpty()) {
            item {
                EmptyHint(text = "No recent on-chain transactions yet.")
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            items(recentTx) { tx ->
                ActivityRow(
                    title = tx.type.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { it.uppercase() },
                    subtitle = "${tx.value} ${tx.tokenSymbol} • ${formatTime(tx.timestamp)}",
                    status = tx.status
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Recent Notifications
        if (notifications.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Latest Alerts",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            items(notifications.take(4)) { notif ->
                NotificationRow(notif = notif)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DashboardStatRow(
    label: String,
    value: String,
    mono: Boolean = false,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontFamily = if (mono) FontFamily.Monospace else null
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subValue: String,
    subValueColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subValue, fontSize = 11.sp, color = subValueColor)
        }
    }
}

@Composable
private fun ActivityRow(
    title: String,
    subtitle: String,
    status: TransactionStatus
) {
    val (statusLabel, statusColor) = when (status) {
        TransactionStatus.SUCCESS -> "SUCCESS" to NeonEmerald
        TransactionStatus.PENDING -> "PENDING" to GoldRewards
        TransactionStatus.FAILED -> "FAILED" to NeonRose
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
    }
}

@Composable
private fun NotificationRow(notif: NotificationEntity) {
    val tint = when (notif.type) {
        "SECURITY" -> NeonRose
        "TRANSACTION" -> BaseCyan
        "REWARD" -> GoldRewards
        "MISSION" -> RadiantPurple
        else -> TextSecondary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(notif.message, fontSize = 11.sp, color = TextSecondary, maxLines = 2)
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, color = TextMuted)
    }
}

private fun formatCount(count: Long): String =
    when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
