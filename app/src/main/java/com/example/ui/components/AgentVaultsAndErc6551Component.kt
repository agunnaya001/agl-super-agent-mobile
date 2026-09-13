package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class YieldVault(
    val id: String,
    val name: String,
    val pairSymbol1: String,
    val pairSymbol2: String,
    val protocol: String,
    val apy: Double,
    val tvlUsd: Double,
    val totalBurnedAgl: Double,
    val userStakedUsd: Double
)

data class Erc6551AgentAccount(
    val agentId: String,
    val agentName: String,
    val smartAccountAddress: String,
    val aglBalance: Double,
    val ethBalance: Double,
    val activeStrategy: String,
    val totalTradesExecuted: Int,
    val winRatePercent: Double
)

/**
 * Aerodrome Liquidity Yield Vaults & ERC-6551 Token Bound Agent Account Component.
 * Features:
 * - Auto-compounding Aerodrome v3 Liquidity Pools & veAERO Bribe Vaults
 * - Deflationary AGL Buyback-and-Burn Tracker
 * - ERC-6551 Token Bound Smart Contract Account Manager (Agent Wallets)
 * - 1-Click Vault Deposit & Agent Funding
 */
@Composable
fun AgentVaultsAndErc6551Component(
    walletAddress: String,
    modifier: Modifier = Modifier,
    onExecuteTx: (title: String, targetContract: String, callData: String) -> Unit = { _, _, _ -> }
) {
    var activeTab by remember { mutableStateOf("VAULTS") } // VAULTS or ERC6551
    var depositModalVault by remember { mutableStateOf<YieldVault?>(null) }
    var depositAmountInput by remember { mutableStateOf("100.0") }
    val clipboardManager = LocalClipboardManager.current

    val vaults = remember {
        listOf(
            YieldVault(
                id = "aerodrome_agl_eth",
                name = "Aerodrome v3 AGL/ETH Auto-Vault",
                pairSymbol1 = "AGL",
                pairSymbol2 = "ETH",
                protocol = "Aerodrome Finance v3",
                apy = 84.5,
                tvlUsd = 1_420_000.0,
                totalBurnedAgl = 42_500.0,
                userStakedUsd = 650.00
            ),
            YieldVault(
                id = "aerodrome_agl_usdc",
                name = "AGL/USDC High Yield Pool",
                pairSymbol1 = "AGL",
                pairSymbol2 = "USDC",
                protocol = "Aerodrome Concentrated",
                apy = 62.8,
                tvlUsd = 890_000.0,
                totalBurnedAgl = 28_100.0,
                userStakedUsd = 0.0
            ),
            YieldVault(
                id = "veaero_bribe_vault",
                name = "veAERO Voting Bribe & Burn Vault",
                pairSymbol1 = "wAGL",
                pairSymbol2 = "AERO",
                protocol = "Base Bribe Protocol",
                apy = 112.4,
                tvlUsd = 2_150_000.0,
                totalBurnedAgl = 115_000.0,
                userStakedUsd = 250.00
            )
        )
    }

    val agentAccount = remember(walletAddress) {
        Erc6551AgentAccount(
            agentId = "#6551-AGL-8842",
            agentName = "Base Autonomous Arbitrage Agent",
            smartAccountAddress = "0x6551D034E94465Db1669f80D817c66e58cF194d027",
            aglBalance = 850.0,
            ethBalance = 0.15,
            activeStrategy = "DEX Triangular Arbitrage & Liquidity Rebalancing",
            totalTradesExecuted = 142,
            winRatePercent = 94.2
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("agent_vaults_erc6551_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Bar
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
                            .background(Brush.linearGradient(listOf(BaseBlue, RadiantPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Vaults",
                            tint = BaseCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Yield Vaults & ERC-6551",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Aerodrome v3 Liquidity & Token Bound Accounts",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Switch Tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardElevated)
                        .padding(3.dp)
                ) {
                    listOf("VAULTS" to "Vaults", "ERC6551" to "6551 Wallet").forEach { (tabKey, tabLabel) ->
                        val isSelected = (activeTab == tabKey)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BaseCyan else Color.Transparent)
                                .clickable { activeTab = tabKey }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tabLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DarkBackground else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == "VAULTS") {
                // Total AGL Burned Deflationary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF3B0000),
                                    Color(0xFF1E0A00)
                                )
                            )
                        )
                        .border(1.dp, NeonRose.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
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
                                    .background(NeonRose.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Burn",
                                    tint = NeonRose,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Deflationary Buyback & Burn Engine",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonRose
                                )
                                Text(
                                    text = "185,600 AGL ($157,760) Permanently Burned",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonRose.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("-7.4% Supply", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonRose)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vaults List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    vaults.forEach { vault ->
                        VaultRowCard(
                            vault = vault,
                            onDepositClick = { depositModalVault = vault }
                        )
                    }
                }
            } else {
                // ERC-6551 Agent Account View
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkBackground)
                            .border(1.dp, BaseCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TokenLogoComponent(symbol = "AGL", size = 28.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = agentAccount.agentName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = agentAccount.agentId,
                                            fontSize = 10.sp,
                                            color = BaseCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Autonomous Smart Wallet", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Address Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCardElevated)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "6551 Contract: ${agentAccount.smartAccountAddress.take(10)}...${agentAccount.smartAccountAddress.takeLast(6)}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Address",
                                    tint = BaseCyan,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(agentAccount.smartAccountAddress))
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Agent Wallet Balances
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkCardElevated)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("Agent AGL Balance", fontSize = 10.sp, color = TextMuted)
                                        Text(
                                            "${agentAccount.aglBalance} AGL",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkCardElevated)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("Agent ETH Reserve", fontSize = 10.sp, color = TextMuted)
                                        Text(
                                            "${agentAccount.ethBalance} ETH",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Performance Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Active Execution Strategy", fontSize = 10.sp, color = TextMuted)
                                    Text(agentAccount.activeStrategy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Win Rate", fontSize = 10.sp, color = TextMuted)
                                    Text("${agentAccount.winRatePercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onExecuteTx(
                                        "Fund ERC-6551 Agent Smart Wallet",
                                        agentAccount.smartAccountAddress,
                                        "0x"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fund_6551_agent_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Deposit Funds to Agent ERC-6551 Wallet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Vault Deposit
    if (depositModalVault != null) {
        val vault = depositModalVault!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { depositModalVault = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Deposit into Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(vault.name, fontSize = 12.sp, color = BaseCyan)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expected APY:", fontSize = 12.sp, color = TextMuted)
                        Text("${vault.apy}% APY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = depositAmountInput,
                        onValueChange = { depositAmountInput = it },
                        label = { Text("Deposit Amount (${vault.pairSymbol1})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_deposit_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = BaseCyan,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { depositModalVault = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                val amount = depositAmountInput.toDoubleOrNull() ?: 100.0
                                onExecuteTx(
                                    "Deposit ${amount} ${vault.pairSymbol1} to ${vault.name}",
                                    "0xD034E94465Db1669f80D817c66e58cF194d027C8",
                                    "0x"
                                )
                                depositModalVault = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Deposit", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultRowCard(
    vault: YieldVault,
    onDepositClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        TokenLogoComponent(symbol = vault.pairSymbol1, size = 24.dp)
                        Spacer(modifier = Modifier.width((-6).dp))
                        TokenLogoComponent(symbol = vault.pairSymbol2, size = 24.dp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(vault.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(vault.protocol, fontSize = 10.sp, color = TextMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("${vault.apy}% APY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pool TVL", fontSize = 9.sp, color = TextMuted)
                    Text("$%,.0f".format(vault.tvlUsd), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }

                Column {
                    Text("Auto Buyback & Burn", fontSize = 9.sp, color = TextMuted)
                    Text("${vault.totalBurnedAgl.toInt()} AGL", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NeonRose, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                }
            }
        }
    }
}
