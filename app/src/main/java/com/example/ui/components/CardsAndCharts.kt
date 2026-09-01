package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BaseTransaction
import com.example.data.model.TokenAsset
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
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
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = DarkBorder,
    backgroundColor: Color = DarkCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .then(clickableModifier)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    iconEmoji: String? = null,
    isPositive: Boolean? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (iconEmoji != null) {
                    Text(text = iconEmoji, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPositive != null) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.NorthEast else Icons.Default.SouthEast,
                            contentDescription = null,
                            tint = if (isPositive) NeonEmerald else DangerCrimson,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isPositive == true) NeonEmerald else if (isPositive == false) DangerCrimson else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun Web3PortfolioChart(
    points: List<Float> = listOf(6200f, 6340f, 6180f, 6420f, 6750f, 6600f, 7120f, 7450f, 7890f),
    lineColor: Color = BaseCyan,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(120.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (points.size < 2) return@Canvas

            val minVal = points.minOrNull() ?: 0f
            val maxVal = points.maxOrNull() ?: 1f
            val range = (maxVal - minVal).coerceAtLeast(1f)

            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)

            val path = Path()
            val fillPath = Path()

            val firstX = 0f
            val firstY = height - ((points[0] - minVal) / range) * (height * 0.8f) - (height * 0.1f)
            path.moveTo(firstX, firstY)
            fillPath.moveTo(firstX, height)
            fillPath.lineTo(firstX, firstY)

            for (i in 1 until points.size) {
                val currentX = i * stepX
                val currentY = height - ((points[i] - minVal) / range) * (height * 0.8f) - (height * 0.1f)

                val prevX = (i - 1) * stepX
                val prevY = height - ((points[i - 1] - minVal) / range) * (height * 0.8f) - (height * 0.1f)

                // Cubic bezier smooth curve
                val cx1 = prevX + (currentX - prevX) / 2
                val cy1 = prevY
                val cx2 = prevX + (currentX - prevX) / 2
                val cy2 = currentY

                path.cubicTo(cx1, cy1, cx2, cy2, currentX, currentY)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, currentX, currentY)
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw gradient area under the curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw chart line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw glowing end dot
            val lastX = width
            val lastY = height - ((points.last() - minVal) / range) * (height * 0.8f) - (height * 0.1f)
            drawCircle(
                color = BaseBlue,
                radius = 6.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
    }
}

@Composable
fun TokenRow(
    token: TokenAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = token.iconEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = token.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    if (token.isEcosystemToken) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldRewards.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Ecosystem",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldRewards
                            )
                        }
                    }
                }
                Text(
                    text = token.name,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$%.2f".format(token.totalValueUsd),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.4f %s".format(token.balance, token.symbol),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                val isPositive = token.change24h >= 0
                Text(
                    text = (if (isPositive) "+" else "") + "%.2f%%".format(token.change24h),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) NeonEmerald else DangerCrimson
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: BaseTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeLabel, iconEmoji, color) = when (transaction.type) {
        TransactionType.TRANSFER_IN -> Triple("Received", "📥", NeonEmerald)
        TransactionType.TRANSFER_OUT -> Triple("Sent", "📤", TextSecondary)
        TransactionType.SWAP -> Triple("Swapped", "🔄", BaseCyan)
        TransactionType.STAKE_AGL -> Triple("Staked AGL", "🔒", GoldRewards)
        TransactionType.CLAIM_REWARD -> Triple("Claimed Bounty", "🎁", NeonEmerald)
        TransactionType.MINT -> Triple("Minted", "✨", BaseBlue)
        TransactionType.CONTRACT_CALL -> Triple("Contract Call", "⚙️", BaseCyan)
    }

    val dateFormatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(transaction.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = typeLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val prefix = if (transaction.type == TransactionType.TRANSFER_IN || transaction.type == TransactionType.CLAIM_REWARD) "+" else "-"
            Text(
                text = "$prefix${transaction.value} ${transaction.tokenSymbol}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gas $%.3f".format(transaction.gasFeeUsd),
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.status) {
                                TransactionStatus.SUCCESS -> NeonEmerald
                                TransactionStatus.PENDING -> GoldRewards
                                TransactionStatus.FAILED -> DangerCrimson
                            }
                        )
                )
            }
        }
    }
}
