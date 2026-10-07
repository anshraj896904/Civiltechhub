package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.StudyMode
import com.example.ui.CivilTutorViewModel
import com.example.ui.MainTab
import com.example.ui.screens.CommandVaultScreen
import com.example.ui.screens.CurriculumLabScreen
import com.example.ui.screens.MockInterviewScreen
import com.example.ui.screens.TutorChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CivilBimTutorApp()
            }
        }
    }
}

private data class NavDestination(
    val tab: MainTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun CivilBimTutorApp(
    viewModel: CivilTutorViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val selectedTopic by viewModel.selectedTopicName.collectAsStateWithLifecycle()
    val softwareVersion by viewModel.softwareVersion.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val selectedCurriculumModule by viewModel.selectedCurriculumModule.collectAsStateWithLifecycle()
    val selectedDiplomaTrack by viewModel.selectedDiplomaTrack.collectAsStateWithLifecycle()
    val selectedSoftwareCategory by viewModel.selectedSoftwareCategory.collectAsStateWithLifecycle()
    val selectedMaterialType by viewModel.selectedMaterialType.collectAsStateWithLifecycle()
    val studyIndexSearchQuery by viewModel.studyIndexSearchQuery.collectAsStateWithLifecycle()
    val bookmarkedCommands by viewModel.bookmarkedCommands.collectAsStateWithLifecycle()
    val interviewState by viewModel.dedicatedInterviewState.collectAsStateWithLifecycle()
    val pastAttempts by viewModel.interviewAttempts.collectAsStateWithLifecycle()

    val destinations = listOf(
        NavDestination(
            tab = MainTab.TUTOR,
            labelRes = R.string.nav_tutor,
            selectedIcon = Icons.Filled.Engineering,
            unselectedIcon = Icons.Outlined.Engineering,
            testTag = "nav_tab_tutor"
        ),
        NavDestination(
            tab = MainTab.CURRICULUM,
            labelRes = R.string.nav_curriculum,
            selectedIcon = Icons.Filled.School,
            unselectedIcon = Icons.Outlined.School,
            testTag = "nav_tab_curriculum"
        ),
        NavDestination(
            tab = MainTab.INTERVIEW,
            labelRes = R.string.nav_interview,
            selectedIcon = Icons.Filled.RecordVoiceOver,
            unselectedIcon = Icons.Outlined.RecordVoiceOver,
            testTag = "nav_tab_interview"
        ),
        NavDestination(
            tab = MainTab.VAULT,
            labelRes = R.string.nav_vault,
            selectedIcon = Icons.Filled.Terminal,
            unselectedIcon = Icons.Outlined.Terminal,
            testTag = "nav_tab_vault"
        )
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar {
                        destinations.forEach { dest ->
                            val selected = currentTab == dest.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail {
                        destinations.forEach { dest ->
                            val selected = currentTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        MainTab.TUTOR -> {
                            TutorChatScreen(
                                messages = messages,
                                sessions = sessions,
                                currentMode = currentMode,
                                selectedTopic = selectedTopic,
                                softwareVersion = softwareVersion,
                                isGenerating = isGenerating,
                                onSelectMode = { mode -> viewModel.chooseModeInChat(mode) },
                                onSelectTopic = { topic -> viewModel.chooseTopicFromChip(topic) },
                                onSelectVersion = { ver -> viewModel.setSoftwareVersionAndContinue(ver) },
                                onSendMessage = { msg -> viewModel.sendUserMessage(msg) },
                                onNewChat = { viewModel.startNewChat() },
                                onSwitchSession = { session -> viewModel.switchSession(session) },
                                onDeleteSession = { id -> viewModel.deleteSession(id) },
                                onSaveCommandBlock = { sw, ver, block, site ->
                                    viewModel.saveChatCommandsToVault(sw, ver, block, site)
                                }
                            )
                        }

                        MainTab.CURRICULUM -> {
                            CurriculumLabScreen(
                                selectedModule = selectedCurriculumModule,
                                bookmarkedCommands = bookmarkedCommands,
                                searchQuery = studyIndexSearchQuery,
                                selectedDiplomaTrack = selectedDiplomaTrack,
                                selectedSoftwareCategory = selectedSoftwareCategory,
                                selectedMaterialType = selectedMaterialType,
                                onSearchQueryChange = { q -> viewModel.updateStudyIndexSearchQuery(q) },
                                onSelectDiplomaTrack = { track -> viewModel.selectDiplomaTrack(track) },
                                onSelectSoftwareCategory = { cat -> viewModel.selectSoftwareCategory(cat) },
                                onSelectMaterialType = { type -> viewModel.selectMaterialType(type) },
                                onResetIndexFilters = { viewModel.resetStudyIndexFilters() },
                                onSelectModule = { mod -> viewModel.openCurriculumModule(mod) },
                                onLaunchInTutor = { mod, mode, ver ->
                                    if (ver.isNotBlank()) {
                                        viewModel.setSoftwareVersionAndContinue(ver)
                                    }
                                    viewModel.chooseTopicFromChip(mod, mode)
                                },
                                onStudyItemInTutor = { item, mod ->
                                    viewModel.launchStudyMaterialInTutor(item, mod)
                                },
                                onToggleBookmark = { cmd, isSaved ->
                                    viewModel.toggleCommandBookmark(cmd, isSaved)
                                },
                                onBackToTutor = { viewModel.selectTab(MainTab.TUTOR) }
                            )
                        }

                        MainTab.INTERVIEW -> {
                            MockInterviewScreen(
                                interviewState = interviewState,
                                pastAttempts = pastAttempts,
                                onSelectFocus = { focus -> viewModel.setDedicatedInterviewFocus(focus) },
                                onSubmitAnswer = { ans -> viewModel.submitDedicatedInterviewAnswer(ans) },
                                onStartChatMockInterview = {
                                    viewModel.selectTab(MainTab.TUTOR)
                                    viewModel.chooseModeInChat(StudyMode.MOCK_INTERVIEW)
                                },
                                onBackToTutor = { viewModel.selectTab(MainTab.TUTOR) }
                            )
                        }

                        MainTab.VAULT -> {
                            CommandVaultScreen(
                                bookmarkedCommands = bookmarkedCommands,
                                onToggleCatalogBookmark = { cmd, isSaved ->
                                    viewModel.toggleCommandBookmark(cmd, isSaved)
                                },
                                onRemoveCustomBookmark = { id ->
                                    viewModel.removeBookmarkedCommand(id)
                                },
                                onBackToTutor = { viewModel.selectTab(MainTab.TUTOR) }
                            )
                        }
                    }
                }
            }
        }
    }
}
