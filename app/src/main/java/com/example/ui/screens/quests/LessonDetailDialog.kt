package com.example.ui.screens.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LearningLesson
import com.example.data.model.QuizOption
import com.example.data.model.QuizQuestion
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

@Composable
fun LessonDetailDialog(
    lesson: LearningLesson,
    onDismiss: () -> Unit,
    onQuizCompleted: (score: Int, total: Int) -> Unit
) {
    var inQuizMode by remember { mutableStateOf(false) }
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = lesson.iconEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = lesson.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${lesson.category} • ${lesson.durationMinutes} min read • +${lesson.xpReward} XP",
                                fontSize = 11.sp,
                                color = GoldRewards
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!inQuizMode) {
                    // Reading Lesson Mode
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = lesson.shortSummary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BaseCyan,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Lesson Body Content
                        Text(
                            text = lesson.contentMarkdown,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Takeaways
                        Text(
                            text = "Key Takeaways",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        lesson.keyTakeaways.forEach { takeaway ->
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
                                    text = takeaway,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Start Quiz Button
                    Button(
                        onClick = { inQuizMode = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_quiz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Quiz to Earn +${lesson.xpReward} XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else if (isQuizFinished) {
                    // Quiz Result screen
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🎉", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quiz Completed!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You scored $correctAnswersCount / ${lesson.quiz.size}",
                            fontSize = 15.sp,
                            color = NeonEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldRewards.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "+${lesson.xpReward} XP & +2.0 AGL Added",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldRewards
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onQuizCompleted(correctAnswersCount, lesson.quiz.size)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finish_quiz_done_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Claim Rewards & Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Active Question
                    val currentQuestion = lesson.quiz.getOrNull(currentQuestionIdx)
                    if (currentQuestion != null) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Question ${currentQuestionIdx + 1} of ${lesson.quiz.size}",
                                fontSize = 12.sp,
                                color = BaseCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQuestion.question,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 21.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Options
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                currentQuestion.options.forEach { option ->
                                    QuizOptionRow(
                                        option = option,
                                        isSelected = selectedOptionId == option.id,
                                        isSubmitted = isAnswerSubmitted,
                                        isCorrect = option.id == currentQuestion.correctOptionId,
                                        onSelect = {
                                            if (!isAnswerSubmitted) {
                                                selectedOptionId = option.id
                                            }
                                        }
                                    )
                                }
                            }

                            if (isAnswerSubmitted) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedOptionId == currentQuestion.correctOptionId)
                                            NeonEmerald.copy(alpha = 0.15f)
                                        else
                                            DangerCrimson.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (selectedOptionId == currentQuestion.correctOptionId) "Correct! 🎯" else "Explanation:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (selectedOptionId == currentQuestion.correctOptionId) NeonEmerald else DangerCrimson
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuestion.explanation,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Submit / Next Question button
                        Button(
                            onClick = {
                                if (!isAnswerSubmitted) {
                                    if (selectedOptionId != null) {
                                        isAnswerSubmitted = true
                                        if (selectedOptionId == currentQuestion.correctOptionId) {
                                            correctAnswersCount++
                                        }
                                    }
                                } else {
                                    if (currentQuestionIdx + 1 < lesson.quiz.size) {
                                        currentQuestionIdx++
                                        selectedOptionId = null
                                        isAnswerSubmitted = false
                                    } else {
                                        isQuizFinished = true
                                    }
                                }
                            },
                            enabled = selectedOptionId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quiz_action_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (!isAnswerSubmitted) "Check Answer" else if (currentQuestionIdx + 1 < lesson.quiz.size) "Next Question →" else "Finish Quiz 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionRow(
    option: QuizOption,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        !isSubmitted && isSelected -> BaseCyan
        isSubmitted && isCorrect -> NeonEmerald
        isSubmitted && isSelected && !isCorrect -> DangerCrimson
        else -> DarkBorder
    }

    val bgColor = when {
        !isSubmitted && isSelected -> BaseBlue.copy(alpha = 0.2f)
        isSubmitted && isCorrect -> NeonEmerald.copy(alpha = 0.15f)
        isSubmitted && isSelected && !isCorrect -> DangerCrimson.copy(alpha = 0.15f)
        else -> DarkCard
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected || (isSubmitted && isCorrect)) borderColor else DarkCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${option.id}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected || (isSubmitted && isCorrect)) Color.White else TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = option.text,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
