package com.example.data.model

import com.example.ui.viewmodel.AiSubTab

enum class AiSuggestionCategory(val label: String, val emoji: String) {
    ALL("All", "✨"),
    SECURITY("Security", "🛡️"),
    OPTIMIZATION("Gas & Yield", "⚡"),
    PORTFOLIO("Portfolio", "💎"),
    GOVERNANCE("Governance", "🏛️"),
    CONTRACT("Contracts", "📜")
}

data class AiSuggestion(
    val id: String,
    val category: AiSuggestionCategory,
    val title: String,
    val description: String,
    val prompt: String,
    val badge: String = "Recommended",
    val impactTag: String? = null,
    val icon: String = "✨",
    val isActionable: Boolean = true,
    val actionLabel: String = "Ask Agent",
    val targetAiTab: AiSubTab? = null,
    val contractAddress: String? = null
)
