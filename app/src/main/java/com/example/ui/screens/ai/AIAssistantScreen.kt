package com.example.ui.screens.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.model.AiPersona
import com.example.data.model.AiSuggestion
import com.example.data.model.AiSuggestionCategory
import com.example.data.model.SecurityRiskReport
import com.example.data.model.SmartContractDetails
import com.example.data.remote.AiGroundedResponse
import com.example.ui.components.AiFollowUpSuggestionsRow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkNav
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AiSubTab
import com.example.util.VoiceAssistantHelper

@Composable
fun AIAssistantScreen(
    currentTab: AiSubTab,
    onTabSelected: (AiSubTab) -> Unit,
    messages: List<ChatMessageEntity>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    // Persona & Grounding
    selectedPersona: AiPersona = AiPersona.DEFI_STRATEGIST,
    onSelectPersona: (AiPersona) -> Unit = {},
    isSearchGroundingEnabled: Boolean = true,
    onToggleSearchGrounding: (Boolean) -> Unit = {},
    // Contract Analyzer props
    contractResult: SmartContractDetails?,
    isAnalyzingContract: Boolean,
    onAnalyzeContract: (String) -> Unit,
    deepAuditResult: String? = null,
    isDeepAuditing: Boolean = false,
    onDeepAudit: (String, Boolean) -> Unit = { _, _ -> },
    // Security Audit props
    securityReport: SecurityRiskReport?,
    isAuditingSecurity: Boolean,
    onAuditSecurity: (String) -> Unit,
    // DeFi Portfolio Rebalance props
    portfolioPlan: String? = null,
    isGeneratingPortfolioPlan: Boolean = false,
    onGeneratePortfolioPlan: () -> Unit = {},
    // Market Radar props
    marketRadar: AiGroundedResponse? = null,
    isLoadingMarketRadar: Boolean = false,
    onFetchMarketRadar: (String) -> Unit = {},
    // Suggestions
    suggestions: List<AiSuggestion> = emptyList(),
    selectedSuggestionCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    onSelectSuggestionCategory: (AiSuggestionCategory) -> Unit = {},
    followUpSuggestions: List<String> = emptyList(),
    onApplySuggestion: (AiSuggestion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceHelper = remember { VoiceAssistantHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // AI Sub-tabs (Scrollable for rich multi-agent tabs)
        ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = DarkNav,
            contentColor = BaseCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (currentTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = BaseCyan,
                        height = 3.dp
                    )
                }
            },
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = currentTab == AiSubTab.CHAT,
                onClick = { onTabSelected(AiSubTab.CHAT) },
                text = {
                    Text(
                        text = "🤖 AI Chat",
                        fontWeight = if (currentTab == AiSubTab.CHAT) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_chat")
            )
            Tab(
                selected = currentTab == AiSubTab.PORTFOLIO_REBALANCE,
                onClick = {
                    onTabSelected(AiSubTab.PORTFOLIO_REBALANCE)
                    if (portfolioPlan == null && !isGeneratingPortfolioPlan) {
                        onGeneratePortfolioPlan()
                    }
                },
                text = {
                    Text(
                        text = "📊 DeFi Rebalance",
                        fontWeight = if (currentTab == AiSubTab.PORTFOLIO_REBALANCE) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_rebalance")
            )
            Tab(
                selected = currentTab == AiSubTab.CONTRACT_ANALYZER,
                onClick = { onTabSelected(AiSubTab.CONTRACT_ANALYZER) },
                text = {
                    Text(
                        text = "🧠 Code & Audits",
                        fontWeight = if (currentTab == AiSubTab.CONTRACT_ANALYZER) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_contracts")
            )
            Tab(
                selected = currentTab == AiSubTab.SECURITY_AUDIT,
                onClick = { onTabSelected(AiSubTab.SECURITY_AUDIT) },
                text = {
                    Text(
                        text = "🛡️ Drainer Sentinel",
                        fontWeight = if (currentTab == AiSubTab.SECURITY_AUDIT) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_security")
            )
            Tab(
                selected = currentTab == AiSubTab.MARKET_RADAR,
                onClick = {
                    onTabSelected(AiSubTab.MARKET_RADAR)
                    if (marketRadar == null && !isLoadingMarketRadar) {
                        onFetchMarketRadar("Base L2 crypto ecosystem trends")
                    }
                },
                text = {
                    Text(
                        text = "🌐 Live Base Radar",
                        fontWeight = if (currentTab == AiSubTab.MARKET_RADAR) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_radar")
            )
        }

        when (currentTab) {
            AiSubTab.CHAT -> {
                AIChatContent(
                    messages = messages,
                    isThinking = isThinking,
                    selectedPersona = selectedPersona,
                    onSelectPersona = onSelectPersona,
                    isSearchGroundingEnabled = isSearchGroundingEnabled,
                    onToggleSearchGrounding = onToggleSearchGrounding,
                    onSendMessage = onSendMessage,
                    onClearChat = onClearChat,
                    suggestions = suggestions,
                    selectedCategory = selectedSuggestionCategory,
                    onSelectCategory = onSelectSuggestionCategory,
                    followUpSuggestions = followUpSuggestions,
                    onApplySuggestion = onApplySuggestion,
                    voiceHelper = voiceHelper,
                    isSpeaking = isSpeaking
                )
            }
            AiSubTab.PORTFOLIO_REBALANCE -> {
                DeFiPortfolioRebalanceContent(
                    planText = portfolioPlan,
                    isGenerating = isGeneratingPortfolioPlan,
                    onRegenerate = onGeneratePortfolioPlan,
                    voiceHelper = voiceHelper,
                    isSpeaking = isSpeaking
                )
            }
            AiSubTab.CONTRACT_ANALYZER -> {
                SmartContractDeepInspectorContent(
                    quickResult = contractResult,
                    isQuickAnalyzing = isAnalyzingContract,
                    onQuickAnalyze = onAnalyzeContract,
                    deepResult = deepAuditResult,
                    isDeepAuditing = isDeepAuditing,
                    onDeepAudit = onDeepAudit,
                    voiceHelper = voiceHelper,
                    isSpeaking = isSpeaking
                )
            }
            AiSubTab.SECURITY_AUDIT -> {
                SecurityAuditContent(
                    report = securityReport,
                    isAuditing = isAuditingSecurity,
                    onAudit = onAuditSecurity
                )
            }
            AiSubTab.MARKET_RADAR -> {
                BaseMarketRadarContent(
                    radarData = marketRadar,
                    isLoading = isLoadingMarketRadar,
                    onRefreshTopic = onFetchMarketRadar,
                    voiceHelper = voiceHelper,
                    isSpeaking = isSpeaking
                )
            }
        }
    }
}

@Composable
fun AIChatContent(
    messages: List<ChatMessageEntity>,
    isThinking: Boolean,
    selectedPersona: AiPersona,
    onSelectPersona: (AiPersona) -> Unit,
    isSearchGroundingEnabled: Boolean,
    onToggleSearchGrounding: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    suggestions: List<AiSuggestion> = emptyList(),
    selectedCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    onSelectCategory: (AiSuggestionCategory) -> Unit = {},
    followUpSuggestions: List<String> = emptyList(),
    onApplySuggestion: (AiSuggestion) -> Unit = {},
    voiceHelper: VoiceAssistantHelper,
    isSpeaking: Boolean
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredSuggestions = if (selectedCategory == AiSuggestionCategory.ALL) {
        suggestions
    } else {
        suggestions.filter { it.category == selectedCategory }
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Persona Selector Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(AiPersona.values()) { persona ->
                val isSelected = persona == selectedPersona
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BaseBlue.copy(alpha = 0.25f) else DarkCard)
                        .border(
                            1.dp,
                            if (isSelected) BaseCyan else DarkBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectPersona(persona) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(persona.emoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            persona.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BaseCyan else TextSecondary
                        )
                    }
                }
            }
        }

        // Grounding Toggle & Active Agent Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSearchGroundingEnabled) BaseBlue.copy(alpha = 0.15f) else DarkCard)
                    .border(
                        1.dp,
                        if (isSearchGroundingEnabled) BaseCyan.copy(alpha = 0.4f) else DarkBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleSearchGrounding(!isSearchGroundingEnabled) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Search Grounding",
                    tint = if (isSearchGroundingEnabled) BaseCyan else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSearchGroundingEnabled) "🌐 Google Search: ON" else "Search Grounding: OFF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSearchGroundingEnabled) BaseCyan else TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSpeaking) {
                    IconButton(
                        onClick = { voiceHelper.stop() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Voice",
                            tint = DangerCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(28.dp).testTag("button_clear_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Suggestions Category Filter Row
        if (suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(AiSuggestionCategory.values()) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(
                                text = "${category.emoji} ${category.label}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCard,
                            labelColor = TextSecondary,
                            selectedContainerColor = BaseBlue.copy(alpha = 0.25f),
                            selectedLabelColor = BaseCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkBorder,
                            selectedBorderColor = BaseCyan,
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onSpeak = { voiceHelper.speak(msg.text) }
                )
            }

            if (isThinking) {
                item {
                    AiThinkingBubble(persona = selectedPersona)
                }
            }

            // Follow-up Suggestions
            if (followUpSuggestions.isNotEmpty() && !isThinking) {
                item {
                    AiFollowUpSuggestionsRow(
                        followUps = followUpSuggestions,
                        onSelectFollowUp = { prompt -> onSendMessage(prompt) },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Quick Suggestion Chips (when chat has few messages)
        if (messages.size <= 2 && filteredSuggestions.isNotEmpty() && !isThinking) {
            Text(
                text = "⚡ Suggested Web3 Prompts",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSuggestions.take(6)) { sug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onApplySuggestion(sug)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sug.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sug.title,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Ask ${selectedPersona.title} about Base, contracts, gas, yield...",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_ai_chat"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedBorderColor = BaseCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 3,
                trailingIcon = {
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                inputText = ""
                                onSendMessage(text)
                            },
                            modifier = Modifier.testTag("button_send_ai_chat")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = BaseCyan
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: () -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    // Parse grounding sources if available
    val sources: List<Pair<String, String>> = remember(message.sourcesJson) {
        if (message.sourcesJson.isNullOrBlank()) emptyList()
        else {
            message.sourcesJson.split(";;;").mapNotNull { item ->
                val parts = item.split("|||")
                if (parts.size >= 2) Pair(parts[0], parts[1]) else null
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(BaseBlue, BaseCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (message.personaRole) {
                        "security_sentinel" -> "🛡️"
                        "base_alpha" -> "🌐"
                        "gas_optimizer" -> "⛽"
                        "web3_tutor" -> "🎓"
                        else -> "⚡"
                    },
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) BaseBlue.copy(alpha = 0.85f) else DarkCardElevated
                )
                .border(
                    1.dp,
                    if (isUser) BaseBlue else DarkBorder,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            // Model Tag & Role Header
            if (!isUser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AGL Super Agent • ${message.modelUsed ?: "gemini-3.5-flash"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseCyan
                    )

                    Row {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AGL AI Response", message.text)
                                clipboard.setPrimaryClip(clip)
                                copied = true
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copied) NeonEmerald else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = message.text,
                fontSize = 13.sp,
                color = if (isUser) Color.White else TextPrimary,
                lineHeight = 19.sp
            )

            // Render Google Search Grounding Sources
            if (sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🌐 Grounded Web Sources:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BaseCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                sources.forEach { (title, url) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCard)
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Link",
                            tint = BaseCyan,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            color = BaseCyan,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiThinkingBubble(persona: AiPersona) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(BaseBlue, BaseCyan))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(persona.emoji, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCardElevated)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = BaseCyan,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${persona.title} analyzing Base telemetry & grounding with Gemini...",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun DeFiPortfolioRebalanceContent(
    planText: String?,
    isGenerating: Boolean,
    onRegenerate: () -> Unit,
    voiceHelper: VoiceAssistantHelper,
    isSpeaking: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 AI DeFi Portfolio Rebalancer",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Powered by Gemini 3.5 Flash & Base On-Chain Telemetry",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onRegenerate,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Analyze", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BaseCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Formulating optimal yield & staking allocation for Base...",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else if (planText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, BaseCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ AI Yield Allocation Blueprint",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                        IconButton(
                            onClick = {
                                if (isSpeaking) voiceHelper.stop() else voiceHelper.speak(planText)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = if (isSpeaking) DangerCrimson else BaseCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = planText,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmartContractDeepInspectorContent(
    quickResult: SmartContractDetails?,
    isQuickAnalyzing: Boolean,
    onQuickAnalyze: (String) -> Unit,
    deepResult: String?,
    isDeepAuditing: Boolean,
    onDeepAudit: (String, Boolean) -> Unit,
    voiceHelper: VoiceAssistantHelper,
    isSpeaking: Boolean
) {
    var inputCodeOrAddress by remember { mutableStateOf("") }
    var isRawSolidityMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "🧠 Smart Contract & Bytecode Deep Inspector",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Powered by Gemini 3.1 Pro Preview for complex vulnerability reasoning",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode switch: Contract Address vs Raw Solidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRawSolidityMode) "Mode: Paste Solidity Code" else "Mode: Contract Address on Base",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BaseCyan
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Solidity Code", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = isRawSolidityMode,
                    onCheckedChange = { isRawSolidityMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BaseCyan,
                        uncheckedTrackColor = DarkCard
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val clipboardManager = LocalClipboardManager.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sample: AGL Token",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BaseCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BaseBlue.copy(alpha = 0.2f))
                    .clickable {
                        inputCodeOrAddress = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "0xD034...27C8",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BaseCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BaseBlue.copy(alpha = 0.2f))
                    .clickable {
                        inputCodeOrAddress = "0xD034E94465Db1669f80D817c66e58cF194d027C8"
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = inputCodeOrAddress,
            onValueChange = { inputCodeOrAddress = it },
            placeholder = {
                Text(
                    text = if (isRawSolidityMode) "Paste Solidity code here (pragma solidity ^0.8.20; ...)" else "0x... contract address on Base Mainnet",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            },
            trailingIcon = {
                Button(
                    onClick = {
                        val text = clipboardManager.getText()?.text
                        if (!text.isNullOrBlank()) {
                            inputCodeOrAddress = text.trim()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue.copy(alpha = 0.35f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = BaseCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PASTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isRawSolidityMode) 140.dp else 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard,
                focusedBorderColor = BaseCyan,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            maxLines = if (isRawSolidityMode) 10 else 1
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onDeepAudit(inputCodeOrAddress, isRawSolidityMode)
                },
                enabled = !isDeepAuditing && inputCodeOrAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isDeepAuditing) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Deep Audit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deep AI Audit (Pro)", fontSize = 12.sp)
                }
            }

            if (!isRawSolidityMode) {
                OutlinedButton(
                    onClick = { onQuickAnalyze(inputCodeOrAddress) },
                    enabled = !isQuickAnalyzing && inputCodeOrAddress.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isQuickAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = BaseCyan, strokeWidth = 2.dp)
                    } else {
                        Text("Quick RPC Scan", fontSize = 12.sp, color = BaseCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deep Audit Result View
        if (deepResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, BaseCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ Gemini 3.1 Pro Security Audit Report",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                        IconButton(
                            onClick = {
                                if (isSpeaking) voiceHelper.stop() else voiceHelper.speak(deepResult)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = if (isSpeaking) DangerCrimson else BaseCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = deepResult,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BaseMarketRadarContent(
    radarData: AiGroundedResponse?,
    isLoading: Boolean,
    onRefreshTopic: (String) -> Unit,
    voiceHelper: VoiceAssistantHelper,
    isSpeaking: Boolean
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🌐 Live Base Market Radar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time Google Search Grounding for Base Ecosystem",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { onRefreshTopic("Base L2 crypto ecosystem trends") },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Topic Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                listOf(
                    "Base L2 TVL & Volume Trends",
                    "Aerodrome Slipstream Pools",
                    "Base Gas & Blob Fee Dynamics",
                    "Trending Base Tokens & dApps"
                )
            ) { topic ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { onRefreshTopic(topic) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(topic, fontSize = 11.sp, color = BaseCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BaseCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Synthesizing live Base ecosystem web data with Google Search Grounding...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        } else if (radarData != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .border(1.dp, BaseCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 Base Live Briefing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                        IconButton(
                            onClick = {
                                if (isSpeaking) voiceHelper.stop() else voiceHelper.speak(radarData.text)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = if (isSpeaking) DangerCrimson else BaseCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = radarData.text,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    if (radarData.webSources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🌐 Grounded Web Citations:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        radarData.webSources.forEach { source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCardElevated)
                                    .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open",
                                    tint = BaseCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = source.title,
                                    fontSize = 11.sp,
                                    color = BaseCyan,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
