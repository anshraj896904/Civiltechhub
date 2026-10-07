package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.InterviewAttemptEntity
import com.example.ui.DedicatedInterviewUiState
import com.example.ui.theme.BlueprintNavy
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SiteGreen
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockInterviewScreen(
    interviewState: DedicatedInterviewUiState,
    pastAttempts: List<InterviewAttemptEntity>,
    onSelectFocus: (String) -> Unit,
    onSubmitAnswer: (String) -> Unit,
    onStartChatMockInterview: () -> Unit,
    onBackToTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToTutor() }

    var candidateAnswer by rememberSaveable { mutableStateOf("") }

    val avgScore = if (pastAttempts.isNotEmpty()) {
        String.format(Locale.US, "%.1f", pastAttempts.map { it.scoreOutOf10 }.average())
    } else {
        "—"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_interview_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mock Interview Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1 Question at a time • Technical & HR Mix • Score / 10 + Sample Answer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onStartChatMockInterview,
                    modifier = Modifier.testTag("switch_to_chat_interview_btn")
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Live Chat Mode")
                }
            }
        }

        // Performance Summary Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadgeCard(
                    title = "Avg Score",
                    value = "$avgScore / 10",
                    subtitle = "Diploma Readiness",
                    modifier = Modifier.weight(1f)
                )
                StatBadgeCard(
                    title = "Questions Done",
                    value = "${pastAttempts.size}",
                    subtitle = "Tech + HR Mix",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Focus Area Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Interview Focus Track:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                val tracks = listOf(
                    "All Topics (Technical + HR Mix)",
                    "Revit & BIM Coordination + HR",
                    "AutoCAD / MicroStation + Site HR",
                    "Navisworks 4D/5D, CDE & COBie"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tracks.forEach { track ->
                        FilterChip(
                            selected = interviewState.focusArea == track,
                            onClick = { onSelectFocus(track) },
                            label = { Text(track) }
                        )
                    }
                }
            }
        }

        // Current Single Interview Question Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlueprintNavy,
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dedicated_interview_question_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafetyAmber
                        ) {
                            Text(
                                text = "Question #${interviewState.questionNumber} • ${interviewState.currentQuestionCategory}",
                                style = MaterialTheme.typography.labelLarge,
                                color = BlueprintNavyDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Wait for your answer -> Score /10",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB8D4F0)
                        )
                    }

                    Text(
                        text = interviewState.currentQuestion,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = candidateAnswer,
                        onValueChange = { candidateAnswer = it },
                        placeholder = {
                            Text(
                                text = "Type your interview answer here using simple English and a site/BIM example…",
                                color = Color(0xFFA8C5E2)
                            )
                        },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_answer_input")
                    )

                    Button(
                        onClick = {
                            if (candidateAnswer.isNotBlank()) {
                                onSubmitAnswer(candidateAnswer)
                                candidateAnswer = ""
                            }
                        },
                        enabled = candidateAnswer.isNotBlank() && !interviewState.isEvaluating,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SafetyAmber,
                            contentColor = BlueprintNavyDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_interview_answer_button")
                    ) {
                        if (interviewState.isEvaluating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = BlueprintNavyDark
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Scoring Your Answer out of 10…")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.submit_answer_btn),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Latest Evaluation Card (Score out of 10, What was good, Better sample answer)
        item {
            AnimatedVisibility(visible = interviewState.lastEvaluationScore != null) {
                val score = interviewState.lastEvaluationScore ?: 0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latest_interview_evaluation_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Feedback on Previous Answer",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (score >= 7) SiteGreen else SafetyAmber
                            ) {
                                Text(
                                    text = "Score: $score / 10",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Question: ${interviewState.lastQuestionEvaluated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        HorizontalDivider()
                        Text(
                            text = "✅ What Was Good:\n${interviewState.lastWhatWasGood}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "💡 Better Sample Answer (10/10 Benchmark):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = interviewState.lastBetterAnswer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Past Scored Attempts Log
        if (pastAttempts.isNotEmpty()) {
            item {
                Text(
                    text = "Scored Interview History (${pastAttempts.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(pastAttempts, key = { it.id }) { attempt ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${attempt.questionType}] ${attempt.topic}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SafetyAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${attempt.scoreOutOf10}/10",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = "Q: ${attempt.questionAsked}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Your Answer: \"${attempt.studentAnswer}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "What Was Good: ${attempt.whatWasGood}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SiteGreen
                        )
                        Text(
                            text = "Better Sample: ${attempt.betterSampleAnswer}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadgeCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
