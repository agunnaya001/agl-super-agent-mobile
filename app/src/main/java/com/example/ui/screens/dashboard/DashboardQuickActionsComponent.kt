package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TokenAsset
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Quick Action Grid on Dashboard showing top-performing assets with 1-tap Swap & Bridge.
 */
@Composable
fun DashboardQuickActionGrid(
    topAssets: List<TokenAsset>,
    onSwapAsset: (TokenAsset) -> Unit,
    onBridgeAsset: (TokenAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_quick_action_grid"),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
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
                            .background(BaseCyan.copy(alpha = 0.15f))
                            .border(1.dp, BaseCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = BaseCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quick Actions • Top Assets",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Instant DEX Swap & L1/L2 Cross-Chain Bridge",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BaseCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BASE 8453",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Asset Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topAssets.take(4).forEach { asset ->
                    QuickAssetCard(
                        asset = asset,
                        onSwap = { onSwapAsset(asset) },
                        onBridge = { onBridgeAsset(asset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAssetCard(
    asset: TokenAsset,
    onSwap: () -> Unit,
    onBridge: () -> Unit
) {
    val isPositive = asset.change24h >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_action_asset_${asset.symbol}"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Asset Info Row
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
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(asset.iconEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.symbol,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (asset.isEcosystemToken) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BaseBlue.copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("CORE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                }
                            }
                        }
                        Text(
                            text = asset.name,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // 24h Change Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPositive) NeonEmerald.copy(alpha = 0.15f) else NeonRose.copy(alpha = 0.15f))
                        .border(1.dp, if (isPositive) NeonEmerald.copy(alpha = 0.35f) else NeonRose.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "↗ +" else "↘ "}${"%.2f".format(asset.change24h)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) NeonEmerald else NeonRose
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price and Balance details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Price", fontSize = 10.sp, color = TextMuted)
                    Text("$${"%.2f".format(asset.priceUsd)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Balance", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${"%.2f".format(asset.balance)} ${asset.symbol} ($${"%.2f".format(asset.totalValueUsd)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BaseCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Action Buttons: Swap & Bridge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSwap,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("dashboard_quick_swap_${asset.symbol}"),
                    colors = ButtonDefaults.buttonColors(containerColor = BaseCyan.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BaseCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = BaseCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Swap", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                }

                Button(
                    onClick = onBridge,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("dashboard_quick_bridge_${asset.symbol}"),
                    colors = ButtonDefaults.buttonColors(containerColor = RadiantPurple.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RadiantPurple.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.AltRoute, contentDescription = "Bridge", tint = RadiantPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bridge", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RadiantPurple)
                }
            }
        }
    }
}

/**
 * Floating Action Button for Quick Actions on Dashboard
 */
@Composable
fun DashboardQuickActionsFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag("dashboard_quick_actions_fab"),
        containerColor = BaseCyan,
        contentColor = DarkBackground,
        shape = RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(Icons.Default.Bolt, contentDescription = "Quick Actions", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Quick Actions",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/**
 * Quick Action Selection Dialog (triggered by FAB)
 */
@Composable
fun DashboardQuickActionsMenuDialog(
    topAssets: List<TokenAsset>,
    onDismiss: () -> Unit,
    onSelectSwap: (TokenAsset) -> Unit,
    onSelectBridge: (TokenAsset) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BaseCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("⚡ Quick Action Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Quickly trigger on-chain operations for your top-performing assets on Base Mainnet.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: DEX Swap Primary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val defaultSwapAsset = topAssets.firstOrNull() ?: TokenAsset(
                                symbol = "AGL",
                                name = "Agunnaya Labs",
                                balance = 420.5,
                                priceUsd = 3.42,
                                change24h = 8.65,
                                iconEmoji = "🪙",
                                contractAddress = ""
                            )
                            onSelectSwap(defaultSwapAsset)
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BaseCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instant DEX Swap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Aerodrome SlipStream & Uniswap V3 on Base", fontSize = 11.sp, color = BaseCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Bridge Primary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val defaultBridgeAsset = topAssets.firstOrNull { it.symbol == "ETH" }
                                ?: topAssets.firstOrNull()
                                ?: TokenAsset(
                                    symbol = "ETH",
                                    name = "Ethereum",
                                    balance = 1.45,
                                    priceUsd = 2680.5,
                                    change24h = 3.12,
                                    iconEmoji = "🔷",
                                    contractAddress = ""
                                )
                            onSelectBridge(defaultBridgeAsset)
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, RadiantPurple.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cross-Chain Bridge", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Ethereum L1 / Arbitrum / OP ⇄ Base Mainnet", fontSize = 11.sp, color = RadiantPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("1-Tap Shortcuts for Top Assets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                topAssets.take(3).forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${asset.iconEmoji} ${asset.symbol} (+${"%.1f".format(asset.change24h)}%)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onSelectSwap(asset) },
                                modifier = Modifier.height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BaseCyan.copy(alpha = 0.5f))
                            ) {
                                Text("Swap", fontSize = 10.sp, color = BaseCyan)
                            }
                            OutlinedButton(
                                onClick = { onSelectBridge(asset) },
                                modifier = Modifier.height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, RadiantPurple.copy(alpha = 0.5f))
                            ) {
                                Text("Bridge", fontSize = 10.sp, color = RadiantPurple)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
            ) {
                Text("Close")
            }
        },
        containerColor = DarkCardElevated
    )
}

/**
 * Interactive In-Dashboard Quick Swap Dialog
 */
@Composable
fun DashboardQuickSwapDialog(
    asset: TokenAsset,
    allAssets: List<TokenAsset>,
    onDismiss: () -> Unit,
    onConfirmSwap: (amountIn: String, tokenIn: String, tokenOut: String, estimatedOut: String) -> Unit
) {
    var amountIn by remember { mutableStateOf("10") }
    var targetSymbol by remember { mutableStateOf(if (asset.symbol == "AGL") "ETH" else "AGL") }
    var slippage by remember { mutableStateOf("0.5") }
    var isBroadcasting by remember { mutableStateOf(false) }

    val inPrice = asset.priceUsd
    val outPrice = allAssets.find { it.symbol == targetSymbol }?.priceUsd
        ?: if (targetSymbol == "ETH") 2680.5 else if (targetSymbol == "AGL") 3.42 else 1.0
    val numAmt = amountIn.toDoubleOrNull() ?: 0.0
    val slippageVal = (1.0 - (slippage.toDoubleOrNull() ?: 0.5) / 100.0)
    val estimatedOut = if (outPrice > 0) ((numAmt * inPrice) / outPrice) * slippageVal else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔄", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Instant DEX Swap", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Aerodrome Router • Base Mainnet (8453)", fontSize = 11.sp, color = BaseCyan)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Pay Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("You Pay", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "Bal: ${"%.2f".format(asset.balance)} ${asset.symbol}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = amountIn,
                                onValueChange = { amountIn = it },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BaseBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "${asset.iconEmoji} ${asset.symbol}",
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.25 to "25%", 0.5 to "50%", 1.0 to "MAX").forEach { (pct, label) ->
                                OutlinedButton(
                                    onClick = {
                                        amountIn = "%.2f".format(asset.balance * pct)
                                    },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Text(label, fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Receive Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("You Receive (Estimated)", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "1 ${asset.symbol} ≈ ${"%.3f".format(inPrice / outPrice)} $targetSymbol",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "%.4f".format(estimatedOut),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )

                            // Target token selector chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("ETH", "AGL", "USDC").filter { it != asset.symbol }.forEach { sym ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (targetSymbol == sym) BaseCyan.copy(alpha = 0.25f) else DarkBackground)
                                            .border(1.dp, if (targetSymbol == sym) BaseCyan else DarkBorder, RoundedCornerShape(8.dp))
                                            .clickable { targetSymbol = sym }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (targetSymbol == sym) BaseCyan else TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Slippage Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Slippage Tolerance", fontSize = 11.sp, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("0.1", "0.5", "1.0").forEach { s ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (slippage == s) BaseCyan.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (slippage == s) BaseCyan else DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable { slippage = s }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("$s%", fontSize = 10.sp, color = if (slippage == s) BaseCyan else TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Network fee
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Base Network Fee", fontSize = 11.sp, color = TextSecondary)
                    Text("~$0.0012 (Blob Gas)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isBroadcasting = true
                    onConfirmSwap(amountIn, asset.symbol, targetSymbol, "%.4f".format(estimatedOut))
                },
                enabled = !isBroadcasting && numAmt > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_confirm_swap_button")
            ) {
                if (isBroadcasting) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Broadcasting Swap on Base...", color = DarkBackground, fontWeight = FontWeight.Bold)
                } else {
                    Text("Swap ${asset.symbol} → $targetSymbol", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkCardElevated
    )
}

/**
 * Interactive In-Dashboard Quick Bridge Dialog
 */
@Composable
fun DashboardQuickBridgeDialog(
    asset: TokenAsset,
    onDismiss: () -> Unit,
    onConfirmBridge: (amount: String, asset: String, sourceChain: String) -> Unit
) {
    var amount by remember { mutableStateOf("0.5") }
    var sourceChain by remember { mutableStateOf("Ethereum Mainnet (L1)") }
    var bridgeProvider by remember { mutableStateOf("Base Official Native Bridge") }
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Cross-Chain Bridge", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Migrate ${asset.symbol} to Base Mainnet (Chain 8453)", fontSize = 11.sp, color = RadiantPurple)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Route Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("From Source Chain", fontSize = 10.sp, color = TextMuted)
                                Text(sourceChain, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("➔", fontSize = 16.sp, color = RadiantPurple, modifier = Modifier.padding(horizontal = 8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("To Target", fontSize = 10.sp, color = TextMuted)
                                Text("Base Mainnet (8453)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Source network buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Ethereum L1", "Arbitrum", "Optimism").forEach { net ->
                                val fullName = if (net == "Ethereum L1") "Ethereum Mainnet (L1)" else "$net Mainnet (L2)"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sourceChain == fullName) RadiantPurple.copy(alpha = 0.2f) else DarkBackground)
                                        .border(1.dp, if (sourceChain == fullName) RadiantPurple else DarkBorder, RoundedCornerShape(6.dp))
                                        .clickable { sourceChain = fullName }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(net, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (sourceChain == fullName) RadiantPurple else TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                Text("Bridge Amount (${asset.symbol})", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RadiantPurple,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        TextButtonSmall(text = "MAX", onClick = { amount = "2.50" })
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Options
                Text("Bridge Provider", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Triple("Base Official Native Bridge", "~10-15 min", "CANONICAL"),
                        Triple("Across Protocol (Instant)", "~1-2 min", "FASTEST")
                    ).forEach { (prov, time, tag) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (bridgeProvider == prov) RadiantPurple.copy(alpha = 0.15f) else DarkSurface)
                                .border(1.dp, if (bridgeProvider == prov) RadiantPurple else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { bridgeProvider = prov }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prov, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Est. Time: $time", fontSize = 10.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RadiantPurple.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RadiantPurple)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // External Link to official bridge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BaseBlue.copy(alpha = 0.15f))
                        .clickable { uriHandler.openUri("https://bridge.base.org/deposit") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open bridge.base.org portal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmBridge(amount, asset.symbol, sourceChain)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RadiantPurple),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_confirm_bridge_button")
            ) {
                Text("Initiate Bridge Transfer", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkCardElevated
    )
}

@Composable
private fun TextButtonSmall(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BaseCyan.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
    }
}
