package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class SoulboundNft(
    val title: String,
    val description: String,
    val levelReq: String,
    val iconEmoji: String,
    val isClaimed: Boolean
)

/**
 * Viral Marketing, Farcaster Frame & Soulbound Achievement NFT Modal.
 * Promotes viral growth of AGL token and ecosystem on Base Mainnet.
 */
@Composable
fun ViralSocialShareModal(
    walletAddress: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf(false) }

    val frameUrl = "https://farcaster.frame.agunnayalabs.io/agent/0xD034E94465Db1669f80D817c66e58cF194d027C8"
    val shareText = "🚀 Check out my AGL Super Agent on Base! Automated DEX arbitrage, 84.5% Aerodrome APY yield & 1-click smart audits. Join the AI revolution on Base Mainnet! \$AGL #Base #AIAgent"

    val soulboundNfts = remember {
        listOf(
            SoulboundNft("Base AI Pioneer", "Granted to early testers on Base Mainnet", "Level 1", "🛡️", true),
            SoulboundNft("Aerodrome LP Master", "Staked in Aerodrome v3 AGL Liquidity Vaults", "Level 5", "🌊", true),
            SoulboundNft("Super Agent Level 24", "Earned 10,000+ XP in Agent Quests", "Level 20", "⚡", false)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("viral_social_share_modal"),
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Modal Header
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
                                .background(Brush.linearGradient(listOf(Color(0xFF8A63D2), BaseBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Viral Growth & Rewards", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Farcaster Frame & Proof-of-Agent Cards", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    ) {
                        Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Farcaster Interactive Frame Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF472A91),
                                    Color(0xFF1E1035)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF8A63D2), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💜 Farcaster Frame Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC4A8FF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF8A63D2).copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Warpcast Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE4D5FF))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Frame Mock Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkBackground)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("AGL Super Agent #8842", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                Text("30-Day Yield: +18.4% ($2,640.00) · Aerodrome LP: Active", fontSize = 11.sp, color = NeonEmerald)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF8A63D2))
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1-Tap Stake AGL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BaseBlue)
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Audit Contract", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val warpcastUrl = "https://warpcast.com/~/compose?text=${Uri.encode(shareText)}&embeds[]=${Uri.encode(frameUrl)}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(warpcastUrl)))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A63D2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Post on Warpcast", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val twitterUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(shareText)}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl)))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Share on X / Twitter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Soulbound Achievement NFTs
                Text("Soulbound Proof-of-Agent NFTs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    soulboundNfts.forEach { nft ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkBackground)
                                .border(1.dp, if (nft.isClaimed) BaseCyan.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(nft.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(nft.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(nft.description, fontSize = 10.sp, color = TextMuted)
                                    }
                                }

                                if (nft.isClaimed) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Claimed on Base", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                    }
                                } else {
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Claim NFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
