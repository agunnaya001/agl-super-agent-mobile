package com.example.ui.screens.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskFlag
import com.example.data.model.RiskLevel
import com.example.data.model.SecurityRiskReport
import com.example.data.remote.BlockchainService
import com.example.ui.components.RiskBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.VoiceAssistantHelper

@Composable
fun SecurityAuditContent(
    report: SecurityRiskReport?,
    isAuditing: Boolean,
    onAudit: (String) -> Unit,
    voiceHelper: VoiceAssistantHelper? = null,
    isSpeaking: Boolean = false
) {
    var contractInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val sampleContracts = listOf(
        Pair("AGL Token", BlockchainService.AGL_TOKEN_CONTRACT),
        Pair("wAGL Gov", BlockchainService.AGL_VOTES_WRAPPER_CONTRACT),
        Pair("DAO Timelock", BlockchainService.TIMELOCK_CONTRACT),
        Pair("AGL Credits", BlockchainService.AGL_CREDITS_CONTRACT),
        Pair("Aerodrome Slipstream", "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = BaseCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Smart Contract Security Audit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Paste any smart contract address on Base Mainnet (Chain ID 8453) to run an AI-powered security audit and vulnerability summary.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }

        item {
            // Input Target Field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Base Contract Address",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BaseCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = contractInput,
                    onValueChange = { contractInput = it },
                    placeholder = {
                        Text("0x... contract address on Base", fontSize = 12.sp, color = TextMuted)
                    },
                    trailingIcon = {
                        Button(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    contractInput = text.trim()
                                    Toast.makeText(context, "Address pasted from clipboard", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue.copy(alpha = 0.35f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("audit_paste_button")
                        ) {
                            Icon(
                                Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = BaseCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PASTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("security_target_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BaseCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets Row
                Text(
                    text = "Verified Base Contracts:",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleContracts) { (name, address) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkCardElevated)
                                .border(0.5.dp, BaseCyan.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                .clickable {
                                    contractInput = address
                                    onAudit(address)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                color = BaseCyan,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onAudit(contractInput) },
                    enabled = contractInput.isNotBlank() && !isAuditing,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("audit_security_submit_button")
                ) {
                    if (isAuditing) {
                        CircularProgressIndicator(
                            color = DarkBackground,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auditing Security on Base...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBackground
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Run AI Security Audit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkBackground
                        )
                    }
                }
            }
        }

        if (report != null) {
            item {
                // Report Header Card with Score
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_score_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (report.riskLevel) {
                            RiskLevel.LOW_CONCERN -> NeonEmerald.copy(alpha = 0.4f)
                            RiskLevel.REVIEW -> AmberWarning.copy(alpha = 0.4f)
                            RiskLevel.HIGH_CONCERN -> DangerCrimson.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Executive Safety Score",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${report.riskScore}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (report.riskLevel) {
                                            RiskLevel.LOW_CONCERN -> NeonEmerald
                                            RiskLevel.REVIEW -> AmberWarning
                                            RiskLevel.HIGH_CONCERN -> DangerCrimson
                                        }
                                    )
                                    Text(
                                        text = " / 100",
                                        fontSize = 14.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            RiskBadge(riskLevel = report.riskLevel)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = report.summary,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // AI Vulnerability Summary Report Card
            if (!report.aiVulnerabilitySummary.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_vulnerability_summary_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = BaseCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Security Vulnerability Summary",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BaseCyan
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Audio TTS read-aloud button
                                    if (voiceHelper != null) {
                                        IconButton(
                                            onClick = {
                                                if (isSpeaking) {
                                                    voiceHelper.stop()
                                                } else {
                                                    voiceHelper.speak(report.aiVulnerabilitySummary)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                contentDescription = "Read Aloud",
                                                tint = if (isSpeaking) DangerCrimson else BaseCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Copy report button
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(report.aiVulnerabilitySummary))
                                            Toast.makeText(context, "Audit report copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Audit",
                                            tint = BaseCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = report.aiVulnerabilitySummary,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                // Flags & Threat Vectors
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Vulnerability Vectors & Flags (${report.flags.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.flags.forEach { flag ->
                            RiskFlagRow(flag = flag)
                        }
                    }
                }
            }

            item {
                // Recommendations
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Actionable Security Recommendations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    report.recommendations.forEach { rec ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rec,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Web3 Safety Principles Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BaseCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Super Agent Non-Custodial Verification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The AGL Super Agent operates completely non-custodially. It inspects Base Mainnet contract bytecode and execution logic without ever accessing your private keys or seed phrases.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RiskFlagRow(flag: RiskFlag) {
    val (icon, color) = when (flag.severity) {
        RiskLevel.LOW_CONCERN -> Pair(Icons.Default.GppGood, NeonEmerald)
        RiskLevel.REVIEW -> Pair(Icons.Default.GppMaybe, AmberWarning)
        RiskLevel.HIGH_CONCERN -> Pair(Icons.Default.ReportProblem, DangerCrimson)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardElevated)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = flag.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = flag.description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
