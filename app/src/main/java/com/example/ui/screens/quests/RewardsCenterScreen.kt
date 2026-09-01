package com.example.ui.screens.quests

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
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

@Composable
fun RewardsCenterScreen(
    userProfile: UserProfile?,
    rewardsHistory: List<RewardHistoryEntity>,
    onDailyCheckIn: () -> Unit
) {
    val streakDays = userProfile?.consecutiveStreakDays ?: 7
    val totalClaimedAgl = userProfile?.totalRewardsClaimedAgl ?: 185.0
    val dateFormatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ecosystem Rewards & Vault",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Claim daily streaks, mission bounties, and staking APY yield.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            // Rewards Summary Hero Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rewards_summary_card"),
                cornerRadius = 16.dp,
                backgroundColor = DarkCardElevated,
                borderColor = GoldRewards.copy(alpha = 0.35f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total AGL Rewards Earned",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%.2f AGL".format(totalClaimedAgl),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldRewards
                            )
                        }

                        // Super Agent Streak Card
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BaseBlue.copy(alpha = 0.2f))
                                .border(1.dp, BaseCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = GoldRewards,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$streakDays Day Streak",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "1.5x Multiplier",
                                    fontSize = 10.sp,
                                    color = BaseCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onDailyCheckIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("rewards_checkin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldRewards),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "⚡ Claim Day $streakDays Check-In (+5.0 AGL)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBackground
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Rewards History & Bounties",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (rewardsHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reward history found.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(rewardsHistory) { reward ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldRewards.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎁", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = reward.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = dateFormatter.format(Date(reward.timestamp)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+%.1f AGL".format(reward.aglAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GoldRewards
                        )
                        Text(
                            text = "+${reward.xpAmount} XP",
                            fontSize = 10.sp,
                            color = NeonEmerald
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
