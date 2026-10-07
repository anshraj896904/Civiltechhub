package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BookmarkedCommandEntity
import com.example.data.CivilCurriculumCatalog
import com.example.data.DiplomaTrack
import com.example.data.SoftwareCategory
import com.example.data.SoftwareCommandItem
import com.example.data.StudyMaterialIndexItem
import com.example.data.StudyMaterialType
import com.example.data.StudyMode
import com.example.data.TopicLessonModule
import com.example.ui.components.SearchableStudyIndexComponent
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SiteGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurriculumLabScreen(
    selectedModule: TopicLessonModule?,
    bookmarkedCommands: List<BookmarkedCommandEntity>,
    searchQuery: String = "",
    selectedDiplomaTrack: DiplomaTrack = DiplomaTrack.ALL_TRACKS,
    selectedSoftwareCategory: SoftwareCategory = SoftwareCategory.ALL,
    selectedMaterialType: StudyMaterialType = StudyMaterialType.ALL,
    onSearchQueryChange: (String) -> Unit = {},
    onSelectDiplomaTrack: (DiplomaTrack) -> Unit = {},
    onSelectSoftwareCategory: (SoftwareCategory) -> Unit = {},
    onSelectMaterialType: (StudyMaterialType) -> Unit = {},
    onResetIndexFilters: () -> Unit = {},
    onSelectModule: (TopicLessonModule?) -> Unit,
    onLaunchInTutor: (TopicLessonModule, StudyMode, String) -> Unit,
    onStudyItemInTutor: (StudyMaterialIndexItem, TopicLessonModule) -> Unit = { item, mod ->
        onLaunchInTutor(mod, item.recommendedMode, "")
    },
    onToggleBookmark: (SoftwareCommandItem, Boolean) -> Unit,
    onBackToTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchableIndexView by rememberSaveable { mutableStateOf(true) }

    BackHandler {
        if (selectedModule != null) {
            onSelectModule(null)
        } else {
            onBackToTutor()
        }
    }

    if (selectedModule == null) {
        if (showSearchableIndexView) {
            SearchableStudyIndexComponent(
                searchQuery = searchQuery,
                selectedDiplomaTrack = selectedDiplomaTrack,
                selectedSoftwareCategory = selectedSoftwareCategory,
                selectedMaterialType = selectedMaterialType,
                onSearchQueryChange = onSearchQueryChange,
                onSelectDiplomaTrack = onSelectDiplomaTrack,
                onSelectSoftwareCategory = onSelectSoftwareCategory,
                onSelectMaterialType = onSelectMaterialType,
                onResetFilters = onResetIndexFilters,
                onOpenModule = { mod -> onSelectModule(mod) },
                onStudyItemInTutor = onStudyItemInTutor,
                modifier = modifier.fillMaxSize(),
                headerContent = {
                    CurriculumHeaderWithViewSwitcher(
                        showSearchableIndexView = showSearchableIndexView,
                        onToggleView = { showSearchableIndexView = it }
                    )
                }
            )
        } else {
            val filteredModules = CivilCurriculumCatalog.topics.filter { module ->
                val matchesTrack = selectedDiplomaTrack == DiplomaTrack.ALL_TRACKS ||
                    CivilCurriculumCatalog.diplomaTracksForModule(module.id).contains(selectedDiplomaTrack)
                val matchesSoftware = selectedSoftwareCategory == SoftwareCategory.ALL ||
                    CivilCurriculumCatalog.softwareCategoryForModule(module.id) == selectedSoftwareCategory
                val matchesQuery = searchQuery.isBlank() ||
                    module.title.contains(searchQuery, ignoreCase = true) ||
                    module.shortName.contains(searchQuery, ignoreCase = true) ||
                    module.overviewSimple.contains(searchQuery, ignoreCase = true)
                matchesTrack && matchesSoftware && matchesQuery
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("curriculum_module_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CurriculumHeaderWithViewSwitcher(
                        showSearchableIndexView = showSearchableIndexView,
                        onToggleView = { showSearchableIndexView = it }
                    )
                }

                items(filteredModules, key = { it.id }) { module ->
                    val moduleTracks = CivilCurriculumCatalog.diplomaTracksForModule(module.id)
                    val moduleSoftware = CivilCurriculumCatalog.softwareCategoryForModule(module.id)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectModule(module) }
                            .testTag("curriculum_card_${module.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = moduleSoftware.displayName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = module.badge,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${module.smallSteps.size} Steps • ${module.keyCommands.size} Commands",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = module.overviewSimple,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                moduleTracks.forEach { track ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${track.semesterBadge}: ${track.shortLabel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (module.requiresSoftwareVersion) {
                                        "Versions: ${module.supportedVersions.take(2).joinToString(", ")}+"
                                    } else {
                                        "Standard: ${module.supportedVersions.firstOrNull().orEmpty()}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Open Lab",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        TopicModuleDetailView(
            module = selectedModule,
            bookmarkedCommands = bookmarkedCommands,
            onBack = { onSelectModule(null) },
            onLaunchInTutor = onLaunchInTutor,
            onToggleBookmark = onToggleBookmark,
            modifier = modifier
        )
    }
}

@Composable
private fun CurriculumHeaderWithViewSwitcher(
    showSearchableIndexView: Boolean,
    onToggleView: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Study Material Index & Curriculum Lab",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Search and filter Civil & BIM study materials categorized by software (AutoCAD, Revit, Navisworks, MicroStation, Bluebeam, CDE/COBie) and tailored to your current Diploma Track.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showSearchableIndexView,
                onClick = { onToggleView(true) },
                label = { Text("Searchable Study Index") },
                modifier = Modifier.testTag("view_mode_study_index")
            )
            FilterChip(
                selected = !showSearchableIndexView,
                onClick = { onToggleView(false) },
                label = { Text("Syllabus Modules (${CivilCurriculumCatalog.topics.size})") },
                modifier = Modifier.testTag("view_mode_syllabus_modules")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicModuleDetailView(
    module: TopicLessonModule,
    bookmarkedCommands: List<BookmarkedCommandEntity>,
    onBack: () -> Unit,
    onLaunchInTutor: (TopicLessonModule, StudyMode, String) -> Unit,
    onToggleBookmark: (SoftwareCommandItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var chosenVersion by rememberSaveable(module.id) {
        mutableStateOf(if (module.requiresSoftwareVersion) "" else module.supportedVersions.firstOrNull().orEmpty())
    }
    var selectedQuizOption by rememberSaveable(module.id) { mutableIntStateOf(-1) }
    var quizSubmitted by rememberSaveable(module.id) { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("curriculum_detail_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("curriculum_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to topics"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = module.badge,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Software Version Gate / Selector (Ask which software version before giving steps!)
        if (module.requiresSoftwareVersion) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("module_version_selector_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Step 0: Which software version of ${module.shortName} do you use?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Select your version below so your commands, ribbon paths, and tutor session match your exact workspace:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            module.supportedVersions.forEach { ver ->
                                FilterChip(
                                    selected = chosenVersion == ver,
                                    onClick = { chosenVersion = ver },
                                    label = { Text(ver) },
                                    modifier = Modifier.testTag("lab_version_chip_${ver.replace(" ", "_")}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simple English Overview & Real Site Example
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Simple English Explanation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = module.overviewSimple,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Real Site & Building Example",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = module.realSiteExample,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Small Steps Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (chosenVersion.isNotBlank()) {
                            "Small Step-by-Step Guide ($chosenVersion)"
                        } else {
                            "Small Step-by-Step Guide (Select version above for exact ribbon paths)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    module.smallSteps.forEach { step ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Exact Commands, Menu Paths & Shortcuts
        item {
            Text(
                text = "Exact Commands, Menu Paths & Shortcuts",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(module.keyCommands, key = { it.id }) { cmd ->
            val isSaved = bookmarkedCommands.any { it.id == cmd.id }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BlueprintNavyDark,
                contentColor = Color.White,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = cmd.taskTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = SafetyAmber
                            )
                        }
                        IconButton(
                            onClick = { onToggleBookmark(cmd, isSaved) },
                            modifier = Modifier.testTag("bookmark_cmd_${cmd.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark command",
                                tint = SafetyAmber
                            )
                        }
                    }
                    Text(
                        text = "Command: ${cmd.command}   |   Shortcut: ${cmd.shortcut}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF90E0EF)
                    )
                    Text(
                        text = "Menu Path: ${cmd.menuPath}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Text(
                        text = "Site Use: ${cmd.siteExample}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD0E2F2)
                    )
                    Text(
                        text = "Mini Exercise: ${cmd.practiceExercise}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafetyAmber
                    )
                }
            }
        }

        // Practice Exercise Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Module Practice Exercise",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        text = module.practiceExercise,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Interactive Quick Check Question
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_check_quiz_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Quick Check Question",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = module.quickCheckQuestion,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    module.quickCheckOptions.forEachIndexed { idx, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuizOption = idx
                                    quizSubmitted = true
                                }
                                .padding(vertical = 4.dp)
                                .testTag("quiz_option_$idx"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedQuizOption == idx,
                                onClick = {
                                    selectedQuizOption = idx
                                    quizSubmitted = true
                                }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    AnimatedVisibility(visible = quizSubmitted && selectedQuizOption >= 0) {
                        val isCorrect = selectedQuizOption == module.quickCheckCorrectIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCorrect) SiteGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isCorrect) SiteGreen else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isCorrect) {
                                        module.quickCheckExplanation
                                    } else {
                                        "Not quite—try another option! Hint: Think about how elements behave on a real building model or site."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Launch in Live Tutor Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onLaunchInTutor(module, StudyMode.LEARN_TOPIC, chosenVersion)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("launch_learn_in_tutor_btn")
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ask AI Tutor")
                }
                OutlinedButton(
                    onClick = {
                        onLaunchInTutor(module, StudyMode.PRACTICE_SOFTWARE, chosenVersion)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("launch_practice_in_tutor_btn")
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Practice Steps")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
