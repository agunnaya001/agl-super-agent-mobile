package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantPurple

/**
 * Authentic Token Brand Asset Logo Composable.
 * Renders distinct, polished token logos with real asset branding colors and symbols:
 * - AGL: Agunnaya Labs AI Star Node (Glowing Base Blue & Cyan)
 * - wAGL: Wrapped AGL Governance Shield (Base Cyan & Gold)
 * - CREDITS: AI Compute Credits (Gold & Orange Lightning)
 * - ETH: Ethereum Base Diamond (Radiant Purple & Electric Blue)
 * - USDC: Circle USD Coin (Neon Emerald & Deep Blue)
 * - AERO: Aerodrome Finance LP Wing (Base Blue & Green Gradient)
 */
@Composable
fun TokenLogoComponent(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val uppercaseSymbol = symbol.uppercase()

    val (bgBrush, iconColor, borderColor) = when (uppercaseSymbol) {
        "AGL" -> Triple(
            Brush.radialGradient(listOf(BaseBlue, Color(0xFF0038FF))),
            BaseCyan,
            BaseCyan.copy(alpha = 0.6f)
        )
        "WAGL" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF0052FF), RadiantPurple)),
            GoldRewards,
            GoldRewards.copy(alpha = 0.6f)
        )
        "CREDITS", "AGLCREDITS" -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFF8C00), GoldRewards)),
            Color.White,
            GoldRewards.copy(alpha = 0.8f)
        )
        "ETH" -> Triple(
            Brush.linearGradient(listOf(RadiantPurple, ElectricBlue)),
            Color.White,
            RadiantPurple.copy(alpha = 0.6f)
        )
        "USDC" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF2775CA), NeonEmerald)),
            Color.White,
            NeonEmerald.copy(alpha = 0.6f)
        )
        "AERO" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF00E6A0), Color(0xFF0052FF))),
            Color.White,
            Color(0xFF00E6A0)
        )
        else -> Triple(
            Brush.linearGradient(listOf(Color(0xFF2C303E), Color(0xFF1E222D))),
            BaseCyan,
            BaseCyan.copy(alpha = 0.3f)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgBrush)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (uppercaseSymbol) {
            "AGL" -> Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AGL Token",
                tint = iconColor,
                modifier = Modifier.size(size * 0.55f)
            )
            "WAGL" -> Icon(
                imageVector = Icons.Default.HowToVote,
                contentDescription = "wAGL Token",
                tint = iconColor,
                modifier = Modifier.size(size * 0.55f)
            )
            "CREDITS", "AGLCREDITS" -> Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "AGL Credits",
                tint = iconColor,
                modifier = Modifier.size(size * 0.6f)
            )
            "ETH" -> Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Ethereum",
                tint = iconColor,
                modifier = Modifier.size(size * 0.55f)
            )
            "USDC" -> Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "USD Coin",
                tint = iconColor,
                modifier = Modifier.size(size * 0.55f)
            )
            "AERO" -> Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = "Aerodrome",
                tint = iconColor,
                modifier = Modifier.size(size * 0.55f)
            )
            else -> Text(
                text = uppercaseSymbol.take(2),
                fontSize = (size.value * 0.35f).sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
        }
    }
}
