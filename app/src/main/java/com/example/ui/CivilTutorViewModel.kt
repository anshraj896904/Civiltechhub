package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkedCommandEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatSessionEntity
import com.example.data.CivilCurriculumCatalog
import com.example.data.CivilTutorDatabase
import com.example.data.CivilTutorRepository
import com.example.data.DiplomaTrack
import com.example.data.FirebaseCloudRepository
import com.example.data.GeminiTutorService
import com.example.data.InterviewAttemptEntity
import com.example.data.SoftwareCategory
import com.example.data.SoftwareCommandItem
import com.example.data.StudyMaterialIndexItem
import com.example.data.StudyMaterialType
import com.example.data.StudyMode
import com.example.data.TopicLessonModule
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    TUTOR,
    CURRICULUM,
    INTERVIEW,
    VAULT
}

data class DedicatedInterviewUiState(
    val focusArea: String = "All Topics (Technical + HR Mix)",
    val currentQuestionCategory: String = "Technical",
    val currentQuestion: String = "In Revit Structure, if you place an RCC column on the Ground Floor plan and it goes downward into the foundation instead of up to the First Floor, what setting did you miss on the Options Bar, and how do you fix it?",
    val lastEvaluationScore: Int? = null,
    val lastWhatWasGood: String = "",
    val lastBetterAnswer: String = "",
    val lastQuestionEvaluated: String = "",
    val isEvaluating: Boolean = false,
    val questionNumber: Int = 1
)

class CivilTutorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CivilTutorRepository
    private val cloudRepository: FirebaseCloudRepository = FirebaseCloudRepository(application.applicationContext)
    private val geminiService = GeminiTutorService()

    private val _currentTab = MutableStateFlow(MainTab.TUTOR)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    private val _currentMode = MutableStateFlow(StudyMode.UNSELECTED)
    val currentMode: StateFlow<StudyMode> = _currentMode.asStateFlow()

    private val _selectedTopicName = MutableStateFlow("")
    val selectedTopicName: StateFlow<String> = _selectedTopicName.asStateFlow()

    private val _softwareVersion = MutableStateFlow("")
    val softwareVersion: StateFlow<String> = _softwareVersion.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedCurriculumModule = MutableStateFlow<TopicLessonModule?>(null)
    val selectedCurriculumModule: StateFlow<TopicLessonModule?> = _selectedCurriculumModule.asStateFlow()

    private val _selectedDiplomaTrack = MutableStateFlow(DiplomaTrack.ALL_TRACKS)
    val selectedDiplomaTrack: StateFlow<DiplomaTrack> = _selectedDiplomaTrack.asStateFlow()

    private val _selectedSoftwareCategory = MutableStateFlow(SoftwareCategory.ALL)
    val selectedSoftwareCategory: StateFlow<SoftwareCategory> = _selectedSoftwareCategory.asStateFlow()

    private val _selectedMaterialType = MutableStateFlow(StudyMaterialType.ALL)
    val selectedMaterialType: StateFlow<StudyMaterialType> = _selectedMaterialType.asStateFlow()

    private val _studyIndexSearchQuery = MutableStateFlow("")
    val studyIndexSearchQuery: StateFlow<String> = _studyIndexSearchQuery.asStateFlow()

    private val _dedicatedInterviewState = MutableStateFlow(DedicatedInterviewUiState())
    val dedicatedInterviewState: StateFlow<DedicatedInterviewUiState> = _dedicatedInterviewState.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>>
    val bookmarkedCommands: StateFlow<List<BookmarkedCommandEntity>>
    val interviewAttempts: StateFlow<List<InterviewAttemptEntity>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessageEntity>>

    init {
        val db = CivilTutorDatabase.getInstance(application)
        repository = CivilTutorRepository(db.dao())

        sessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bookmarkedCommands = repository.bookmarkedCommands.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        interviewAttempts = repository.interviewAttempts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeMessages = _activeSessionId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesForSession(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial session and starter bookmarks if empty
        viewModelScope.launch {
            val initialId = repository.createNewSession(
                title = "Diploma Study Session",
                mode = StudyMode.UNSELECTED,
                topic = "",
                softwareVersion = ""
            )
            _activeSessionId.value = initialId

            // Pre-bookmark 2 essential commands so the Vault has immediate examples
            val firstCmd = CivilCurriculumCatalog.topics.first().keyCommands.first()
            repository.toggleBookmark(firstCmd, isCurrentlySaved = false)

            // Observe cloud profile when authenticated
            if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
                cloudRepository.observeStudyProfile()
                    .catch { }
                    .collect { profile ->
                        if (profile != null) {
                            runCatching { DiplomaTrack.valueOf(profile.selectedDiplomaTrack) }.getOrNull()?.let {
                                _selectedDiplomaTrack.value = it
                            }
                            if (profile.preferredSoftwareVersion.isNotBlank() && _softwareVersion.value.isBlank()) {
                                _softwareVersion.value = profile.preferredSoftwareVersion
                            }
                        }
                    }
            }
        }
    }

    private fun syncProfileToCloudIfSignedIn() {
        val user = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return
        viewModelScope.launch {
            cloudRepository.saveStudyProfile(
                displayName = user.displayName ?: user.email ?: "Civil Student",
                selectedDiplomaTrack = _selectedDiplomaTrack.value.name,
                preferredSoftwareVersion = _softwareVersion.value
            )
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openCurriculumModule(module: TopicLessonModule?) {
        _selectedCurriculumModule.value = module
    }

    fun selectDiplomaTrack(track: DiplomaTrack) {
        _selectedDiplomaTrack.value = track
        syncProfileToCloudIfSignedIn()
    }

    fun selectSoftwareCategory(category: SoftwareCategory) {
        _selectedSoftwareCategory.value = category
    }

    fun selectMaterialType(materialType: StudyMaterialType) {
        _selectedMaterialType.value = materialType
    }

    fun updateStudyIndexSearchQuery(query: String) {
        _studyIndexSearchQuery.value = query
    }

    fun resetStudyIndexFilters() {
        _studyIndexSearchQuery.value = ""
        _selectedDiplomaTrack.value = DiplomaTrack.ALL_TRACKS
        _selectedSoftwareCategory.value = SoftwareCategory.ALL
        _selectedMaterialType.value = StudyMaterialType.ALL
    }

    fun launchStudyMaterialInTutor(item: StudyMaterialIndexItem, module: TopicLessonModule) {
        val targetMode = item.recommendedMode
        _currentMode.value = targetMode
        _selectedTopicName.value = module.shortName
        if (module.requiresSoftwareVersion && !_softwareVersion.value.contains(module.shortName.take(4), ignoreCase = true)) {
            _softwareVersion.value = ""
        }
        _currentTab.value = MainTab.TUTOR
        sendUserMessage(
            "Let's study '${item.title}' (${item.softwareCategory.displayName}) for my ${_selectedDiplomaTrack.value.shortLabel} diploma track."
        )
    }

    fun startNewChat() {
        viewModelScope.launch {
            _currentMode.value = StudyMode.UNSELECTED
            _selectedTopicName.value = ""
            _softwareVersion.value = ""
            val newId = repository.createNewSession(
                title = "New Study Session",
                mode = StudyMode.UNSELECTED,
                topic = "",
                softwareVersion = ""
            )
            _activeSessionId.value = newId
            _currentTab.value = MainTab.TUTOR
        }
    }

    fun switchSession(session: ChatSessionEntity) {
        _activeSessionId.value = session.id
        _currentMode.value = runCatching { StudyMode.valueOf(session.mode) }.getOrDefault(StudyMode.UNSELECTED)
        _selectedTopicName.value = session.selectedTopic
        _softwareVersion.value = session.softwareVersion
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                startNewChat()
            }
        }
    }

    fun chooseModeInChat(mode: StudyMode) {
        _currentMode.value = mode
        when (mode) {
            StudyMode.LEARN_TOPIC -> {
                sendUserMessage("I want to learn a topic. Please explain with simple English, small steps, and real site examples.")
            }
            StudyMode.PRACTICE_SOFTWARE -> {
                sendUserMessage("I want to practice software with exact commands, menu paths, shortcuts, and a practice exercise.")
            }
            StudyMode.MOCK_INTERVIEW -> {
                sendUserMessage("I want to do a mock interview. Ask me one question at a time mixing technical and HR questions.")
            }
            StudyMode.UNSELECTED -> {}
        }
    }

    fun chooseTopicFromChip(module: TopicLessonModule, modeOverride: StudyMode? = null) {
        val targetMode = modeOverride ?: if (_currentMode.value == StudyMode.UNSELECTED) {
            if (module.requiresSoftwareVersion) StudyMode.PRACTICE_SOFTWARE else StudyMode.LEARN_TOPIC
        } else {
            _currentMode.value
        }
        _currentMode.value = targetMode
        _selectedTopicName.value = module.shortName
        // Reset softwareVersion if switching to a different software family so the tutor always asks which version they use!
        if (module.requiresSoftwareVersion && !_softwareVersion.value.contains(module.shortName.take(4), ignoreCase = true)) {
            _softwareVersion.value = ""
        }
        _currentTab.value = MainTab.TUTOR
        sendUserMessage("Let's study ${module.title} in ${targetMode.label} mode.")
    }

    fun setSoftwareVersionAndContinue(version: String) {
        _softwareVersion.value = version
        syncProfileToCloudIfSignedIn()
        val topic = _selectedTopicName.value.ifBlank { "this software" }
        sendUserMessage("I am using version $version for $topic. Please give me the small steps, exact commands, menu paths, shortcuts, and a practice exercise.")
    }

    fun sendUserMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            val sessionId = _activeSessionId.value ?: repository.createNewSession(
                title = "Diploma Study Session",
                mode = _currentMode.value,
                topic = _selectedTopicName.value,
                softwareVersion = _softwareVersion.value
            ).also { _activeSessionId.value = it }

            // Detect mode or version updates from natural text
            val lower = text.lowercase()
            if (lower.contains("mock interview") || lower == "3") {
                _currentMode.value = StudyMode.MOCK_INTERVIEW
            } else if (lower.contains("practice software") || lower == "2") {
                _currentMode.value = StudyMode.PRACTICE_SOFTWARE
            } else if (lower.contains("learn a topic") || lower == "1") {
                _currentMode.value = StudyMode.LEARN_TOPIC
            }

            // Detect if user mentioned a topic
            CivilCurriculumCatalog.topics.firstOrNull {
                lower.contains(it.shortName.lowercase()) || lower.contains(it.id.replace("_", " "))
            }?.let { matched ->
                if (_selectedTopicName.value != matched.shortName) {
                    _selectedTopicName.value = matched.shortName
                }
            }

            // Detect if user stated a software version
            val versionRegex = Regex("(autocad\\s*20\\d\\d|revit\\s*20\\d\\d|navisworks\\s*20\\d\\d|microstation\\s*(20\\d\\d|connect|v8i)|revu\\s*(20|21|2019)|2025|2024|2023|2022|2021|2020|connect edition)", RegexOption.IGNORE_CASE)
            versionRegex.find(text)?.value?.let { foundVer ->
                _softwareVersion.value = foundVer
            }

            val historySnapshot = repository.getMessagesSnapshot(sessionId)
            val lastBotMsg = historySnapshot.lastOrNull { !it.isUser }

            // Insert user message
            repository.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    isUser = true,
                    mainText = text
                )
            )

            _isGenerating.value = true
            try {
                val structured = geminiService.generateTutorResponse(
                    userMessage = text,
                    currentMode = _currentMode.value,
                    selectedTopic = _selectedTopicName.value,
                    softwareVersion = _softwareVersion.value,
                    history = historySnapshot
                )

                val stepsText = structured.smallSteps.joinToString("\n") { step ->
                    if (step.startsWith("Step", ignoreCase = true) || step.startsWith("•")) step else "• $step"
                }

                val combinedMainText = buildString {
                    append(structured.mainExplanation)
                    if (structured.uncertaintyNote.isNotBlank()) {
                        append("\n\n⚠️ ")
                        append(structured.uncertaintyNote)
                    }
                }

                repository.insertMessage(
                    ChatMessageEntity(
                        sessionId = sessionId,
                        isUser = false,
                        mainText = combinedMainText,
                        siteExample = structured.realSiteExample,
                        smallStepsFormatted = stepsText,
                        commandsMenuShortcuts = structured.commandsAndShortcuts,
                        practiceExercise = structured.practiceExercise,
                        quickCheckQuestion = structured.quickCheckQuestion,
                        asksSoftwareVersion = structured.asksSoftwareVersion,
                        suggestedSoftwareName = structured.targetSoftwareName,
                        interviewScore = structured.interviewScoreOutOf10,
                        interviewGoodPoints = structured.interviewWhatWasGood,
                        interviewBetterAnswer = structured.interviewBetterSampleAnswer,
                        interviewQuestionType = structured.interviewQuestionCategory,
                        nextInterviewQuestion = structured.nextInterviewQuestion
                    )
                )

                // Update session metadata title
                val newTitle = when {
                    _currentMode.value == StudyMode.MOCK_INTERVIEW -> "Mock Interview (${_selectedTopicName.value.ifBlank { "Mixed" }})"
                    _selectedTopicName.value.isNotBlank() -> "${_selectedTopicName.value} ${_softwareVersion.value}".trim()
                    else -> "Diploma Study Session"
                }
                repository.updateSessionMetadata(
                    sessionId = sessionId,
                    mode = _currentMode.value,
                    topic = _selectedTopicName.value,
                    version = _softwareVersion.value,
                    title = newTitle
                )

                // If this was a scored mock interview turn, persist to interview_attempts table
                if (structured.interviewScoreOutOf10 != null && lastBotMsg?.nextInterviewQuestion?.isNotBlank() == true) {
                    repository.recordInterviewAttempt(
                        InterviewAttemptEntity(
                            topic = _selectedTopicName.value.ifBlank { "Civil & BIM Mixed" },
                            questionType = lastBotMsg.interviewQuestionType.ifBlank { "Technical" },
                            questionAsked = lastBotMsg.nextInterviewQuestion,
                            studentAnswer = text,
                            scoreOutOf10 = structured.interviewScoreOutOf10.coerceIn(1, 10),
                            whatWasGood = structured.interviewWhatWasGood,
                            betterSampleAnswer = structured.interviewBetterSampleAnswer
                        )
                    )
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Dedicated Mock Interview Arena actions
    fun setDedicatedInterviewFocus(focus: String) {
        _dedicatedInterviewState.value = _dedicatedInterviewState.value.copy(focusArea = focus)
    }

    fun submitDedicatedInterviewAnswer(studentAnswer: String) {
        val trimmed = studentAnswer.trim()
        if (trimmed.isBlank() || _dedicatedInterviewState.value.isEvaluating) return

        val stateBefore = _dedicatedInterviewState.value
        viewModelScope.launch {
            _dedicatedInterviewState.value = stateBefore.copy(isEvaluating = true)
            try {
                val fakeBotLast = ChatMessageEntity(
                    sessionId = -1,
                    isUser = false,
                    mainText = "Mock Interview Question",
                    interviewQuestionType = stateBefore.currentQuestionCategory,
                    nextInterviewQuestion = stateBefore.currentQuestion
                )
                val reply = geminiService.generateTutorResponse(
                    userMessage = trimmed,
                    currentMode = StudyMode.MOCK_INTERVIEW,
                    selectedTopic = stateBefore.focusArea,
                    softwareVersion = _softwareVersion.value,
                    history = listOf(fakeBotLast)
                )

                val score = (reply.interviewScoreOutOf10 ?: 7).coerceIn(1, 10)
                val good = reply.interviewWhatWasGood.ifBlank {
                    "Clear and practical explanation using diploma-level site concepts."
                }
                val better = reply.interviewBetterSampleAnswer.ifBlank {
                    "State the technical rule first, give a real construction site or BIM coordination example, and mention the exact command or ISO standard."
                }
                val nextQ = reply.nextInterviewQuestion.ifBlank {
                    "How do you coordinate a clash between an HVAC duct and an RCC beam in Navisworks Manage?"
                }
                val nextCategory = reply.interviewQuestionCategory.ifBlank {
                    if (stateBefore.currentQuestionCategory == "Technical") "HR" else "Technical"
                }

                repository.recordInterviewAttempt(
                    InterviewAttemptEntity(
                        topic = stateBefore.focusArea,
                        questionType = stateBefore.currentQuestionCategory,
                        questionAsked = stateBefore.currentQuestion,
                        studentAnswer = trimmed,
                        scoreOutOf10 = score,
                        whatWasGood = good,
                        betterSampleAnswer = better
                    )
                )
                if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
                    cloudRepository.saveInterviewAttempt(
                        attemptId = "att_${System.currentTimeMillis()}",
                        attempt = InterviewAttemptEntity(
                            topic = stateBefore.focusArea,
                            questionType = stateBefore.currentQuestionCategory,
                            questionAsked = stateBefore.currentQuestion,
                            studentAnswer = trimmed,
                            scoreOutOf10 = score,
                            whatWasGood = good,
                            betterSampleAnswer = better
                        )
                    )
                }

                _dedicatedInterviewState.value = stateBefore.copy(
                    currentQuestionCategory = nextCategory,
                    currentQuestion = nextQ,
                    lastEvaluationScore = score,
                    lastWhatWasGood = good,
                    lastBetterAnswer = better,
                    lastQuestionEvaluated = stateBefore.currentQuestion,
                    isEvaluating = false,
                    questionNumber = stateBefore.questionNumber + 1
                )
            } finally {
                _dedicatedInterviewState.value = _dedicatedInterviewState.value.copy(isEvaluating = false)
            }
        }
    }

    fun toggleCommandBookmark(item: SoftwareCommandItem, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(item, isCurrentlySaved)
            if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
                if (isCurrentlySaved) {
                    cloudRepository.removeBookmarkedCommand(item.id)
                } else {
                    cloudRepository.saveBookmarkedCommand(
                        BookmarkedCommandEntity(
                            id = item.id,
                            software = item.software,
                            versionScope = item.versionScope,
                            taskTitle = item.taskTitle,
                            command = item.command,
                            shortcut = item.shortcut,
                            menuPath = item.menuPath,
                            siteExample = item.siteExample
                        )
                    )
                }
            }
        }
    }

    fun saveChatCommandsToVault(
        software: String,
        version: String,
        commandsBlock: String,
        siteExample: String
    ) {
        viewModelScope.launch {
            repository.saveCustomCommandBookmark(
                software = software.ifBlank { _selectedTopicName.value.ifBlank { "BIM/CAD Software" } },
                versionScope = version.ifBlank { _softwareVersion.value.ifBlank { "2024/2025" } },
                taskTitle = "Saved Tutor Command Reference",
                commandBlock = commandsBlock,
                siteExample = siteExample
            )
            if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
                val id = "custom_${System.currentTimeMillis()}"
                cloudRepository.saveBookmarkedCommand(
                    BookmarkedCommandEntity(
                        id = id,
                        software = software.ifBlank { _selectedTopicName.value.ifBlank { "BIM/CAD Software" } },
                        versionScope = version.ifBlank { _softwareVersion.value.ifBlank { "2024/2025" } },
                        taskTitle = "Saved Tutor Command Reference",
                        command = commandsBlock.lines().firstOrNull() ?: commandsBlock,
                        shortcut = "Saved from AI Tutor",
                        menuPath = commandsBlock,
                        siteExample = siteExample
                    )
                )
            }
        }
    }

    fun removeBookmarkedCommand(id: String) {
        viewModelScope.launch {
            repository.removeBookmarkedCommand(id)
            if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
                cloudRepository.removeBookmarkedCommand(id)
            }
        }
    }
}
