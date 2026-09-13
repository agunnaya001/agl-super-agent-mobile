package com.example.ui.screens.ai

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractFunction
import com.example.data.model.SmartContractDetails
import com.example.data.remote.BlockchainService
import com.example.ui.components.RiskBadge
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

@Composable
fun SmartContractAnalyzerContent(
    result: SmartContractDetails?,
    isAnalyzing: Boolean,
    onAnalyze: (String) -> Unit
) {
    var contractInput by remember { mutableStateOf("") }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val sampleContracts = listOf(
        Pair("AGL Token", BlockchainService.AGL_TOKEN_CONTRACT),
        Pair("AGL Credits", BlockchainService.AGL_CREDITS_CONTRACT),
        Pair("Votes Wrapper", BlockchainService.AGL_VOTES_WRAPPER_CONTRACT),
        Pair("DAO Governor", BlockchainService.GOVERNOR_CONTRACT),
        Pair("Timelock", BlockchainService.TIMELOCK_CONTRACT)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Smart Contract Intelligence",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Inspect Base contract ABIs, verified status, proxy patterns, and risk factors.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            // Input Address Field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = contractInput,
                    onValueChange = { contractInput = it },
                    placeholder = {
                        Text("Paste Base contract address (0x...)", fontSize = 12.sp, color = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contract_address_input"),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sample chip buttons
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sampleContracts) { (name, addr) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkCardElevated)
                                    .clickable {
                                        contractInput = addr
                                        onAnalyze(addr)
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onAnalyze(contractInput) },
                        enabled = contractInput.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("scan_contract_submit_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Scan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (result != null) {
            item {
                // Analysis Result Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = result.network,
                                    fontSize = 11.sp,
                                    color = BaseCyan
                                )
                            }
                            RiskBadge(riskLevel = result.securityRisk)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = result.summaryExplanation,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusTag(
                                label = if (result.isVerified) "Verified Source" else "Unverified Bytecode",
                                isGood = result.isVerified
                            )
                            StatusTag(
                                label = if (result.isProxy) "Proxy Contract" else "Direct Implementation",
                                isGood = !result.isProxy
                            )
                        }
                    }
                }
            }

            item {
                // Security Reasons breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Security Inspection Audit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.securityReasons.forEach { reason ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", color = BaseCyan, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            item {
                // Write functions (state-changing)
                FunctionSection(
                    title = "State Modifying Functions (${result.writeFunctions.size})",
                    functions = result.writeFunctions
                )
            }

            item {
                // Read functions
                FunctionSection(
                    title = "View & Read Functions (${result.readFunctions.size})",
                    functions = result.readFunctions
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatusTag(label: String, isGood: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isGood) NeonEmerald.copy(alpha = 0.15f) else DangerCrimson.copy(alpha = 0.15f))
            .border(1.dp, if (isGood) NeonEmerald.copy(alpha = 0.3f) else DangerCrimson.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGood) NeonEmerald else DangerCrimson
        )
    }
}

@Composable
fun FunctionSection(
    title: String,
    functions: List<ContractFunction>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            functions.forEach { fn ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCardElevated)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = fn.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (fn.isDangerous) DangerCrimson else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            if (fn.isDangerous) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Dangerous",
                                    tint = DangerCrimson,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = fn.description,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
