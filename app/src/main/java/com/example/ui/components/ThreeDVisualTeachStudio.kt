package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CivilCurriculumCatalog
import com.example.data.DiplomaTrack
import com.example.data.SoftwareCategory
import com.example.data.TopicLessonModule
import com.example.data.Visual3DCallout
import com.example.data.Visual3DTeachScene
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SiteGreen
import kotlin.math.cos
import kotlin.math.sin

fun resolve3DTeachDrawableRes(imageAssetType: String): Int = when (imageAssetType) {
    "RCC_REBAR" -> R.drawable.img_3d_rcc_rebar_1791351512221
    "CLASH_NAVISWORKS" -> R.drawable.img_3d_clash_navisworks_1791351526358
    "BIM_LEVELS" -> R.drawable.img_3d_bim_levels_1791351541070
    else -> R.drawable.img_hero_bim_1791347759089
}

enum class ViewportVisualStyle(val label: String) {
    SHADED_BIM("3D Shaded BIM"),
    XRAY_REBAR("X-Ray Rebar/Clash"),
    CAD_WIREFRAME("CAD Wireframe")
}

@Composable
fun ThreeDVisualTeachGallery(
    selectedDiplomaTrack: DiplomaTrack,
    selectedSoftwareCategory: SoftwareCategory,
    onSelectDiplomaTrack: (DiplomaTrack) -> Unit,
    onSelectSoftwareCategory: (SoftwareCategory) -> Unit,
    onOpenModule: (TopicLessonModule) -> Unit,
    onAskTutorAbout3D: (Visual3DTeachScene, Visual3DCallout, TopicLessonModule) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null
) {
    val filteredScenes = remember(selectedDiplomaTrack, selectedSoftwareCategory) {
        CivilCurriculumCatalog.visual3DScenes.filter { scene ->
            val matchesTrack = selectedDiplomaTrack == DiplomaTrack.ALL_TRACKS ||
                scene.diplomaTracks.contains(selectedDiplomaTrack)
            val matchesSoftware = selectedSoftwareCategory == SoftwareCategory.ALL ||
                scene.softwareCategory == selectedSoftwareCategory
            matchesTrack && matchesSoftware
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("three_d_teach_gallery_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (headerContent != null) {
            item(key = "gallery_header") {
                headerContent()
            }
        }

        // Track & Software Quick Filter Bar for 3D Studio
        item(key = "gallery_filters") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = SafetyAmber
                        )
                        Column {
                            Text(
                                text = "3D Visual & Interactive CAD/BIM Teach Studio",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap numbered 3D pins on renders or drag to orbit 3D structural, rebar, and clash models.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD0E2F2)
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DiplomaTrack.entries, key = { it.id }) { track ->
                            val selected = track == selectedDiplomaTrack
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) SafetyAmber else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clickable { onSelectDiplomaTrack(track) }
                                    .testTag("3d_studio_track_chip_${track.id}")
                            ) {
                                Text(
                                    text = track.shortLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) BlueprintNavyDark else Color.White,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SoftwareCategory.entries, key = { it.id }) { cat ->
                            FilterChip(
                                selected = selectedSoftwareCategory == cat,
                                onClick = { onSelectSoftwareCategory(cat) },
                                label = { Text(cat.shortLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BlueprintCyan,
                                    selectedLabelColor = BlueprintNavyDark,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        items(filteredScenes, key = { it.id }) { scene ->
            val module = remember(scene.moduleId) {
                CivilCurriculumCatalog.topics.firstOrNull { it.id == scene.moduleId }
            }
            ThreeDVisualTeachCard(
                scene = scene,
                onAskTutorAbout3D = { sc, callout ->
                    if (module != null) {
                        onAskTutorAbout3D(sc, callout, module)
                    }
                },
                onOpenFullModule = if (module != null) {
                    { onOpenModule(module) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreeDVisualTeachCard(
    scene: Visual3DTeachScene,
    onAskTutorAbout3D: (Visual3DTeachScene, Visual3DCallout) -> Unit,
    onOpenFullModule: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCalloutIndex by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    var showInteractiveOrbitCanvas by rememberSaveable(scene.id) { mutableStateOf(false) }
    var activeStageIndex by rememberSaveable(scene.id) { mutableIntStateOf(3) }

    val activeCallout = scene.callouts.getOrElse(selectedCalloutIndex) { scene.callouts.first() }
    val drawableRes = resolve3DTeachDrawableRes(scene.imageAssetType)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("three_d_teach_card_${scene.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Software Category + 3D Badge + Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BlueprintNavyDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "3D IMAGE TEACH • ${scene.softwareCategory.displayName.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SafetyAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Switch between Annotated 3D Render Image and Interactive 3D Orbit Model
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .clickable { showInteractiveOrbitCanvas = !showInteractiveOrbitCanvas }
                        .testTag("toggle_3d_mode_${scene.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (showInteractiveOrbitCanvas) Icons.Default.Visibility else Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (showInteractiveOrbitCanvas) "Show 3D Render" else "Interactive 3D Orbit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scene Title & Subtitle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = scene.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scene.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!showInteractiveOrbitCanvas) {
                // PART A: Annotated 3D Render Diagram with Interactive Hotspot Pins
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("annotated_3d_image_box_${scene.id}")
                ) {
                    val boxWidth = maxWidth
                    val boxHeight = maxHeight

                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = scene.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle dark gradient at top & bottom for legibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        BlueprintNavyDark.copy(alpha = 0.55f),
                                        Color.Transparent,
                                        BlueprintNavyDark.copy(alpha = 0.70f)
                                    )
                                )
                            )
                    )

                    // Top-left HUD badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .background(BlueprintNavyDark.copy(alpha = 0.82f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tap numbered 3D pins (1–${scene.callouts.size}) to inspect",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    // Interactive Numbered 3D Callout Pins overlaid on the 3D render
                    scene.callouts.forEachIndexed { idx, callout ->
                        val isSelectedPin = idx == selectedCalloutIndex
                        val pinOffsetX = (boxWidth * callout.xFraction) - 18.dp
                        val pinOffsetY = (boxHeight * callout.yFraction) - 18.dp

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(x = pinOffsetX, y = pinOffsetY)
                                .size(if (isSelectedPin) 38.dp else 32.dp)
                                .clip(CircleShape)
                                .background(if (isSelectedPin) SafetyAmber else BlueprintNavyDark.copy(alpha = 0.9f))
                                .border(
                                    width = 2.dp,
                                    color = if (isSelectedPin) Color.White else BlueprintCyan,
                                    shape = CircleShape
                                )
                                .clickable { selectedCalloutIndex = idx }
                                .testTag("callout_pin_${scene.id}_${callout.number}")
                        ) {
                            Text(
                                text = "${callout.number}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelectedPin) BlueprintNavyDark else Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Bottom HUD showing currently selected 3D element
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BlueprintNavyDark.copy(alpha = 0.88f)
                        ) {
                            Text(
                                text = "Pin #${activeCallout.number}: ${activeCallout.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SafetyAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            } else {
                // PART B: Interactive 3D Orbiting CAD/BIM Viewport
                Interactive3DCadViewport(
                    scene = scene,
                    activeStageIndex = activeStageIndex,
                    onStageChange = { activeStageIndex = it }
                )
            }

            // Selectable 3D Callout Chips Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scene.callouts.forEachIndexed { idx, callout ->
                    val selected = idx == selectedCalloutIndex
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCalloutIndex = idx },
                        label = { Text("#${callout.number} ${callout.label}") },
                        modifier = Modifier.testTag("callout_chip_${scene.id}_${callout.number}")
                    )
                }
            }

            // Active 3D Callout Deep-Dive Teaching Panel
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BlueprintNavyDark,
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_3d_callout_panel_${scene.id}")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SafetyAmber,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${activeCallout.number}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BlueprintNavyDark,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = activeCallout.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = SafetyAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = activeCallout.explanationSimple,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color(0xFF90E0EF),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Command / Rule: ${activeCallout.exactCommandOrRule}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF90E0EF)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Site Impact: ${activeCallout.siteImpact}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0E2F2)
                        )
                    }
                }
            }

            // Action Buttons: Open Lab Module or Teach This 3D View in AI Tutor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onOpenFullModule != null) {
                    OutlinedButton(
                        onClick = onOpenFullModule,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_module_from_3d_${scene.id}")
                    ) {
                        Text("Open Full Lab")
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Button(
                    onClick = { onAskTutorAbout3D(scene, activeCallout) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ask_tutor_3d_${scene.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Teach 3D in AI Tutor")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Interactive3DCadViewport(
    scene: Visual3DTeachScene,
    activeStageIndex: Int,
    onStageChange: (Int) -> Unit
) {
    var yawDegrees by rememberSaveable(scene.id) { mutableFloatStateOf(38f) }
    var pitchDegrees by rememberSaveable(scene.id) { mutableFloatStateOf(24f) }
    var explodeFactor by rememberSaveable(scene.id) { mutableFloatStateOf(0.15f) }
    var visualStyle by rememberSaveable(scene.id) { mutableStateOf(ViewportVisualStyle.XRAY_REBAR) }

    var showGrids by rememberSaveable(scene.id) { mutableStateOf(true) }
    var showFootings by rememberSaveable(scene.id) { mutableStateOf(true) }
    var showColumnsAndRebar by rememberSaveable(scene.id) { mutableStateOf(true) }
    var showBeamsAndSlab by rememberSaveable(scene.id) { mutableStateOf(true) }
    var showMepClash by rememberSaveable(scene.id) {
        mutableStateOf(scene.modelPreset == "CLASH_4D")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlueprintNavyDark)
            .border(1.5.dp, BlueprintCyan.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Viewport Bar: Instructions + Reset 3D Camera
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
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = SafetyAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Drag to Orbit 3D Model (Yaw ${yawDegrees.toInt()}° • Pitch ${pitchDegrees.toInt()}°)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
            IconButton(
                onClick = {
                    yawDegrees = 38f
                    pitchDegrees = 24f
                    explodeFactor = 0.15f
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset 3D View",
                    tint = SafetyAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Interactive 3D Projection Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF06111C))
                .pointerInput(scene.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        yawDegrees = (yawDegrees + dragAmount.x * 0.45f) % 360f
                        pitchDegrees = (pitchDegrees + dragAmount.y * 0.35f).coerceIn(8f, 65f)
                    }
                }
                .testTag("interactive_3d_canvas_${scene.id}")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                draw3DBimStructuralScene(
                    yawDeg = yawDegrees,
                    pitchDeg = pitchDegrees,
                    explode = explodeFactor,
                    visualStyle = visualStyle,
                    stageIndex = activeStageIndex,
                    showGrids = showGrids,
                    showFootings = showFootings,
                    showColumnsAndRebar = showColumnsAndRebar,
                    showBeamsAndSlab = showBeamsAndSlab,
                    showMepClash = showMepClash
                )
            }

            // Legend Overlay in Corner
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(BlueprintNavyDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Cyan: S-GRID | Blue: Concrete | Amber: Rebar | Red: MEP Clash",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF90E0EF)
                )
            }
        }

        // 3D Stage Stepper
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scene.stageDescriptions.forEachIndexed { idx, _ ->
                    val selected = idx == activeStageIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) SafetyAmber else Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStageChange(idx) }
                    ) {
                        Text(
                            text = "Stage ${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) BlueprintNavyDark else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
            Text(
                text = scene.stageDescriptions.getOrElse(activeStageIndex) { "" },
                style = MaterialTheme.typography.bodySmall,
                color = SafetyAmber
            )
        }

        // Visual Style & Exploded View Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Explode 3D Levels:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Slider(
                value = explodeFactor,
                onValueChange = { explodeFactor = it },
                valueRange = 0f..0.8f,
                modifier = Modifier.weight(1f)
            )
        }

        // 3D Visual Style Switcher
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ViewportVisualStyle.entries.forEach { style ->
                val isSelected = visualStyle == style
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) BlueprintCyan else Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { visualStyle = style }
                ) {
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) BlueprintNavyDark else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 3D Layer Visibility Toggles
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LayerTogglePill("S-GRID", showGrids) { showGrids = !showGrids }
            LayerTogglePill("3D Footings", showFootings) { showFootings = !showFootings }
            LayerTogglePill("Columns & Rebar", showColumnsAndRebar) { showColumnsAndRebar = !showColumnsAndRebar }
            LayerTogglePill("Beams & Slab", showBeamsAndSlab) { showBeamsAndSlab = !showBeamsAndSlab }
            LayerTogglePill("MEP Clash Pipe", showMepClash) { showMepClash = !showMepClash }
        }
    }
}

