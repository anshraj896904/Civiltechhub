package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CivilCurriculumCatalog
import com.example.data.DiplomaTrack
import com.example.data.SoftwareCategory
import com.example.data.StudyMaterialIndexItem
import com.example.data.StudyMaterialType
import com.example.data.StudyMode
import com.example.data.TopicLessonModule
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintNavy
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SiteGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchableStudyIndexComponent(
    searchQuery: String,
    selectedDiplomaTrack: DiplomaTrack,
    selectedSoftwareCategory: SoftwareCategory,
    selectedMaterialType: StudyMaterialType,
    onSearchQueryChange: (String) -> Unit,
    onSelectDiplomaTrack: (DiplomaTrack) -> Unit,
    onSelectSoftwareCategory: (SoftwareCategory) -> Unit,
    onSelectMaterialType: (StudyMaterialType) -> Unit,
    onResetFilters: () -> Unit,
    onOpenModule: (TopicLessonModule) -> Unit,
    onStudyItemInTutor: (StudyMaterialIndexItem, TopicLessonModule) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null
) {
    val collapsedCategories = remember { mutableStateMapOf<SoftwareCategory, Boolean>() }

    // Filtered materials matching current track, software category, material type, and search query
    val filteredMaterials = remember(
        searchQuery,
        selectedDiplomaTrack,
        selectedSoftwareCategory,
        selectedMaterialType
    ) {
        CivilCurriculumCatalog.filterStudyMaterials(
            query = searchQuery,
            diplomaTrack = selectedDiplomaTrack,
            softwareCategory = selectedSoftwareCategory,
            materialType = selectedMaterialType
        )
    }

    // Software category counts for the active Diploma Track + Search Query + Material Type
    val softwareCounts = remember(searchQuery, selectedDiplomaTrack, selectedMaterialType) {
        val baseForCounts = CivilCurriculumCatalog.filterStudyMaterials(
            query = searchQuery,
            diplomaTrack = selectedDiplomaTrack,
            softwareCategory = SoftwareCategory.ALL,
            materialType = selectedMaterialType
        )
        SoftwareCategory.entries.associateWith { category ->
            if (category == SoftwareCategory.ALL) {
                baseForCounts.size
            } else {
                baseForCounts.count { it.softwareCategory == category }
            }
        }
    }

    // Group matching materials by SoftwareCategory in canonical curriculum order
    val groupedBySoftware = remember(filteredMaterials) {
        SoftwareCategory.entries
            .filter { it != SoftwareCategory.ALL }
            .mapNotNull { category ->
                val itemsForSoftware = filteredMaterials.filter { it.softwareCategory == category }
                if (itemsForSoftware.isNotEmpty()) category to itemsForSoftware else null
            }
    }

    LazyColumn(
        modifier = modifier.testTag("searchable_study_index_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (headerContent != null) {
            item(key = "custom_header") {
                headerContent()
            }
        }

        // 1. Diploma Track Selector Banner Card
        item(key = "diploma_track_selector_card") {
            DiplomaTrackFilterBanner(
                selectedTrack = selectedDiplomaTrack,
                matchingCount = filteredMaterials.size,
                onSelectTrack = onSelectDiplomaTrack
            )
        }

        // 2. Search Input & Software Category Filter Bar
        item(key = "search_and_software_filters") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("study_index_clear_search")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search query"
                                    )
                                }
                            }
                        },
                        placeholder = {
                            Text("Search AutoCAD, Revit, Navisworks, commands, rebar, clash…")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_index_search_input")
                    )

                    // Categorized Software Filter Row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter by Software Category",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${filteredMaterials.size} study items across ${groupedBySoftware.size} software groups",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("study_index_result_summary")
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("software_category_filter_row")
                        ) {
                            items(SoftwareCategory.entries, key = { it.id }) { category ->
                                val count = softwareCounts[category] ?: 0
                                val isSelected = selectedSoftwareCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectSoftwareCategory(category) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = iconForSoftwareCategory(category),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text("${category.shortLabel} ($count)")
                                    },
                                    modifier = Modifier.testTag("software_category_chip_${category.id}")
                                )
                            }
                        }
                    }

                    // Material Type Sub-Filter Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("material_type_filter_row")
                    ) {
                        items(StudyMaterialType.entries, key = { it.id }) { type ->
                            val isSelected = selectedMaterialType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectMaterialType(type) },
                                label = { Text(type.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("material_type_chip_${type.id}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Empty State if no study materials match filters
        if (groupedBySoftware.isEmpty()) {
            item(key = "empty_study_index_state") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_index_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAltOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No study materials match these filters",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Try switching your Diploma Track to 'All Tracks', selecting 'All Software', or clearing your search query.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onResetFilters,
                            modifier = Modifier.testTag("study_index_reset_filters_btn")
                        ) {
                            Text("Reset Index Filters")
                        }
                    }
                }
            }
        } else {
            // 4. Categorized Software Sections (AutoCAD, Revit, Navisworks, etc.)
            groupedBySoftware.forEach { (softwareCategory, itemsInCategory) ->
                val isCollapsed = collapsedCategories[softwareCategory] == true

                item(key = "software_header_${softwareCategory.id}") {
                    SoftwareCategoryGroupHeader(
                        category = softwareCategory,
                        itemCount = itemsInCategory.size,
                        isCollapsed = isCollapsed,
                        onToggleCollapse = {
                            collapsedCategories[softwareCategory] = !isCollapsed
                        }
                    )
                }

                if (!isCollapsed) {
                    items(
                        items = itemsInCategory,
                        key = { it.id }
                    ) { item ->
                        val parentModule = remember(item.parentModuleId) {
                            CivilCurriculumCatalog.topics.firstOrNull { it.id == item.parentModuleId }
                        }
                        StudyMaterialIndexCard(
                            item = item,
                            activeTrack = selectedDiplomaTrack,
                            onOpenModule = {
                                if (parentModule != null) {
                                    onOpenModule(parentModule)
                                }
                            },
                            onAskTutor = {
                                if (parentModule != null) {
                                    onStudyItemInTutor(item, parentModule)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiplomaTrackFilterBanner(
    selectedTrack: DiplomaTrack,
    matchingCount: Int,
    onSelectTrack: (DiplomaTrack) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BlueprintNavyDark),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diploma_track_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(BlueprintNavyDark, BlueprintNavy)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Current Diploma Track Filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SafetyAmber
                    ) {
                        Text(
                            text = "${selectedTrack.semesterBadge} • $matchingCount Items",
                            style = MaterialTheme.typography.labelMedium,
                            color = BlueprintNavyDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Track selector pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("diploma_track_chip_row")
                ) {
                    items(DiplomaTrack.entries, key = { it.id }) { track ->
                        val isSelected = track == selectedTrack
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SafetyAmber else Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable { onSelectTrack(track) }
                                .testTag("diploma_track_chip_${track.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = track.shortLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) BlueprintNavyDark else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Active track details
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedTrack.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = SafetyAmber,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("active_diploma_track_title")
                        )
                        Text(
                            text = selectedTrack.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD9E8F6)
                        )
                        Text(
                            text = "Track Software: ${selectedTrack.coreSoftwareSummary}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF90E0EF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftwareCategoryGroupHeader(
    category: SoftwareCategory,
    itemCount: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCollapse() }
            .testTag("software_section_header_${category.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = iconForSoftwareCategory(category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "$itemCount materials",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = category.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Icon(
                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (isCollapsed) "Expand software section" else "Collapse software section",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyMaterialIndexCard(
    item: StudyMaterialIndexItem,
    activeTrack: DiplomaTrack,
    onOpenModule: () -> Unit,
    onAskTutor: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenModule() }
            .testTag("study_index_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top badges: Material Type + Software Version + Estimated Time
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
                        color = badgeColorForMaterialType(item.materialType)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = iconForMaterialType(item.materialType),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = item.materialType.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.softwareVersionTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${item.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title & Summary
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            if (item.materialType == StudyMaterialType.VISUAL_3D_TEACH) {
                val scene = remember(item.parentModuleId) {
                    CivilCurriculumCatalog.visual3DSceneForModule(item.parentModuleId)
                }
                val drawableRes = resolve3DTeachDrawableRes(scene.imageAssetType)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BlueprintNavyDark.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${scene.callouts.size} Interactive 3D Hotspots + 3D Orbit Canvas",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafetyAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Optional Command & Shortcut Highlight Pill
            AnimatedVisibility(visible = item.commandHighlight.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueprintNavyDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Key Command / Shortcut: ${item.commandHighlight}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF90E0EF)
                        )
                    }
                }
            }

            // Diploma Tracks applicable to this material
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.diplomaTracks.forEach { track ->
                    val isCurrentTrack = activeTrack == track
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrentTrack) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        }
                    ) {
                        Text(
                            text = "${track.semesterBadge}: ${track.shortLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrentTrack) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons: Open Lab Module or Study in AI Tutor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenModule,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_index_open_module_${item.id}")
                ) {
                    Text("Open Lab")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onAskTutor,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_index_ask_tutor_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Ask Tutor")
                }
            }
        }
    }
}

private fun iconForSoftwareCategory(category: SoftwareCategory): ImageVector = when (category) {
    SoftwareCategory.ALL -> Icons.Default.Hub
    SoftwareCategory.AUTOCAD -> Icons.Default.Architecture
    SoftwareCategory.REVIT -> Icons.Default.Layers
    SoftwareCategory.NAVISWORKS -> Icons.Default.AccountTree
    SoftwareCategory.MICROSTATION -> Icons.Default.Construction
    SoftwareCategory.BLUEBEAM -> Icons.Default.Description
    SoftwareCategory.CDE_COBIE -> Icons.AutoMirrored.Filled.MenuBook
}

private fun iconForMaterialType(type: StudyMaterialType): ImageVector = when (type) {
    StudyMaterialType.ALL -> Icons.AutoMirrored.Filled.MenuBook
    StudyMaterialType.VISUAL_3D_TEACH -> Icons.Default.ViewInAr
    StudyMaterialType.CONCEPT_GUIDE -> Icons.Default.School
    StudyMaterialType.STEP_WORKFLOW -> Icons.Default.Layers
    StudyMaterialType.COMMAND_REFERENCE -> Icons.Default.Terminal
    StudyMaterialType.SITE_CASE_STUDY -> Icons.Default.Construction
    StudyMaterialType.PRACTICE_EXERCISE -> Icons.Default.Engineering
    StudyMaterialType.QUICK_CHECK -> Icons.Default.Quiz
}

private fun badgeColorForMaterialType(type: StudyMaterialType): Color = when (type) {
    StudyMaterialType.ALL -> BlueprintNavy
    StudyMaterialType.VISUAL_3D_TEACH -> Color(0xFFD62828)
    StudyMaterialType.CONCEPT_GUIDE -> BlueprintNavy
    StudyMaterialType.STEP_WORKFLOW -> BlueprintCyan
    StudyMaterialType.COMMAND_REFERENCE -> Color(0xFF5A189A)
    StudyMaterialType.SITE_CASE_STUDY -> SiteGreen
    StudyMaterialType.PRACTICE_EXERCISE -> Color(0xFFD97706)
    StudyMaterialType.QUICK_CHECK -> Color(0xFF0077B6)
}
