package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.data.BookmarkedCommandEntity
import com.example.data.CivilCurriculumCatalog
import com.example.data.SoftwareCommandItem
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafetyAmber

@Composable
fun CommandVaultScreen(
    bookmarkedCommands: List<BookmarkedCommandEntity>,
    onToggleCatalogBookmark: (SoftwareCommandItem, Boolean) -> Unit,
    onRemoveCustomBookmark: (String) -> Unit,
    onBackToTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToTutor() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSoftwareFilter by rememberSaveable { mutableStateOf("All") }
    var showBookmarkedOnly by rememberSaveable { mutableStateOf(false) }

    val allCatalogCommands: List<SoftwareCommandItem> =
        CivilCurriculumCatalog.topics.flatMap { it.keyCommands }

    val softwareFilters = listOf(
        "All",
        "AutoCAD 2D/3D",
        "MicroStation",
        "Revit Arch & Struct",
        "Revit Plugins",
        "Bluebeam Revu",
        "Navisworks Manage",
        "CDE (ISO 19650)",
        "COBie"
    )

    val filteredCommands = allCatalogCommands.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
            item.taskTitle.contains(searchQuery, ignoreCase = true) ||
            item.command.contains(searchQuery, ignoreCase = true) ||
            item.shortcut.contains(searchQuery, ignoreCase = true) ||
            item.menuPath.contains(searchQuery, ignoreCase = true) ||
            item.siteExample.contains(searchQuery, ignoreCase = true)

        val matchesSoftware = selectedSoftwareFilter == "All" ||
            item.software.contains(selectedSoftwareFilter.take(7), ignoreCase = true)

        val isSaved = bookmarkedCommands.any { it.id == item.id }
        matchesSearch && matchesSoftware && (!showBookmarkedOnly || isSaved)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("command_vault_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Command, Menu Path & Shortcut Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Instant reference for AutoCAD, MicroStation, Revit, Plugins, Bluebeam Revu, Navisworks 4D/5D, CDE, and COBie—with site examples and practice tasks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = {
                    Text("Search command, shortcut (e.g. EXTRUDE, Ctrl+F2, Calibrate)…")
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_input")
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = showBookmarkedOnly,
                        onClick = { showBookmarkedOnly = !showBookmarkedOnly },
                        label = { Text("Bookmarked (${bookmarkedCommands.size})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("vault_bookmarked_filter_chip")
                    )
                }
                items(softwareFilters) { filter ->
                    FilterChip(
                        selected = selectedSoftwareFilter == filter,
                        onClick = { selectedSoftwareFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        items(filteredCommands, key = { it.id }) { cmd ->
            val isBookmarked = bookmarkedCommands.any { it.id == cmd.id }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlueprintNavyDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_command_card_${cmd.id}")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "${cmd.software} (${cmd.versionScope})",
                                style = MaterialTheme.typography.labelMedium,
                                color = BlueprintNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(
                            onClick = { onToggleCatalogBookmark(cmd, isBookmarked) }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Toggle bookmark",
                                tint = SafetyAmber
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Command: ${cmd.command}  |  Shortcut: ${cmd.shortcut}",
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
                        text = "Site Example: ${cmd.siteExample}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD0E2F2)
                    )
                    Text(
                        text = "Practice Exercise: ${cmd.practiceExercise}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafetyAmber
                    )
                }
            }
        }
    }
}