@Composable
private fun LayerTogglePill(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (checked) SiteGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (checked) SiteGreen else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onToggle() }
    ) {
        Text(
            text = if (checked) "✓ $label" else label,
            style = MaterialTheme.typography.labelSmall,
            color = if (checked) Color(0xFFB7F4C8) else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private data class Vec3(val x: Float, val y: Float, val z: Float)

private fun DrawScope.draw3DBimStructuralScene(
    yawDeg: Float,
    pitchDeg: Float,
    explode: Float,
    visualStyle: ViewportVisualStyle,
    stageIndex: Int,
    showGrids: Boolean,
    showFootings: Boolean,
    showColumnsAndRebar: Boolean,
    showBeamsAndSlab: Boolean,
    showMepClash: Boolean
) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.62f
    val scale = size.minDimension * 0.34f

    val yawRad = Math.toRadians(yawDeg.toDouble())
    val pitchRad = Math.toRadians(pitchDeg.toDouble())
    val cosYaw = cos(yawRad).toFloat()
    val sinYaw = sin(yawRad).toFloat()
    val cosPitch = cos(pitchRad).toFloat()
    val sinPitch = sin(pitchRad).toFloat()

    fun project(v: Vec3): Offset {
        val rx = v.x * cosYaw - v.y * sinYaw
        val ry = v.x * sinYaw + v.y * cosYaw
        val screenX = cx + rx * scale
        val screenY = cy - (v.z * cosPitch - ry * sinPitch) * scale
        return Offset(screenX, screenY)
    }

    fun draw3DBox(
        centerX: Float,
        centerY: Float,
        baseZ: Float,
        dx: Float,
        dy: Float,
        dz: Float,
        fillColor: Color,
        strokeColor: Color
    ) {
        val hx = dx / 2f
        val hy = dy / 2f
        val b0 = project(Vec3(centerX - hx, centerY - hy, baseZ))
        val b1 = project(Vec3(centerX + hx, centerY - hy, baseZ))
        val b2 = project(Vec3(centerX + hx, centerY + hy, baseZ))
        val b3 = project(Vec3(centerX - hx, centerY + hy, baseZ))

        val t0 = project(Vec3(centerX - hx, centerY - hy, baseZ + dz))
        val t1 = project(Vec3(centerX + hx, centerY - hy, baseZ + dz))
        val t2 = project(Vec3(centerX + hx, centerY + hy, baseZ + dz))
        val t3 = project(Vec3(centerX - hx, centerY + hy, baseZ + dz))

        if (visualStyle != ViewportVisualStyle.CAD_WIREFRAME) {
            val alphaMult = if (visualStyle == ViewportVisualStyle.XRAY_REBAR) 0.36f else 0.78f
            val topPath = Path().apply {
                moveTo(t0.x, t0.y)
                lineTo(t1.x, t1.y)
                lineTo(t2.x, t2.y)
                lineTo(t3.x, t3.y)
                close()
            }
            drawPath(topPath, fillColor.copy(alpha = alphaMult))

            val side1 = Path().apply {
                moveTo(b1.x, b1.y)
                lineTo(b2.x, b2.y)
                lineTo(t2.x, t2.y)
                lineTo(t1.x, t1.y)
                close()
            }
            drawPath(side1, fillColor.copy(alpha = alphaMult * 0.85f))

            val side2 = Path().apply {
                moveTo(b2.x, b2.y)
                lineTo(b3.x, b3.y)
                lineTo(t3.x, t3.y)
                lineTo(t2.x, t2.y)
                close()
            }
            drawPath(side2, fillColor.copy(alpha = alphaMult * 0.7f))
        }

        val edges = listOf(
            b0 to b1, b1 to b2, b2 to b3, b3 to b0,
            t0 to t1, t1 to t2, t2 to t3, t3 to t0,
            b0 to t0, b1 to t1, b2 to t2, b3 to t3
        )
        edges.forEach { (p1, p2) ->
            drawLine(strokeColor, p1, p2, strokeWidth = 2f)
        }
    }

    val gridOffsets = listOf(-0.65f, 0.65f)

    if (showGrids) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        gridOffsets.forEach { g ->
            drawLine(
                color = BlueprintCyan.copy(alpha = 0.75f),
                start = project(Vec3(-1.05f, g, 0f)),
                end = project(Vec3(1.05f, g, 0f)),
                strokeWidth = 2f,
                pathEffect = dash
            )
            drawLine(
                color = BlueprintCyan.copy(alpha = 0.75f),
                start = project(Vec3(g, -1.05f, 0f)),
                end = project(Vec3(g, 1.05f, 0f)),
                strokeWidth = 2f,
                pathEffect = dash
            )
        }
    }

    if (showFootings && stageIndex >= 1) {
        val footingZ = -0.18f - explode * 0.25f
        for (gx in gridOffsets) {
            for (gy in gridOffsets) {
                draw3DBox(
                    centerX = gx,
                    centerY = gy,
                    baseZ = footingZ,
                    dx = 0.42f,
                    dy = 0.42f,
                    dz = 0.16f,
                    fillColor = Color(0xFF2B5B84),
                    strokeColor = BlueprintCyan
                )
            }
        }
    }

    if (showColumnsAndRebar && stageIndex >= 2) {
        val colBaseZ = 0f + explode * 0.15f
        val colHeight = 0.82f
        for (gx in gridOffsets) {
            for (gy in gridOffsets) {
                draw3DBox(
                    centerX = gx,
                    centerY = gy,
                    baseZ = colBaseZ,
                    dx = 0.18f,
                    dy = 0.18f,
                    dz = colHeight,
                    fillColor = Color(0xFF3E7CB1),
                    strokeColor = Color(0xFF90E0EF)
                )

                if (visualStyle != ViewportVisualStyle.SHADED_BIM) {
                    val rebarOffsets = listOf(-0.055f, 0.055f)
                    for (rx in rebarOffsets) {
                        for (ry in rebarOffsets) {
                            drawLine(
                                color = SafetyAmber,
                                start = project(Vec3(gx + rx, gy + ry, colBaseZ - 0.05f)),
                                end = project(Vec3(gx + rx, gy + ry, colBaseZ + colHeight + 0.06f)),
                                strokeWidth = 2.4f
                            )
                        }
                    }
                    listOf(0.18f, 0.42f, 0.66f).forEach { sz ->
                        val s0 = project(Vec3(gx - 0.06f, gy - 0.06f, colBaseZ + sz))
                        val s1 = project(Vec3(gx + 0.06f, gy - 0.06f, colBaseZ + sz))
                        val s2 = project(Vec3(gx + 0.06f, gy + 0.06f, colBaseZ + sz))
                        val s3 = project(Vec3(gx - 0.06f, gy + 0.06f, colBaseZ + sz))
                        drawLine(SafetyAmber.copy(alpha = 0.85f), s0, s1, 1.5f)
                        drawLine(SafetyAmber.copy(alpha = 0.85f), s1, s2, 1.5f)
                        drawLine(SafetyAmber.copy(alpha = 0.85f), s2, s3, 1.5f)
                        drawLine(SafetyAmber.copy(alpha = 0.85f), s3, s0, 1.5f)
                    }
                }
            }
        }
    }

    if (showBeamsAndSlab && stageIndex >= 3) {
        val beamZ = 0.82f + explode * 0.45f
        gridOffsets.forEach { gy ->
            draw3DBox(
                centerX = 0f,
                centerY = gy,
                baseZ = beamZ,
                dx = 1.48f,
                dy = 0.14f,
                dz = 0.16f,
                fillColor = Color(0xFF4A90E2),
                strokeColor = Color(0xFFCAF0F8)
            )
        }
        gridOffsets.forEach { gx ->
            draw3DBox(
                centerX = gx,
                centerY = 0f,
                baseZ = beamZ,
                dx = 0.14f,
                dy = 1.48f,
                dz = 0.16f,
                fillColor = Color(0xFF4A90E2),
                strokeColor = Color(0xFFCAF0F8)
            )
        }
    }

    if (showMepClash && stageIndex >= 2) {
        val pipeZ = 0.88f + explode * 0.45f
        val pStart = project(Vec3(-1.05f, 0.15f, pipeZ))
        val pEnd = project(Vec3(1.05f, -0.25f, pipeZ))
        drawLine(
            color = Color(0xFF00F5D4),
            start = pStart,
            end = pEnd,
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        val clashCenter = project(Vec3(0.65f, -0.17f, pipeZ))
        drawCircle(
            color = AlertRed.copy(alpha = 0.45f),
            radius = 22f,
            center = clashCenter
        )
        drawCircle(
            color = SafetyAmber,
            radius = 12f,
            center = clashCenter,
            style = Stroke(width = 3f)
        )
        drawCircle(
            color = AlertRed,
            radius = 6f,
            center = clashCenter
        )
    }

    val gizmoOrigin = Offset(size.width - 42f, 42f)
    val gxEnd = Offset(gizmoOrigin.x + cosYaw * 22f, gizmoOrigin.y + sinYaw * sinPitch * 22f)
    val gyEnd = Offset(gizmoOrigin.x - sinYaw * 22f, gizmoOrigin.y + cosYaw * sinPitch * 22f)
    val gzEnd = Offset(gizmoOrigin.x, gizmoOrigin.y - cosPitch * 24f)
    drawLine(AlertRed, gizmoOrigin, gxEnd, strokeWidth = 3f)
    drawLine(SiteGreen, gizmoOrigin, gyEnd, strokeWidth = 3f)
    drawLine(BlueprintCyan, gizmoOrigin, gzEnd, strokeWidth = 3f)
}
