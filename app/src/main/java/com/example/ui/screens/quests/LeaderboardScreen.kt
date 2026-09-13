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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.LeaderboardUser
import com.example.data.model.SuperAgentTier
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardScreen(
    users: List<LeaderboardUser>,
    selectedTimeframe: LeaderboardTimeframe,
    onSelectTimeframe: (LeaderboardTimeframe) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Super Agent Leaderboards",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Rankings based on on-chain missions, learning XP & Base activity.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Timeframe selection chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeaderboardTimeframe.entries.forEach { timeframe ->
                    FilterChip(
                        selected = selectedTimeframe == timeframe,
                        onClick = { onSelectTimeframe(timeframe) },
                        label = {
                            Text(
                                text = timeframe.name.replace("_", " ").lowercase().replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BaseBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(users) { user ->
            LeaderboardUserRow(user = user)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LeaderboardUserRow(user: LeaderboardUser) {
    val tierColor = Color(user.tier.colorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (user.isCurrentUser) BaseBlue.copy(alpha = 0.2f) else DarkCard)
            .border(
                1.dp,
                if (user.isCurrentUser) BaseCyan else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("leaderboard_row_${user.rank}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rank Number / Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (user.rank) {
                            1 -> GoldRewards
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> DarkCardElevated
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user.rank}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (user.rank <= 3) DarkCard else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.avatarEmoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isCurrentUser) BaseCyan else TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lvl ${user.level}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(text = " • ", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = user.tier.displayName,
                        fontSize = 11.sp,
                        color = tierColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%,d XP".format(user.xp),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GoldRewards
            )
            Text(
                text = "${user.questsCompleted} quests",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
