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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.model.AiSuggestion
import com.example.data.model.AiSuggestionCategory
import com.example.data.model.SecurityRiskReport
import com.example.data.model.SmartContractDetails
import com.example.ui.components.AiFollowUpSuggestionsRow
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
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

@Composable
fun AIAssistantScreen(
    currentTab: AiSubTab,
    onTabSelected: (AiSubTab) -> Unit,
    messages: List<ChatMessageEntity>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    // Contract Analyzer props
    contractResult: SmartContractDetails?,
    isAnalyzingContract: Boolean,
    onAnalyzeContract: (String) -> Unit,
    // Security Audit props
    securityReport: SecurityRiskReport?,
    isAuditingSecurity: Boolean,
    onAuditSecurity: (String) -> Unit,
    suggestions: List<AiSuggestion> = emptyList(),
    selectedSuggestionCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    onSelectSuggestionCategory: (AiSuggestionCategory) -> Unit = {},
    followUpSuggestions: List<String> = emptyList(),
    onApplySuggestion: (AiSuggestion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // AI Sub-tabs
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = DarkNav,
            contentColor = BaseCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                    color = BaseCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(10.dp))
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
                selected = currentTab == AiSubTab.CONTRACT_ANALYZER,
                onClick = { onTabSelected(AiSubTab.CONTRACT_ANALYZER) },
                text = {
                    Text(
                        text = "🧠 Contracts",
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
                        text = "🛡️ Security",
                        fontWeight = if (currentTab == AiSubTab.SECURITY_AUDIT) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_ai_security")
            )
        }

        when (currentTab) {
            AiSubTab.CHAT -> {
                AIChatContent(
                    messages = messages,
                    isThinking = isThinking,
                    onSendMessage = onSendMessage,
                    onClearChat = onClearChat,
                    suggestions = suggestions,
                    selectedCategory = selectedSuggestionCategory,
                    onSelectCategory = onSelectSuggestionCategory,
                    followUpSuggestions = followUpSuggestions,
                    onApplySuggestion = onApplySuggestion
                )
            }
            AiSubTab.CONTRACT_ANALYZER -> {
                SmartContractAnalyzerContent(
                    result = contractResult,
                    isAnalyzing = isAnalyzingContract,
                    onAnalyze = onAnalyzeContract
                )
            }
            AiSubTab.SECURITY_AUDIT -> {
                SecurityAuditContent(
                    report = securityReport,
                    isAuditing = isAuditingSecurity,
                    onAudit = onAuditSecurity
                )
            }
        }
    }
}

@Composable
fun AIChatContent(
    messages: List<ChatMessageEntity>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    suggestions: List<AiSuggestion> = emptyList(),
    selectedCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    onSelectCategory: (AiSuggestionCategory) -> Unit = {},
    followUpSuggestions: List<String> = emptyList(),
    onApplySuggestion: (AiSuggestion) -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredSuggestions = if (selectedCategory == AiSuggestionCategory.ALL) {
        suggestions
    } else {
        suggestions.filter { it.category == selectedCategory }
    }

    val fallbackSuggestions = listOf(
        "What is my current AGL balance?",
        "Show my real-time balances",
        "What is my voting power in wAGL?",
        "Explain my last Base transaction",
        "Explain Base L2 gas in simple terms",
        "Audit wallet security risks"
    )

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Header with Clear button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AGL Super Agent Intelligence",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(32.dp).testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear Chat",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(message = msg)
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCard)
                            .border(1.dp, BaseCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = BaseCyan,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Super Agent is analyzing on-chain data...",
                            fontSize = 12.sp,
                            color = BaseCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Inline Follow-up suggestion row right in the chat stream
            if (followUpSuggestions.isNotEmpty() && !isThinking) {
                item {
                    AiFollowUpSuggestionsRow(
                        followUps = followUpSuggestions,
                        onSelectFollowUp = { prompt ->
                            onSendMessage(prompt)
                        }
                    )
                }
            }
        }

        // AI Suggestion category chips bar
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            items(AiSuggestionCategory.entries) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(cat) },
                    label = {
                        Text(
                            text = "${cat.emoji} ${cat.label}",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaseCyan.copy(alpha = 0.2f),
                        selectedLabelColor = BaseCyan,
                        containerColor = DarkCardElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = BaseCyan,
                        borderColor = DarkBorder,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Suggestion prompt chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            if (filteredSuggestions.isNotEmpty()) {
                items(filteredSuggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardElevated)
                            .border(1.dp, BaseCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable {
                                onApplySuggestion(suggestion)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("ai_suggestion_chip_${suggestion.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${suggestion.icon} ${suggestion.title}",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            suggestion.impactTag?.let { tag ->
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BaseCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 9.sp,
                                        color = BaseCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(fallbackSuggestions) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onSendMessage(prompt)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input Field Bar
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
                    Text("Ask anything about Base & AGL...", fontSize = 13.sp, color = TextMuted)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BaseCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        onSendMessage(textToSend)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(BaseBlue, BaseCyan)
                        )
                    )
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isUser = message.sender.equals("USER", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BaseBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤖", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) BaseBlue.copy(alpha = 0.85f) else DarkCardElevated)
                .border(
                    1.dp,
                    if (isUser) BaseCyan.copy(alpha = 0.4f) else DarkBorder,
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
