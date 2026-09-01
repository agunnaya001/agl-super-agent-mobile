package com.example.data.model

data class QuizOption(
    val id: Int,
    val text: String
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<QuizOption>,
    val correctOptionId: Int,
    val explanation: String
)

data class LearningLesson(
    val id: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val iconEmoji: String,
    val shortSummary: String,
    val contentMarkdown: String,
    val keyTakeaways: List<String>,
    val quiz: List<QuizQuestion>,
    val isCompleted: Boolean = false
)

data class QuestItem(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val aglReward: Double,
    val category: QuestCategory,
    val currentProgress: Int,
    val maxProgress: Int,
    val isClaimed: Boolean,
    val iconEmoji: String
) {
    val isCompleted: Boolean get() = currentProgress >= maxProgress
    val progressFraction: Float get() = if (maxProgress > 0) (currentProgress.toFloat() / maxProgress).coerceIn(0f, 1f) else 0f
}

enum class QuestCategory {
    DAILY,
    WEEKLY,
    ECOSYSTEM,
    SECURITY_CHALLENGE
}
