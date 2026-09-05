package com.example.ui.screens.governance

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.math.BigInteger

/**
 * Full-featured on-chain Governance Dashboard for Agunnaya DAO on Base Mainnet.
 * Pulls live proposal status from the Governor and AGL/wAGL contracts,
 * tracks quorum metrics, and provides an interactive on-chain voting interface.
 */
@Composable
fun GovernanceScreen(
    governorDetails: GovernorDetails?,
    proposals: List<ProposalInfo>,
    walletState: LiveWalletState?,
    onBack: () -> Unit,
    onVote: (proposalId: BigInteger, support: Int, reason: String?) -> Unit,
    onStartTx: (TxPipelineRequest) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onRefresh: () -> Unit = {},
    onNavigateToWagL: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val daoName = governorDetails?.name ?: "Agunnaya DAO Governor"
    val quorum = governorDetails?.formattedQuorum ?: "40,000 wAGL"
    val votingPower = walletState?.formattedVotingPower ?: "0.00"
    val userAddress = walletState?.address ?: BaseBlockchainConfig.DEFAULT_DEMO_WALLET

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeBallotProposal by remember { mutableStateOf<ProposalInfo?>(null) }
    var expandedProposalId by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Filter proposals based on selected tab
    val filteredProposals = remember(proposals, selectedTab) {
        when (selectedTab) {
            1 -> proposals.filter { it.state == GovernorAbi.ProposalState.ACTIVE }
            2 -> proposals.filter { it.state == GovernorAbi.ProposalState.SUCCEEDED }
            3 -> proposals.filter { it.state == GovernorAbi.ProposalState.QUEUED }
            4 -> proposals.filter { it.state == GovernorAbi.ProposalState.EXECUTED }
            else -> proposals
        }
    }

    val activeCount = proposals.count { it.state == GovernorAbi.ProposalState.ACTIVE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("governance_screen")
    ) {
        // App Bar & Live Status
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Agunnaya DAO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Base Mainnet • On-Chain Governance",
                            fontSize = 11.sp,
                            color = BaseCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CHAIN ID: 8453", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                        }
                    }

                    IconButton(
                        onClick = {
                            isRefreshing = true
                            onRefresh()
                            isRefreshing = false
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BaseCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // DAO Overview Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
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
                                    text = "OpenZeppelin Governor v4",
                                    fontSize = 11.sp,
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
                            Text(
                                text = "$activeCount Active Vote${if (activeCount != 1) "s" else ""}",
                                fontSize = 11.sp,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voting Power & Quorum Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Your Voting Power Pill
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Your Voting Power", fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$votingPower wAGL",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BaseCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (votingPower == "0.00" || votingPower == "0") "Undelegated" else "Active Weight",
                                    fontSize = 10.sp,
                                    color = if (votingPower == "0.00" || votingPower == "0") GoldRewards else NeonEmerald
                                )
                            }
                        }

                        // Quorum Pill
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = GoldRewards, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quorum Required", fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = quorum,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldRewards
                                )
                                Text(
                                    text = "4% of Voting Supply",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voting Parameters & Timelock Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voting Delay: ${governorDetails?.votingDelayBlocks ?: 43200} blocks (~24h)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Period: ${governorDetails?.votingPeriodBlocks ?: 216000} blocks (~5d)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Zero Voting Power Prompt Banner
                    if (votingPower == "0.00" || votingPower == "0") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldRewards.copy(alpha = 0.12f))
                                .border(1.dp, GoldRewards.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("No voting weight detected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldRewards)
                                Text("Wrap AGL to wAGL or delegate voting power to vote on-chain.", fontSize = 10.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onNavigateToWagL,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldRewards),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Wrap wAGL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contract address row with BaseScan link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Governor: " + BaseBlockchainConfig.GOVERNOR_CONTRACT.take(8) + "..." + BaseBlockchainConfig.GOVERNOR_CONTRACT.takeLast(6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BaseBlockchainConfig.GOVERNOR_CONTRACT))
                                    onShowSnackbar("Governor address copied to clipboard")
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BaseCyan, modifier = Modifier.size(13.dp))
                            }
                            IconButton(
                                onClick = {
                                    val url = BaseBlockchainConfig.getExplorerAddressUrl(BaseBlockchainConfig.GOVERNOR_CONTRACT)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.NorthEast, contentDescription = "Basescan", tint = TextSecondary, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Filter Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = BaseCyan,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (selectedTab in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BaseCyan,
                            height = 2.dp
                        )
                    }
                },
                divider = {}
            ) {
                listOf("All (${proposals.size})", "Active ($activeCount)", "Succeeded", "Queued", "Executed").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) BaseCyan else TextSecondary
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Proposal Cards
        if (filteredProposals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HowToVote, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No proposals found in this category", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredProposals) { proposal ->
                val proposalIdBigInt = proposal.id.toBigIntegerOrNull() ?: BigInteger.ONE
                val isExpanded = expandedProposalId == proposal.id

                val stateColor = when (proposal.state) {
                    GovernorAbi.ProposalState.ACTIVE -> NeonEmerald
                    GovernorAbi.ProposalState.SUCCEEDED -> BaseCyan
                    GovernorAbi.ProposalState.QUEUED -> GoldRewards
                    GovernorAbi.ProposalState.EXECUTED -> Color(0xFF9E9E9E)
                    GovernorAbi.ProposalState.DEFEATED -> NeonRose
                    else -> TextMuted
                }

                // Calculate vote percentages
                val totalVotes = proposal.forVotesRaw + proposal.againstVotesRaw + proposal.abstainVotesRaw
                val totalDouble = totalVotes.toDouble().coerceAtLeast(1.0)
                val forPct = ((proposal.forVotesRaw.toDouble() / totalDouble) * 100).toFloat().coerceIn(0f, 100f)
                val againstPct = ((proposal.againstVotesRaw.toDouble() / totalDouble) * 100).toFloat().coerceIn(0f, 100f)
                val abstainPct = ((proposal.abstainVotesRaw.toDouble() / totalDouble) * 100).toFloat().coerceIn(0f, 100f)

                val quorumMet = (proposal.forVotesRaw + proposal.abstainVotesRaw) >= proposal.quorumVotes

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("proposal_card_${proposal.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (proposal.state == GovernorAbi.ProposalState.ACTIVE) BaseCyan.copy(alpha = 0.4f) else DarkBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Proposal Tag & State Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BaseBlue.copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("AGIP #${proposal.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "End Block: ${proposal.endBlock}",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Proposal Title
                        Text(
                            text = proposal.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Proposal Description
                        Text(
                            text = proposal.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vote breakdown statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonEmerald))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("For: ${proposal.forVotes} (%.1f%%)".format(forPct), fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonRose))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Against: ${proposal.againstVotes} (%.1f%%)".format(againstPct), fontSize = 11.sp, color = NeonRose, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextMuted))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abstain: ${proposal.abstainVotes}", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tri-color Segmented Progress Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkBorder)
                        ) {
                            if (forPct > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(forPct.coerceAtLeast(0.01f))
                                        .height(8.dp)
                                        .background(NeonEmerald)
                                )
                            }
                            if (againstPct > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(againstPct.coerceAtLeast(0.01f))
                                        .height(8.dp)
                                        .background(NeonRose)
                                )
                            }
                            if (abstainPct > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(abstainPct.coerceAtLeast(0.01f))
                                        .height(8.dp)
                                        .background(Color.Gray)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quorum Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (quorumMet) "✓ Quorum Reached" else "Quorum in progress",
                                fontSize = 10.sp,
                                color = if (quorumMet) NeonEmerald else GoldRewards,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (isExpanded) "Less details" else "Inspect on-chain actions",
                                fontSize = 10.sp,
                                color = BaseCyan,
                                modifier = Modifier.clickable {
                                    expandedProposalId = if (isExpanded) null else proposal.id
                                }
                            )
                        }

                        // Expanded Execution Payload Details
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground)
                                    .padding(10.dp)
                            ) {
                                Text("Execution Payload:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Proposer: ${proposal.proposer}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                if (proposal.targets.isNotEmpty()) {
                                    Text("Target: ${proposal.targets.first()}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                }
                                Text("Calldata: ${proposal.calldatasSummary}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = BaseCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Voting Interface
                        if (proposal.state == GovernorAbi.ProposalState.ACTIVE) {
                            if (proposal.hasVoted) {
                                // Already voted badge
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonEmerald.copy(alpha = 0.12f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ballot Cast On-Chain by Active Wallet",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonEmerald
                                    )
                                }
                            } else {
                                // Cast Ballot Call to Action
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Cast Your Ballot:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Weight: $votingPower wAGL", fontSize = 11.sp, color = BaseCyan)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick Vote Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onVote(proposalIdBigInt, 1, null)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("vote_for_${proposal.id}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "For", tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("FOR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onVote(proposalIdBigInt, 0, null)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("vote_against_${proposal.id}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Against", tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AGAINST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                onVote(proposalIdBigInt, 2, null)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("vote_abstain_${proposal.id}"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("ABSTAIN", fontSize = 10.sp, color = TextSecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Advanced vote dialog trigger
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeBallotProposal = proposal }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Vote with statement / reason...",
                                            fontSize = 11.sp,
                                            color = BaseCyan
                                        )
                                    }
                                }
                            }
                        } else if (proposal.state == GovernorAbi.ProposalState.SUCCEEDED) {
                            Button(
                                onClick = {
                                    val req = TxPipelineRequest(
                                        title = "Queue Proposal #${proposal.id}",
                                        description = "Queue proposal #${proposal.id} into Timelock Controller",
                                        targetContract = BaseBlockchainConfig.GOVERNOR_CONTRACT,
                                        tokenRequired = null,
                                        calldata = GovernorAbi.encodeQueue(proposalIdBigInt)
                                    )
                                    onStartTx(req)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = "Queue", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Queue Proposal to Timelock", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else if (proposal.state == GovernorAbi.ProposalState.QUEUED) {
                            Button(
                                onClick = {
                                    val req = TxPipelineRequest(
                                        title = "Execute Proposal #${proposal.id}",
                                        description = "Execute proposal #${proposal.id} through Timelock Controller",
                                        targetContract = BaseBlockchainConfig.GOVERNOR_CONTRACT,
                                        tokenRequired = null,
                                        calldata = GovernorAbi.encodeExecute(proposalIdBigInt)
                                    )
                                    onStartTx(req)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldRewards),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Proposal On-Chain", color = Color.Black, fontWeight = FontWeight.Bold)
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

    // Comprehensive Ballot Casting Dialog with Reason
    activeBallotProposal?.let { targetProposal ->
        val targetIdBigInt = targetProposal.id.toBigIntegerOrNull() ?: BigInteger.ONE
        var selectedSupport by remember { mutableIntStateOf(1) } // Default: For
        var voteReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeBallotProposal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cast On-Chain Ballot", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Proposal #${targetProposal.id}: ${targetProposal.title}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voting power badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BaseBlue.copy(alpha = 0.2f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Voting Weight:", fontSize = 12.sp, color = TextMuted)
                        Text("$votingPower wAGL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Select Your Vote:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Preference Selection Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // FOR
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSupport = 1 },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSupport == 1) NeonEmerald.copy(alpha = 0.2f) else DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedSupport == 1) NeonEmerald else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "For", tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("FOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                            }
                        }

                        // AGAINST
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSupport = 0 },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSupport == 0) NeonRose.copy(alpha = 0.2f) else DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedSupport == 0) NeonRose else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ThumbDown, contentDescription = "Against", tint = NeonRose, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("AGAINST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonRose)
                            }
                        }

                        // ABSTAIN
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSupport = 2 },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSupport == 2) Color.White.copy(alpha = 0.15f) else DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedSupport == 2) Color.White else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Abstain", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ABSTAIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Statement / Reason input
                    OutlinedTextField(
                        value = voteReason,
                        onValueChange = { voteReason = it },
                        label = { Text("Ballot Statement (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("Reason for voting preference...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BaseCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your ballot will be recorded on Base Mainnet directly via Governor contract.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = voteReason.trim().ifBlank { null }
                        onVote(targetIdBigInt, selectedSupport, reason)
                        activeBallotProposal = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedSupport) {
                            1 -> NeonEmerald
                            0 -> NeonRose
                            else -> BaseCyan
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val label = when (selectedSupport) {
                        1 -> "Confirm Vote FOR"
                        0 -> "Confirm Vote AGAINST"
                        else -> "Confirm ABSTAIN"
                    }
                    Text(label, color = if (selectedSupport == 0) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { activeBallotProposal = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
