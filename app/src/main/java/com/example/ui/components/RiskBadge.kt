package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskHighBorder
import com.example.ui.theme.RiskLowBg
import com.example.ui.theme.RiskLowBorder
import com.example.ui.theme.RiskReviewBg
import com.example.ui.theme.RiskReviewBorder

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, label, dotColor) = when (riskLevel) {
        RiskLevel.LOW_CONCERN -> {
            Tuple5(RiskLowBg, RiskLowBorder, NeonEmerald, "LOW CONCERN", NeonEmerald)
        }
        RiskLevel.REVIEW -> {
            Tuple5(RiskReviewBg, RiskReviewBorder, AmberWarning, "REVIEW REQUIRED", AmberWarning)
        }
        RiskLevel.HIGH_CONCERN -> {
            Tuple5(RiskHighBg, RiskHighBorder, DangerCrimson, "HIGH CONCERN", DangerCrimson)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
