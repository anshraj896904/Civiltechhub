package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChatMessageEntity
import com.example.data.ChatSessionEntity
import com.example.data.CivilCurriculumCatalog
import com.example.data.StudyMode
import com.example.data.TopicLessonModule
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintNavy
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SiteGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TutorChatScreen(
    messages: List<ChatMessageEntity>,
    sessions: List<ChatSessionEntity>,
    currentMode: StudyMode,
    selectedTopic: String,
    softwareVersion: String,
    isGenerating: Boolean,
    onSelectMode: (StudyMode) -> Unit,
    onSelectTopic: (TopicLessonModule) -> Unit,
    onSelectVersion: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onNewChat: () -> Unit,
    onSwitchSession: (ChatSessionEntity) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onSaveCommandBlock: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }
    var showVersionPickerDialog by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Compact Blueprint Header with Session Controls & Active Mode/Version Badges
        TutorHeroHeader(
            currentMode = currentMode,
            selectedTopic = selectedTopic,
            softwareVersion = softwareVersion,
            onNewChat = onNewChat,
            onOpenHistory = { showHistorySheet = true },
            onOpenVersionPicker = { showVersionPickerDialog = true }
        )

        // Chat Message Stream + Mode & Topic Starters
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("tutor_chat_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Interactive 3-Mode Selector & Topic Pills always accessible at top of chat
                ChatStarterPanel(
                    currentMode = currentMode,
                    selectedTopic = selectedTopic,
                    onSelectMode = onSelectMode,
                    onSelectTopic = onSelectTopic
                )
            }

            items(messages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    UserMessageBubble(message = msg)
                } else {
                    TutorStructuredMessageCard(
                        message = msg,
                        currentSoftwareVersion = softwareVersion,
                        onSelectVersion = onSelectVersion,
                        onQuickReply = { reply -> onSendMessage(reply) },
                        onSaveCommands = {
                            onSaveCommandBlock(
                                msg.suggestedSoftwareName.ifBlank { selectedTopic },
                                softwareVersion,
                                msg.commandsMenuShortcuts,
                                msg.siteExample
                            )
                        }
                    )
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "Preparing site example & step-by-step guidance…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Composer Bar
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.input_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send_message_desc)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.security_prototype_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }

    if (showVersionPickerDialog) {
        ModalBottomSheet(
            onDismissRequest = { showVersionPickerDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Set Your Software Version",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Your tutor always checks your software version before giving commands, menu paths, and shortcuts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val commonVersions = listOf(
                    "AutoCAD 2025", "AutoCAD 2024", "AutoCAD 2023", "AutoCAD LT",
                    "MicroStation 2024", "MicroStation CONNECT", "MicroStation V8i",
                    "Revit 2025", "Revit 2024", "Revit 2023",
                    "Bluebeam Revu 21", "Bluebeam Revu 20",
                    "Navisworks Manage 2025", "Navisworks Manage 2024"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonVersions.forEach { ver ->
                        FilterChip(
                            selected = softwareVersion.equals(ver, ignoreCase = true),
                            onClick = {
                                onSelectVersion(ver)
                                showVersionPickerDialog = false
                            },
                            label = { Text(ver) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chat_history_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = {
                            showHistorySheet = false
                            onNewChat()
                        },
                        modifier = Modifier.testTag("sheet_new_chat_button")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.new_chat_button))
                    }
                }
                HorizontalDivider()
                sessions.forEach { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSwitchSession(session)
                                showHistorySheet = false
                            }
                            .testTag("session_item_${session.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Mode: ${session.mode} • Version: ${session.softwareVersion.ifBlank { "Not set" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.testTag("delete_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete session",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TutorHeroHeader(
    currentMode: StudyMode,
    selectedTopic: String,
    softwareVersion: String,
    onNewChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenVersionPicker: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BlueprintNavyDark)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_bim_1791347759089),
            contentDescription = stringResource(R.string.hero_banner_desc),
            contentScale = ContentScale.Crop,
            alpha = 0.28f,
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlueprintNavyDark.copy(alpha = 0.65f),
                            BlueprintNavy.copy(alpha = 0.95f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SafetyAmber,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = BlueprintNavyDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "CivilBIM Diploma Tutor",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Site Examples • Exact Commands • Mock Interviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB8D4F0)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("session_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Session History",
                            tint = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = onNewChat,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.new_chat_button), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Status Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Mode: ${currentMode.label}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = BlueprintCyan.copy(alpha = 0.25f)
                    )
                )
                if (selectedTopic.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = selectedTopic,
                                color = SafetyAmber,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
                AssistChip(
                    onClick = onOpenVersionPicker,
                    modifier = Modifier.testTag("software_version_header_chip"),
                    label = {
                        Text(
                            text = if (softwareVersion.isBlank()) "Version: Tap to set" else "Ver: $softwareVersion",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatStarterPanel(
    currentMode: StudyMode,
    selectedTopic: String,
    onSelectMode: (StudyMode) -> Unit,
    onSelectTopic: (TopicLessonModule) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Start Here: How would you like to study today?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(StudyMode.LEARN_TOPIC, Icons.Default.School, "mode_card_learn"),
                    Triple(StudyMode.PRACTICE_SOFTWARE, Icons.Default.Terminal, "mode_card_practice"),
                    Triple(StudyMode.MOCK_INTERVIEW, Icons.Default.RecordVoiceOver, "mode_card_interview")
                )
                modes.forEach { (mode, icon, tag) ->
                    val isSelected = currentMode == mode
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectMode(mode) }
                            .testTag(tag)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Text(
                text = "Quick Topic Jump (AutoCAD, MicroStation, Revit, Bluebeam, Navisworks, CDE, COBie):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("topic_quick_row")
            ) {
                items(CivilCurriculumCatalog.topics, key = { it.id }) { topic ->
                    FilterChip(
                        selected = selectedTopic.equals(topic.shortName, ignoreCase = true),
                        onClick = { onSelectTopic(topic) },
                        modifier = Modifier.testTag("topic_chip_${topic.id}"),
                        label = { Text(topic.shortName) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessageEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.mainText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorStructuredMessageCard(
    message: ChatMessageEntity,
    currentSoftwareVersion: String,
    onSelectVersion: (String) -> Unit,
    onQuickReply: (String) -> Unit,
    onSaveCommands: () -> Unit
) {
    var commandSaved by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tutor Identity Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Civil & BIM Diploma Tutor",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 1. Main Friendly Explanation
            Text(
                text = message.mainText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 2. If Tutor is asking which software version the student uses before giving steps!
            AnimatedVisibility(visible = message.asksSoftwareVersion) {
                val matchedTopic = CivilCurriculumCatalog.topics.firstOrNull {
                    it.shortName.equals(message.suggestedSoftwareName, ignoreCase = true) ||
                        message.mainText.contains(it.shortName, ignoreCase = true)
                }
                val versions = matchedTopic?.supportedVersions ?: listOf(
                    "2025", "2024", "2023", "2022 / Older", "CONNECT Edition", "Revu 21"
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("version_prompt_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Select your ${message.suggestedSoftwareName.ifBlank { "software" }} version so I can give exact steps & commands:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            versions.forEach { ver ->
                                Button(
                                    onClick = { onSelectVersion(ver) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("select_version_chip_${ver.replace(" ", "_")}")
                                ) {
                                    Text(ver, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Real Site / Building Example Box
            if (message.siteExample.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Real Site & Building Example",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.siteExample,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 4. Small Steps Breakdown
            if (message.smallStepsFormatted.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Step-by-Step Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message.smallStepsFormatted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 5. Exact Commands, Menu Paths & Shortcuts (Monospace block)
            if (message.commandsMenuShortcuts.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BlueprintNavyDark,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = SafetyAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Commands, Menu Paths & Shortcuts",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SafetyAmber
                                )
                            }
                            AssistChip(
                                onClick = {
                                    commandSaved = true
                                    onSaveCommands()
                                },
                                label = {
                                    Text(
                                        text = if (commandSaved) "Saved" else "Save to Vault",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (commandSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                                        contentDescription = null,
                                        tint = SafetyAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            )
                        }
                        Text(
                            text = message.commandsMenuShortcuts,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2F1FF)
                        )
                    }
                }
            }

            // 6. Small Practice Exercise
            if (message.practiceExercise.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Hands-On Practice Exercise",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = message.practiceExercise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // 7. Mock Interview Score & Evaluation Card (when score out of 10 is present)
            if (message.interviewScore != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interview_score_feedback_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mock Interview Evaluation",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (message.interviewScore >= 7) SiteGreen else SafetyAmber
                            ) {
                                Text(
                                    text = "Score: ${message.interviewScore} / 10",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (message.interviewGoodPoints.isNotBlank()) {
                            Text(
                                text = "✅ What was good: ${message.interviewGoodPoints}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (message.interviewBetterAnswer.isNotBlank()) {
                            Text(
                                text = "💡 Better Sample Answer:\n\"${message.interviewBetterAnswer}\"",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 8. Active Mock Interview Question (1 at a time)
            if (message.nextInterviewQuestion.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BlueprintNavy,
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_interview_question_box")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SafetyAmber
                            ) {
                                Text(
                                    text = "${message.interviewQuestionType.ifBlank { "Technical" }} Question",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BlueprintNavyDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Answer in the box below (1 question at a time)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8D4F0)
                            )
                        }
                        Text(
                            text = message.nextInterviewQuestion,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // 9. Quick Understanding Check Question
            if (message.quickCheckQuestion.isNotBlank() && !message.asksSoftwareVersion) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Quick Understanding Check",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.quickCheckQuestion,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
