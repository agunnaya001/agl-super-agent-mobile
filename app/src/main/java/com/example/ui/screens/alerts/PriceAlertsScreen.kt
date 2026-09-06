package com.example.ui.screens.alerts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entities.PriceAlertEntity
import com.example.data.model.AglOraclePriceData
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskReviewBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceAlertsScreen(
    oracleData: AglOraclePriceData,
    isRefreshingOracle: Boolean,
    priceAlerts: List<PriceAlertEntity>,
    onBack: () -> Unit,
    onRefreshOracle: () -> Unit,
    onSimulatePrice: (Double?) -> Unit,
    onAddAlert: (targetPriceUsd: Double, condition: String, note: String, oneTimeOnly: Boolean) -> Unit,
    onToggleAlert: (id: Long, enabled: Boolean) -> Unit,
    onRearmAlert: (id: Long) -> Unit,
    onDeleteAlert: (id: Long) -> Unit,
    onTestTriggerAlert: (id: Long) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
            if (isGranted) {
                onShowSnackbar("Push notifications enabled for AGL price alerts")
            } else {
                onShowSnackbar("Notification permission denied. Alerts may not pop up in status bar.")
            }
        }
    )

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSimulationPanel by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("price_alerts_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("alerts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Price Alerts & Oracles",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Base Mainnet Chainlink Aggregator V3",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = BaseCyan
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshOracle,
                    enabled = !isRefreshingOracle,
                    modifier = Modifier.testTag("refresh_oracle_button")
                ) {
                    if (isRefreshingOracle) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BaseCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Price",
                            tint = BaseCyan
                        )
                    }
                }
            }
        }

        // Android 13+ Notification Permission Prompt
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AmberWarning, RadiantPurple)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Alert Notifications",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Allow notifications to receive immediate alerts when AGL hits your target thresholds.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Hero Live Oracle Price Card
        item {
            HeroOracleCard(
                oracleData = oracleData,
                isRefreshing = isRefreshingOracle,
                onCopyAddress = {
                    clipboardManager.setText(AnnotatedString(oracleData.contractAddress))
                    onShowSnackbar("Oracle feed contract address copied")
                },
                onToggleSimulation = { showSimulationPanel = !showSimulationPanel }
            )
        }

        // Market Movement Simulator Panel (For instant threshold testing)
        item {
            AnimatedVisibility(
                visible = showSimulationPanel,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MarketSimulatorCard(
                    currentPrice = oracleData.currentPriceUsd,
                    onSimulatePrice = onSimulatePrice,
                    onResetLive = { onSimulatePrice(null) }
                )
            }
        }

        // Section Title & Set Alert Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = BaseCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Price Thresholds (${priceAlerts.size})",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    modifier = Modifier.testTag("set_alert_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Price Alert Items
        if (priceAlerts.isEmpty()) {
            item {
                EmptyPriceAlertsCard(onCreateClick = { showCreateDialog = true })
            }
        } else {
            items(priceAlerts, key = { it.id }) { alert ->
                PriceAlertItemCard(
                    alert = alert,
                    currentPrice = oracleData.currentPriceUsd,
                    onToggle = { enabled -> onToggleAlert(alert.id, enabled) },
                    onRearm = { onRearmAlert(alert.id) },
                    onDelete = { onDeleteAlert(alert.id) },
                    onTestTrigger = { onTestTriggerAlert(alert.id) }
                )
            }
        }

        // Oracle Architecture Explainer
        item {
            OracleArchitectureCard()
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Modal Sheet / Dialog to Create Price Alert
    if (showCreateDialog) {
        CreatePriceAlertDialog(
            currentPrice = oracleData.currentPriceUsd,
            onDismiss = { showCreateDialog = false },
            onConfirm = { targetPrice, condition, note, oneTimeOnly ->
                onAddAlert(targetPrice, condition, note, oneTimeOnly)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun HeroOracleCard(
    oracleData: AglOraclePriceData,
    isRefreshing: Boolean,
    onCopyAddress: () -> Unit,
    onToggleSimulation: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("oracle_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BaseBlue, BaseCyan)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Live Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                            .alpha(pulseAlpha)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHAINLINK AGGREGATOR V3",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = DarkBorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = BaseCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${oracleData.latencyMs}ms latency",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Price Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AGL / USD Price",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$${"%.3f".format(oracleData.currentPriceUsd)}",
                        style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = if (oracleData.change24hPercent >= 0) RiskLowBg else RiskHighBg,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (oracleData.change24hPercent >= 0) NeonEmerald else DangerCrimson,
                                if (oracleData.change24hPercent >= 0) BaseCyan else NeonRose
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (oracleData.change24hPercent >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (oracleData.change24hPercent >= 0) NeonEmerald else DangerCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (oracleData.change24hPercent >= 0) "+%.2f%%".format(oracleData.change24hPercent) else "%.2f%%".format(oracleData.change24hPercent),
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (oracleData.change24hPercent >= 0) NeonEmerald else DangerCrimson
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkBorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            // Oracle Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Feed Source",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = oracleData.oracleProvider.take(24),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Aggregator Round",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "#${oracleData.roundId.takeLast(8)}",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = BaseCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(oracleData.updatedAt))
                Text(
                    text = "Last Tick: $timeStr (Heartbeat: ${oracleData.heartbeatSeconds}s)",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )

                Text(
                    text = "Contract: ${oracleData.contractAddress.take(6)}...${oracleData.contractAddress.takeLast(4)}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = BaseCyan,
                    modifier = Modifier.clickable { onCopyAddress() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Simulator Trigger
            OutlinedButton(
                onClick = onToggleSimulation,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_simulator_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BaseCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(DarkBorder, BaseCyan.copy(alpha = 0.5f)))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Price Movement / Trigger Alerts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MarketSimulatorCard(
    currentPrice: Double,
    onSimulatePrice: (Double?) -> Unit,
    onResetLive: () -> Unit
) {
    var manualPriceText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_simulator_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RadiantPurple, BaseCyan)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = RadiantPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Oracle Simulator & Test Suite",
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Reset Live",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = NeonRose,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onResetLive() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Simulate price changes to verify immediate notification triggering when thresholds are crossed.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Quick Movements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Triple("Dip -8%", 3.15, DangerCrimson),
                    Triple("Dip -15%", 2.90, DangerCrimson),
                    Triple("Rally +12%", 3.85, NeonEmerald),
                    Triple("Moon +35%", 4.65, NeonEmerald)
                )

                presets.forEach { (label, target, color) ->
                    OutlinedButton(
                        onClick = { onSimulatePrice(target) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$${"%.2f".format(target)}", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom manual price input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualPriceText,
                    onValueChange = { manualPriceText = it },
                    placeholder = { Text("Enter target e.g. 3.95", fontSize = 12.sp, color = TextMuted) },
                    prefix = { Text("$ ", color = BaseCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BaseCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val parsed = manualPriceText.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onSimulatePrice(parsed)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RadiantPurple),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Apply Tick", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PriceAlertItemCard(
    alert: PriceAlertEntity,
    currentPrice: Double,
    onToggle: (Boolean) -> Unit,
    onRearm: () -> Unit,
    onDelete: () -> Unit,
    onTestTrigger: () -> Unit
) {
    val isAbove = alert.condition == "ABOVE"
    val diff = currentPrice - alert.targetPriceUsd
    val pctDiff = (diff / alert.targetPriceUsd) * 100.0

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            alert.isTriggered -> NeonRose
            alert.isEnabled -> if (isAbove) NeonEmerald else AmberWarning
            else -> DarkBorder
        },
        label = "alertBorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("price_alert_item_${alert.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isTriggered) DarkCardElevated else DarkCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(cardBorderColor.copy(alpha = 0.8f), DarkBorder)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Condition, Target Price, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isAbove) RiskLowBg else RiskReviewBg,
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(if (isAbove) NeonEmerald else AmberWarning, DarkBorder)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAbove) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isAbove) NeonEmerald else AmberWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAbove) "RISES ABOVE" else "DROPS BELOW",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAbove) NeonEmerald else AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$${"%.3f".format(alert.targetPriceUsd)}",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = alert.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BaseCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkCardElevated
                        ),
                        modifier = Modifier.testTag("toggle_alert_${alert.id}")
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Alert",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strategy Note (if provided)
            if (alert.note.isNotBlank()) {
                Text(
                    text = alert.note,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Distance / Proximity Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val distanceLabel = if (isAbove) {
                    if (currentPrice >= alert.targetPriceUsd) {
                        "Target Met! Currently $${"%.3f".format(currentPrice)}"
                    } else {
                        val toGo = alert.targetPriceUsd - currentPrice
                        "Needs +$${"%.3f".format(toGo)} (+${"%.2f".format((toGo / currentPrice) * 100)}%) to trigger"
                    }
                } else {
                    if (currentPrice <= alert.targetPriceUsd) {
                        "Target Met! Currently $${"%.3f".format(currentPrice)}"
                    } else {
                        val toGo = currentPrice - alert.targetPriceUsd
                        "Needs -$${"%.3f".format(toGo)} (-${"%.2f".format((toGo / currentPrice) * 100)}%) to trigger"
                    }
                }

                Text(
                    text = distanceLabel,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = if (alert.isTriggered) NeonRose else TextTertiary
                )

                if (alert.oneTimeOnly) {
                    Text(
                        text = "1x Auto-Disable",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = RadiantPurple
                    )
                }
            }

            // Triggered Banner & Rearm Button
            if (alert.isTriggered) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val triggerTimeStr = alert.lastTriggeredTimestamp?.let {
                            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
                        } ?: "Recent"
                        Text(
                            text = "Triggered at $${"%.3f".format(alert.lastTriggeredPriceUsd ?: alert.targetPriceUsd)} ($triggerTimeStr)",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonRose
                        )
                    }

                    Button(
                        onClick = onRearm,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = BaseCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Arm", fontSize = 12.sp, color = BaseCyan)
                    }
                }
            }

            // Test Trigger Notification Button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Test Push Notification",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = BaseCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onTestTrigger() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyPriceAlertsCard(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorderSubtle)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Price Alerts Configured",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Set thresholds to get instant Android notifications when AGL token reaches your target buy/sell prices.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set First Price Alert")
            }
        }
    }
}

