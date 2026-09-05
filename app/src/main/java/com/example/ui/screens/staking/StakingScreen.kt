package com.example.ui.screens.staking

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.remote.blockchain.StakingInfo
import com.example.data.remote.blockchain.StakingPosition
import com.example.data.remote.blockchain.StakingTier
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
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
fun StakingScreen(
    stakingInfo: StakingInfo?,
    tiers: List<StakingTier>,
    positions: List<StakingPosition>,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var stakeAmount by remember { mutableStateOf("100") }
    var selectedTierId by remember { mutableIntStateOf(tiers.firstOrNull()?.tierId ?: 0) }

    val aglBalance = walletState?.formattedAglBalance ?: "0.00"
    val totalStaked = stakingInfo?.formattedTotalStaked ?: "0.00 AGL"
    val rewardPool = stakingInfo?.formattedRewardPoolBalance ?: "0.00 AGL"

    val userTotalStakedTokens = positions.sumOf { it.amountWei.toDouble() / 1e18 }
    val userTotalClaimableTokens = positions.sumOf { it.totalClaimableWei.toDouble() / 1e18 }
    val userPendingRewardsTokens = positions.sumOf { it.pendingRewardWei.toDouble() / 1e18 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("staking_screen")
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
                    text = "AGL Staking Pool",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Global Pool Stats Card
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
                                    .background(GoldRewards.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Diamond, contentDescription = "Staking", tint = GoldRewards, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tiered Yield Pool",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Fixed Duration & Dynamic APY",
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
                            Text("Total Staked (Pool)", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = totalStaked,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Reward Pool Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = rewardPool,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldRewards
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Stats summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Your Staked AGL", fontSize = 10.sp, color = TextMuted)
                            Text("%.2f AGL".format(userTotalStakedTokens), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pending Rewards", fontSize = 10.sp, color = TextMuted)
                            Text("%.2f AGL".format(userPendingRewardsTokens), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Claimable", fontSize = 10.sp, color = TextMuted)
                            Text("%.2f AGL".format(userTotalClaimableTokens), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldRewards)
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
                            text = BaseBlockchainConfig.STAKING_CONTRACT.take(12) + "..." + BaseBlockchainConfig.STAKING_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.STAKING_CONTRACT))
                                    onShowSnackbar("Staking contract address copied")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerAddressUrl(BaseBlockchainConfig.STAKING_CONTRACT)
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
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Tiers Selector Section
        item {
            Text(
                text = "Available Staking Tiers",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tiers) { tier ->
                    val isSelected = tier.tierId == selectedTierId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) BaseBlue.copy(alpha = 0.25f) else DarkCard)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BaseCyan else DarkBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedTierId = tier.tierId }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tier #${tier.tierId}",
                                    fontSize = 11.sp,
                                    color = if (isSelected) BaseCyan else TextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (tier.isActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonEmerald)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tier.formattedDuration,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${tier.aprPercent}% APR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldRewards
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stake New Position Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lock AGL into Tier #$selectedTierId",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text("Balance: $aglBalance AGL", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = stakeAmount,
                        onValueChange = { stakeAmount = it },
                        placeholder = { Text("0.0", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stake_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountWei = EvmCoder.parseUnits(stakeAmount, 18)
                            if (amountWei <= BigInteger.ZERO) {
                                onShowSnackbar("Please enter a valid amount of AGL to stake")
                                return@Button
                            }
                            val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                .buildStakeAgl(amountWei, selectedTierId)
                            onStartTx(req)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stake_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Stake", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stake AGL (Approval Pipeline)", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Active Positions List
        item {
            Text(
                text = "Your Staking Positions (${positions.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (positions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCard)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LockOpen, contentDescription = "No positions", tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active staking positions", fontSize = 14.sp, color = TextSecondary)
                        Text("Lock AGL in any tier above to start earning yield", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(positions) { pos ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (pos.isUnlocked) NeonEmerald.copy(alpha = 0.5f) else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Position #${pos.positionId}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BaseBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Tier #${pos.tierId} (${pos.aprPercent}% APR)", fontSize = 10.sp, color = BaseCyan)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (pos.isUnlocked) NeonEmerald.copy(alpha = 0.2f) else DarkBorder)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (pos.isUnlocked) "UNLOCKED" else "LOCKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pos.isUnlocked) NeonEmerald else TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Principal Staked", fontSize = 11.sp, color = TextMuted)
                                Text("${pos.formattedAmount} AGL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Pending Yield", fontSize = 11.sp, color = TextMuted)
                                Text("+${pos.formattedPendingReward} AGL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlocks: ${pos.formattedUnlockDate}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildUnstakeAgl(pos.positionId)
                                    onStartTx(req)
                                },
                                enabled = pos.isUnlocked,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                            ) {
                                Text("Unstake & Claim", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val req = com.example.data.remote.blockchain.tx.TransactionPipelineEngine()
                                        .buildEmergencyWithdraw(pos.positionId)
                                    onStartTx(req)
                                },
                                modifier = Modifier.weight(1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonRose.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = NeonRose, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Emergency", color = NeonRose, fontSize = 12.sp)
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
