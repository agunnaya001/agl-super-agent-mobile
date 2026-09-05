package com.example.ui.screens.governance

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.blockchain.abi.GovernorAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.services.GovernorDetails
import com.example.data.remote.blockchain.services.LiveWalletState
import com.example.data.remote.blockchain.services.ProposalInfo
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
fun GovernanceScreen(
    governorDetails: GovernorDetails?,
    proposals: List<ProposalInfo>,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onVote: (proposalId: BigInteger, support: Int) -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val daoName = governorDetails?.name ?: "Agunnaya DAO Governor"
    val quorum = governorDetails?.formattedQuorum ?: "40,000 wAGL"
    val votingPower = walletState?.formattedVotingPower ?: "0.00"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("governance_screen")
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
                    text = "Agunnaya DAO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // DAO Overview Card
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
                                    .background(BaseBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "DAO", tint = BaseCyan, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = daoName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Base Mainnet On-Chain Governance",
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
                            Text("Quorum Active", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Your Voting Power", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$votingPower Votes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = BaseCyan
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Quorum Required", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = quorum,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldRewards
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voting Delay: ${governorDetails?.votingDelayBlocks ?: 43200} blocks", fontSize = 11.sp, color = TextSecondary)
                        Text("Voting Period: ${governorDetails?.votingPeriodBlocks ?: 216000} blocks", fontSize = 11.sp, color = TextSecondary)
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
                            text = BaseBlockchainConfig.GOVERNOR_CONTRACT.take(12) + "..." + BaseBlockchainConfig.GOVERNOR_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.GOVERNOR_CONTRACT))
                                    onShowSnackbar("Governor address copied")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerAddressUrl(BaseBlockchainConfig.GOVERNOR_CONTRACT)
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

        // Section header
        item {
            Text(
                text = "Proposals (${proposals.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(proposals) { proposal ->
            val proposalIdBigInt = proposal.id.toBigIntegerOrNull() ?: BigInteger.ONE
            val stateColor = when (proposal.state) {
                GovernorAbi.ProposalState.ACTIVE -> NeonEmerald
                GovernorAbi.ProposalState.SUCCEEDED -> BaseCyan
                GovernorAbi.ProposalState.QUEUED -> GoldRewards
                GovernorAbi.ProposalState.EXECUTED -> Color(0xFF9E9E9E)
                GovernorAbi.ProposalState.DEFEATED -> NeonRose
                else -> TextMuted
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proposal #${proposal.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(stateColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = proposal.state.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = stateColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = proposal.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = proposal.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vote tallies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("For: ${proposal.forVotes}", fontSize = 12.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        Text("Against: ${proposal.againstVotes}", fontSize = 12.sp, color = NeonRose, fontWeight = FontWeight.Bold)
                        Text("Abstain: ${proposal.abstainVotes}", fontSize = 12.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 0.85f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonEmerald,
                        trackColor = DarkBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (proposal.state == GovernorAbi.ProposalState.ACTIVE) {
                        Text("Cast Your Ballot:", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onVote(proposalIdBigInt, 1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vote_for_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "For", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FOR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onVote(proposalIdBigInt, 0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vote_against_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Against", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AGAINST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onVote(proposalIdBigInt, 2) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vote_abstain_button")
                            ) {
                                Text("ABSTAIN", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    } else if (proposal.state == GovernorAbi.ProposalState.QUEUED) {
                        Button(
                            onClick = {
                                onShowSnackbar("Executing queued proposal on Timelock...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldRewards)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute Queued Proposal", color = Color.Black, fontWeight = FontWeight.Bold)
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