@Composable
fun CreatePriceAlertDialog(
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (targetPrice: Double, condition: String, note: String, oneTimeOnly: Boolean) -> Unit
) {
    var condition by remember { mutableStateOf("ABOVE") }
    var targetPriceText by remember { mutableStateOf("%.3f".format(currentPrice * 1.10)) }
    var noteText by remember { mutableStateOf("") }
    var oneTimeOnly by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set AGL Price Threshold",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Current Oracle Price: $${"%.3f".format(currentPrice)} (Chainlink Base Feed)",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = BaseCyan
                )

                // Condition Selector: ABOVE vs BELOW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = condition == "ABOVE",
                        onClick = {
                            condition = "ABOVE"
                            targetPriceText = "%.3f".format(currentPrice * 1.10)
                        },
                        label = { Text("▲ Rises Above", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskLowBg,
                            selectedLabelColor = NeonEmerald
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = condition == "BELOW",
                        onClick = {
                            condition = "BELOW"
                            targetPriceText = "%.3f".format(currentPrice * 0.90)
                        },
                        label = { Text("▼ Drops Below", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskReviewBg,
                            selectedLabelColor = AmberWarning
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Target Price Field
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it },
                    label = { Text("Target Price (USD)") },
                    prefix = { Text("$ ", color = BaseCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BaseCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Quick percentage adjustments
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val deltas = if (condition == "ABOVE") {
                        listOf(0.05, 0.10, 0.20, 0.35)
                    } else {
                        listOf(-0.05, -0.10, -0.15, -0.25)
                    }

                    deltas.forEach { delta ->
                        val target = currentPrice * (1.0 + delta)
                        val sign = if (delta > 0) "+${(delta * 100).toInt()}%" else "${(delta * 100).toInt()}%"
                        Surface(
                            color = DarkCardElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetPriceText = "%.3f".format(target) }
                        ) {
                            Text(
                                text = sign,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (delta > 0) NeonEmerald else DangerCrimson,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Strategy Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Strategy Note (Optional)") },
                    placeholder = { Text("e.g. wAGL yield harvest zone", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BaseCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 2
                )

                // 1-Time Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Auto-Disable After Trigger",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Turn off alert automatically once hit",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = oneTimeOnly,
                        onCheckedChange = { oneTimeOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BaseCyan
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPriceText.toDoubleOrNull()
                    if (price != null && price > 0) {
                        onConfirm(price, condition, noteText.trim(), oneTimeOnly)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Alert")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun OracleArchitectureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorderSubtle)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BaseCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Oracle & Notification Architecture",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val points = listOf(
                "• Chainlink Aggregator V3: Queries latestRoundData() on Base Mainnet to obtain verified on-chain exchange rates.",
                "• Aerodrome & DeFi TWAP: Hybrid backup feeds ensure resilient price reporting even during high L2 volatility.",
                "• Local Notification Engine: Zero-delay push alerts executed via Android NotificationManagerCompat directly on device.",
                "• Room Database Persistence: Alerts, trigger history, and custom notes persist reliably across application sessions."
            )

            points.forEach { point ->
                Text(
                    text = point,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
