package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class StudyMode(val label: String, val subtitle: String) {
    UNSELECTED("Choose Mode", "Select how you want to study today"),
    LEARN_TOPIC("Learn a Topic", "Simple English, real site examples & quick checks"),
    PRACTICE_SOFTWARE("Practice Software", "Version-specific commands, menu paths & exercises"),
    MOCK_INTERVIEW("Mock Interview", "1-by-1 Technical + HR questions scored out of 10")
}

enum class DiplomaTrack(
    val id: String,
    val title: String,
    val shortLabel: String,
    val semesterBadge: String,
    val description: String,
    val coreSoftwareSummary: String
) {
    ALL_TRACKS(
        id = "all_tracks",
        title = "All Diploma Tracks",
        shortLabel = "All Tracks",
        semesterBadge = "Full Diploma",
        description = "Complete Civil Engineering & BIM Diploma index across 2D/3D CAD, Parametric BIM, 4D/5D Coordination, and ISO 19650 Handover.",
        coreSoftwareSummary = "AutoCAD • Revit • Navisworks • MicroStation • Bluebeam • CDE/COBie"
    ),
    CAD_STRUCTURAL_DRAFTING(
        id = "cad_drafting",
        title = "Structural CAD & Infrastructure Drafting",
        shortLabel = "CAD & Drafting",
        semesterBadge = "Track 1 • Sem 3",
        description = "Focuses on 2D structural grid drafting, 3D foundation solids, highway/rail DGN alignments, and calibrated PDF site takeoffs.",
        coreSoftwareSummary = "AutoCAD • MicroStation • Bluebeam Revu • Revit Structure"
    ),
    BIM_MODELING_AUTHORING(
        id = "bim_authoring",
        title = "3D BIM Authoring & Rebar Detailing",
        shortLabel = "BIM Authoring",
        semesterBadge = "Track 2 • Sem 4",
        description = "Focuses on parametric RCC/Architectural modeling, levels & grids, structural rebar detailing, and batch sheet/Excel automation.",
        coreSoftwareSummary = "Revit • Revit Plugins (pyRevit/DiRoots) • AutoCAD 3D"
    ),
    BIM_COORDINATION_4D_5D(
        id = "bim_coordination",
        title = "4D/5D BIM Coordination & QS Takeoff",
        shortLabel = "4D/5D & Clash",
        semesterBadge = "Track 3 • Sem 5",
        description = "Focuses on federated NWF/NWD models, hard & clearance clash detection, TimeLiner 4D simulation, and 5D/PDF quantity takeoffs.",
        coreSoftwareSummary = "Navisworks • Bluebeam Revu • Revit NWC Exporter"
    ),
    DIGITAL_DELIVERY_ISO_FM(
        id = "digital_delivery",
        title = "ISO 19650 CDE & 6D Facility Handover",
        shortLabel = "CDE & 6D FM",
        semesterBadge = "Track 4 • Sem 6",
        description = "Focuses on Common Data Environment (WIP/Shared/Published) governance, multi-discipline design review, and COBie 2.4 asset handover.",
        coreSoftwareSummary = "CDE (ISO 19650) • COBie • Navisworks • Bluebeam Revu"
    )
}

enum class SoftwareCategory(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val tagline: String
) {
    ALL(
        id = "all",
        displayName = "All Software",
        shortLabel = "All",
        tagline = "Complete multi-platform Civil & BIM index"
    ),
    AUTOCAD(
        id = "autocad",
        displayName = "AutoCAD",
        shortLabel = "AutoCAD",
        tagline = "2D Structural Drafting, Layers & 3D Solid Modeling"
    ),
    REVIT(
        id = "revit",
        displayName = "Revit",
        shortLabel = "Revit",
        tagline = "3D Parametric BIM Authoring, Rebar & Automation Plugins"
    ),
    NAVISWORKS(
        id = "navisworks",
        displayName = "Navisworks",
        shortLabel = "Navisworks",
        tagline = "Federated Clash Detection, 4D TimeLiner & 5D Quantification"
    ),
    MICROSTATION(
        id = "microstation",
        displayName = "MicroStation",
        shortLabel = "MicroStation",
        tagline = "Infrastructure DGN CAD for Highways, Bridges & Metro Rail"
    ),
    BLUEBEAM(
        id = "bluebeam",
        displayName = "Bluebeam Revu",
        shortLabel = "Bluebeam",
        tagline = "Smart PDF Scale Calibration, Dynamic Fill QTO & Drawing Overlay"
    ),
    CDE_COBIE(
        id = "cde_cobie",
        displayName = "CDE & COBie",
        shortLabel = "CDE / COBie",
        tagline = "ISO 19650 Information Containers & 6D Asset Data Drop"
    )
}

enum class StudyMaterialType(val id: String, val label: String) {
    ALL("all", "All Materials"),
    CONCEPT_GUIDE("concept", "Concept Guide"),
    STEP_WORKFLOW("workflow", "Step Workflow"),
    COMMAND_REFERENCE("command", "Command Reference"),
    SITE_CASE_STUDY("site_case", "Site Case Study"),
    PRACTICE_EXERCISE("exercise", "Practice Lab"),
    QUICK_CHECK("quiz", "Quick Check")
}

data class StudyMaterialIndexItem(
    val id: String,
    val parentModuleId: String,
    val title: String,
    val summary: String,
    val softwareCategory: SoftwareCategory,
    val softwareVersionTag: String,
    val materialType: StudyMaterialType,
    val diplomaTracks: List<DiplomaTrack>,
    val estimatedMinutes: Int,
    val keywords: List<String>,
    val commandHighlight: String = "",
    val recommendedMode: StudyMode = StudyMode.LEARN_TOPIC
)

data class SoftwareCommandItem(
    val id: String,
    val software: String,
    val versionScope: String,
    val taskTitle: String,
    val command: String,
    val shortcut: String,
    val menuPath: String,
    val siteExample: String,
    val practiceExercise: String
)

data class TopicLessonModule(
    val id: String,
    val title: String,
    val shortName: String,
    val badge: String,
    val requiresSoftwareVersion: Boolean,
    val supportedVersions: List<String>,
    val overviewSimple: String,
    val realSiteExample: String,
    val smallSteps: List<String>,
    val keyCommands: List<SoftwareCommandItem>,
    val practiceExercise: String,
    val quickCheckQuestion: String,
    val quickCheckOptions: List<String>,
    val quickCheckCorrectIndex: Int,
    val quickCheckExplanation: String
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String,
    val selectedTopic: String,
    val softwareVersion: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val isUser: Boolean,
    val mainText: String,
    val siteExample: String = "",
    val smallStepsFormatted: String = "",
    val commandsMenuShortcuts: String = "",
    val practiceExercise: String = "",
    val quickCheckQuestion: String = "",
    val asksSoftwareVersion: Boolean = false,
    val suggestedSoftwareName: String = "",
    val interviewScore: Int? = null,
    val interviewGoodPoints: String = "",
    val interviewBetterAnswer: String = "",
    val interviewQuestionType: String = "", // "Technical" or "HR"
    val nextInterviewQuestion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarked_commands")
data class BookmarkedCommandEntity(
    @PrimaryKey val id: String,
    val software: String,
    val versionScope: String,
    val taskTitle: String,
    val command: String,
    val shortcut: String,
    val menuPath: String,
    val siteExample: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_attempts")
data class InterviewAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val questionType: String, // "Technical" or "HR"
    val questionAsked: String,
    val studentAnswer: String,
    val scoreOutOf10: Int,
    val whatWasGood: String,
    val betterSampleAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class TutorStructuredReply(
    val mainExplanation: String = "",
    val smallSteps: List<String> = emptyList(),
    val realSiteExample: String = "",
    val commandsAndShortcuts: String = "",
    val practiceExercise: String = "",
    val quickCheckQuestion: String = "",
    val asksSoftwareVersion: Boolean = false,
    val targetSoftwareName: String = "",
    val interviewScoreOutOf10: Int? = null,
    val interviewWhatWasGood: String = "",
    val interviewBetterSampleAnswer: String = "",
    val interviewQuestionCategory: String = "",
    val nextInterviewQuestion: String = "",
    val uncertaintyNote: String = ""
)

object CivilCurriculumCatalog {
    val topics: List<TopicLessonModule> = listOf(
        TopicLessonModule(
            id = "autocad",
            title = "AutoCAD 2D and 3D",
            shortName = "AutoCAD 2D/3D",
            badge = "CAD Drafting",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("AutoCAD 2025", "AutoCAD 2024", "AutoCAD 2023", "AutoCAD 2022 / LT"),
            overviewSimple = "AutoCAD is our digital drawing board. In 2D, we draft floor plans, column grid layouts, and reinforcement details. In 3D, we extrude walls, footings, and stairs to visualize site levels before concrete is poured.",
            realSiteExample = "On a G+3 residential building site, the site engineer uses a 2D AutoCAD Structural Grid Drawing (Grid A-D and 1-5) to mark excavation pits for isolated footings using lime powder.",
            smallSteps = listOf(
                "Step 1: Set your drawing units to Millimeters (mm) or Meters (m) matching the site survey sheet.",
                "Step 2: Create dedicated Layers (e.g., S-GRID in red dashed line, S-COL in yellow thick line, A-WALL in white).",
                "Step 3: Draft the structural centerline grid using Construction Lines (XLINE) and Offset (O) for exact bay spacing.",
                "Step 4: Place rectangular RCC columns (REC) at grid intersections and switch to 3D Modeling workspace to EXTRUDE footings and columns."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "acad_units",
                    software = "AutoCAD 2D/3D",
                    versionScope = "2022–2025",
                    taskTitle = "Set Site Drawing Units & Precision",
                    command = "UNITS",
                    shortcut = "UN + Enter",
                    menuPath = "Application Button (A) > Drawing Utilities > Units",
                    siteExample = "Setting Decimal & Millimeters before drafting a 230mm brick wall or 300x450mm RCC column.",
                    practiceExercise = "Open a blank drawing, type UN, set Type to Decimal, Precision to 0, and Insertion Scale to Millimeters."
                ),
                SoftwareCommandItem(
                    id = "acad_layer",
                    software = "AutoCAD 2D/3D",
                    versionScope = "2022–2025",
                    taskTitle = "Layer Properties Manager",
                    command = "LAYER",
                    shortcut = "LA + Enter",
                    menuPath = "Home Tab > Layers Panel > Layer Properties",
                    siteExample = "Separating 'A-WALL-BRICK', 'S-COLUMN-RCC', and 'S-GRID-CENTER' so the structural engineer can freeze furniture layers.",
                    practiceExercise = "Create 3 layers: S-GRID (Center2 linetype, Red), S-COLUMN (Continuous, 0.40mm lineweight), and A-WALL (0.25mm)."
                ),
                SoftwareCommandItem(
                    id = "acad_offset",
                    software = "AutoCAD 2D/3D",
                    versionScope = "2022–2025",
                    taskTitle = "Parallel Wall & Grid Spacing",
                    command = "OFFSET",
                    shortcut = "O + Enter",
                    menuPath = "Home Tab > Modify Panel > Offset",
                    siteExample = "Offsetting a grid line by 4000mm for column bay spacing, or offsetting a room line by 230mm for an external brick wall.",
                    practiceExercise = "Draw a 5000mm horizontal line, use O + Enter with distance 230mm to create a double-line outer masonry wall."
                ),
                SoftwareCommandItem(
                    id = "acad_extrude",
                    software = "AutoCAD 2D/3D",
                    versionScope = "2022–2025 (Full 3D)",
                    taskTitle = "Extrude 2D Footing/Column to 3D Solid",
                    command = "EXTRUDE",
                    shortcut = "EXT + Enter",
                    menuPath = "Workspace Gear > 3D Modeling > Home Tab > Modeling Panel > Extrude",
                    siteExample = "Turning a closed 1500x1500mm 2D polyline footing into a 450mm deep 3D concrete pad foundation.",
                    practiceExercise = "Draw a closed 1500x1500mm rectangle (REC), switch to SE Isometric view, and EXTRUDE it upward by 450mm."
                )
            ),
            practiceExercise = "Draft a 2-bay by 1-bay structural grid (4000mm x 3500mm) in mm. Place 300x300mm columns at all 6 grid intersections on layer 'S-COLUMN', then extrude the columns to 3000mm floor height.",
            quickCheckQuestion = "If you want to extrude a 2D column boundary into a 3D solid in AutoCAD, instead of getting hollow 3D surfaces, what must be true about the 2D boundary?",
            quickCheckOptions = listOf(
                "It must be a single closed polyline or region",
                "It must be drawn on Layer 0 in red color",
                "It must be exploded into 4 separate lines first",
                "It must have dimensions attached to it"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Correct! EXTRUDE creates a solid 3D RCC column only when the profile is a closed Polyline (PL/REC) or Region. Separate unjoined lines extrude as paper-thin surfaces."
        ),
        TopicLessonModule(
            id = "microstation",
            title = "MicroStation (Infrastructure CAD)",
            shortName = "MicroStation",
            badge = "Highway & Rail",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("MicroStation 2024", "MicroStation 2023", "MicroStation CONNECT Edition", "MicroStation V8i"),
            overviewSimple = "MicroStation (by Bentley) is the powerhouse CAD tool used on mega civil infrastructure projects—highways, metro rail viaducts, bridges, and water pipelines—working natively with .DGN files and real-world survey coordinates.",
            realSiteExample = "On a Metro Rail Viaduct project, all pier locations, utility corridors, and station footprints are referenced together using MicroStation Reference Attachments (.DGN) at true global survey coordinates.",
            smallSteps = listOf(
                "Step 1: Open or create a .DGN file using the project's 2D or 3D Seed File (which pre-sets Working Units like Master Units = Meters, Sub Units = Millimeters).",
                "Step 2: Use Level Manager (similar to AutoCAD Layers) to organize Highway Centerlines, Curb Stones, and Stormwater Drains.",
                "Step 3: Master AccuDraw (press Spacebar to toggle Rectangular X/Y vs Polar Distance/Angle mode) for precision alignment drafting.",
                "Step 4: Attach survey and utility drawings via the References dialog so you never move the real site coordinate origin."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "ms_smartline",
                    software = "MicroStation",
                    versionScope = "CONNECT / 2023 / 2024",
                    taskTitle = "Place SmartLine (Lines, Arcs & Vertices)",
                    command = "PLACE SMARTLINE",
                    shortcut = "Key-in: place smartline (or Q -> 1 in task workflow)",
                    menuPath = "Drawing Workflow > Home Tab > Placement Group > Place SmartLine",
                    siteExample = "Tracing a continuous highway plot boundary or box-culvert outline with both straight segments and rounded curb arcs.",
                    practiceExercise = "Activate Place SmartLine, click a start point, press Enter on AccuDraw to lock direction, and type 12.5m for a culvert edge."
                ),
                SoftwareCommandItem(
                    id = "ms_accudraw",
                    software = "MicroStation",
                    versionScope = "CONNECT / 2023 / 2024",
                    taskTitle = "AccuDraw Compass Shortcuts",
                    command = "ACCUDRAW",
                    shortcut = "Spacebar (Mode), O (Set Origin), T (Top), F (Front)",
                    menuPath = "Drawing Workflow > Home Tab > Primary Group > More > AccuDraw",
                    siteExample = "Measuring a 3.5m carriageway lane width perpendicular to an angled bridge abutment.",
                    practiceExercise = "While placing a line, press 'O' at an existing pier corner to snap the AccuDraw compass origin, then move right and type 3.5."
                ),
                SoftwareCommandItem(
                    id = "ms_reference",
                    software = "MicroStation",
                    versionScope = "CONNECT / 2023 / 2024",
                    taskTitle = "Attach Survey Reference DGN",
                    command = "REFERENCE ATTACH",
                    shortcut = "Key-in: reference attach",
                    menuPath = "Drawing Workflow > Home Tab > Primary Group > Attach Tools > References",
                    siteExample = "Overlaying the topographical survey .DGN under your proposed highway drainage layout using 'Coincident - World'.",
                    practiceExercise = "Open the References dialog, click Attach Reference, and inspect the Orientation option 'Coincident - World'."
                )
            ),
            practiceExercise = "Create a new 2D Metric DGN file, open Level Manager to create a level 'DRAIN-RCC-BOX', and use Place SmartLine + AccuDraw to draw a 2.0m x 1.5m rectangular precast box culvert section.",
            quickCheckQuestion = "In MicroStation, what is the equivalent of an AutoCAD 'Layer' and what file sets the initial Working Units of a new .DGN?",
            quickCheckOptions = listOf(
                "Level & Seed File",
                "Workset & Template (.DWT)",
                "Cell & Family File",
                "Fence & Sheet Model"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Spot on! MicroStation uses 'Levels' instead of Layers, and every new .DGN is created from a 'Seed File' that defines Master/Sub working units and coordinate systems."
        ),
        TopicLessonModule(
            id = "revit_arch_struct",
            title = "Revit Architecture & Structure",
            shortName = "Revit Arch/Struct",
            badge = "BIM Authoring",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("Revit 2025", "Revit 2024", "Revit 2023", "Revit 2022"),
            overviewSimple = "In Revit, we don't draw dumb lines—we build a real 3D parametric building model (Walls, RCC Columns, Beams, Slabs, Footings). Changing a column size in the 3D view automatically updates every floor plan, section, and BOQ schedule!",
            realSiteExample = "When the structural engineer changes Column C4 from 300x450mm to 300x600mm for seismic safety, Revit automatically updates the Ground Floor Plan, Section A-A, and Concrete Volume Schedule simultaneously.",
            smallSteps = listOf(
                "Step 1: Set up Levels (Datum) first in an Elevation View (e.g., Plinth +0.60m, Ground Floor +0.00m, First Floor +3.30m) BEFORE placing any walls or columns.",
                "Step 2: Create Structural Grids (GR) in Plan View (A, B, C horizontally and 1, 2, 3 vertically).",
                "Step 3: Place Structural Columns (CL) at grid intersections (ensure setting is 'Height' and not 'Depth' when placing upward from Ground to First Floor!).",
                "Step 4: Model Structural Framing/Beams (BM), Isolated Footings (FT), and Architectural Walls (WA) hosted to the exact levels."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "revit_levels_grids",
                    software = "Revit Arch & Struct",
                    versionScope = "2022–2025",
                    taskTitle = "Create Story Levels & Column Grids",
                    command = "Level / Grid",
                    shortcut = "LL (Level) / GR (Grid)",
                    menuPath = "Architecture or Structure Tab > Datum Panel > Level / Grid",
                    siteExample = "Setting Top of Footing (-1.50m), Plinth Level (+0.45m), and Slab Top (+3.45m) matching site bench mark.",
                    practiceExercise = "Open South Elevation, press LL to add a 'First Floor' level at 3300mm above Level 1, then go to Level 1 Plan and press GR to draw a 3x3 grid."
                ),
                SoftwareCommandItem(
                    id = "revit_column_beam",
                    software = "Revit Arch & Struct",
                    versionScope = "2022–2025",
                    taskTitle = "Place Structural RCC Column & Beam",
                    command = "Structural Column / Beam",
                    shortcut = "CL (Column) / BM (Beam)",
                    menuPath = "Structure Tab > Structure Panel > Column / Beam",
                    siteExample = "Placing M30 grade 300x450mm RCC columns at all grid intersections using 'At Grids' multiple placement.",
                    practiceExercise = "Press CL, select Concrete-Rectangular-Column, Duplicate type to '300 x 450mm', click 'At Grids', and select your 9 grid intersections."
                ),
                SoftwareCommandItem(
                    id = "revit_rebar",
                    software = "Revit Arch & Struct",
                    versionScope = "2022–2025 (2023+ Freeform)",
                    taskTitle = "Place Structural Rebar & Cover",
                    command = "Rebar",
                    shortcut = "RB",
                    menuPath = "Structure Tab > Reinforcement Panel > Rebar / Cover",
                    siteExample = "Detailing 16mm dia main longitudinal bars and 8mm @ 150mm c/c stirrups inside a 400mm clear cover footing.",
                    practiceExercise = "Cut a Section through a column, select the column, click Rebar (RB), choose Stirrup shape T1, and place parallel to work plane."
                )
            ),
            practiceExercise = "In Revit, create a 2-story structural frame: 4m x 5m grid, 300x450mm RCC columns from Level 1 to Level 2, 230x450mm plinth/floor beams, and a 150mm thick RCC structural floor slab.",
            quickCheckQuestion = "When placing a Structural Column (CL) in Level 1 Floor Plan in Revit, which Options Bar setting prevents the column from accidentally going underground instead of up to Level 2?",
            quickCheckOptions = listOf(
                "Change 'Depth' to 'Height' and set constraint to Level 2",
                "Turn off Analytical Model visibility",
                "Set Visual Style to Wireframe",
                "Check 'Room Bounding' in Properties"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Exactly right! By default, Structural Columns often default to 'Depth'. Changing the Options Bar dropdown from 'Depth' to 'Height' (constrained to Level 2) builds the column upward as intended."
        ),
        TopicLessonModule(
            id = "revit_plugins",
            title = "Revit Plugins & Automation",
            shortName = "Revit Plugins",
            badge = "BIM Productivity",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("Revit 2025", "Revit 2024", "Revit 2023", "Revit 2022"),
            overviewSimple = "Revit plugins save hours of repetitive BIM tasks—like batch-creating 50 drawing sheets, syncing Excel BOQ data back into Revit parameters, auto-numbering doors/columns, and exporting NWC files for clash detection.",
            realSiteExample = "A BIM Coordinator has to update fire-rating and concrete grade parameters across 400 structural elements from a site consultant's Excel sheet—using DiRoots SheetLink, it takes 90 seconds instead of 2 days.",
            smallSteps = listOf(
                "Step 1: Identify repetitive tasks (renaming views, Excel parameter sync, NWC export, or real-time walkthroughs).",
                "Step 2: Use pyRevit for free open-source batch tools (Batch Sheet Maker, ReNumber elements along a spline, Wipe unused families).",
                "Step 3: Use DiRoots (SheetLink for Excel round-trip, ProSheets for batch PDF/DWG/IFC export, OneFilter for color-coding elements by parameter).",
                "Step 4: Use the Navisworks NWC Export Utility plugin to export clean, level-split models for 4D/5D coordination."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "plugin_pyrevit",
                    software = "Revit Plugins (pyRevit)",
                    versionScope = "Revit 2022–2025",
                    taskTitle = "ReNumber Columns/Rooms by Click Order",
                    command = "pyRevit > ReNumber",
                    shortcut = "Ribbon: pyRevit Tab",
                    menuPath = "pyRevit Tab > Modify Panel > ReNumber",
                    siteExample = "Renumbering 48 foundation piles sequentially (P-01 to P-48) matching the piling rig movement on site.",
                    practiceExercise = "Open a floor plan with structural columns, go to pyRevit > ReNumber, select 'Structural Columns', and click each column in site sequence."
                ),
                SoftwareCommandItem(
                    id = "plugin_diroots",
                    software = "Revit Plugins (DiRoots SheetLink)",
                    versionScope = "Revit 2022–2025",
                    taskTitle = "Round-Trip Revit Schedules with Excel",
                    command = "SheetLink Export/Import",
                    shortcut = "Ribbon: DiRootsOne Tab",
                    menuPath = "DiRootsOne Tab > SheetLink > Export to Excel / Import from Excel",
                    siteExample = "Exporting a Structural Column Schedule to Excel, filling in 'Concrete Pour Date' and 'Cube Test ID', and importing back into Revit.",
                    practiceExercise = "Open DiRoots SheetLink, select 'Structural Columns' category, export Type Mark & Comments to Excel, edit values, and re-import."
                ),
                SoftwareCommandItem(
                    id = "plugin_nwcout",
                    software = "Revit Plugins (Navisworks Exporter)",
                    versionScope = "Revit 2022–2025",
                    taskTitle = "Export NWC for Navisworks Clash & 4D",
                    command = "Navisworks 202X Exporter",
                    shortcut = "File > Export > NWC",
                    menuPath = "File Tab > Export > NWC (Navisworks Cache)",
                    siteExample = "Exporting a clean '3D - Navisworks Export' view with 'Divide file into levels' checked and 'Coordinates = Shared'.",
                    practiceExercise = "Create a 3D view with annotations hidden, go to File > Export > NWC, open Navisworks Settings, and set Coordinates to Shared."
                )
            ),
            practiceExercise = "Prepare a clean 3D view named '3D_NWC_EXPORT' in Revit, hide all Grids and Levels (VG), and configure the NWC Exporter settings to convert Element IDs and split by level.",
            quickCheckQuestion = "Which free Revit plugin tool allows you to export a Revit Schedule to Excel, batch-edit non-read-only parameters in Excel, and push the changes back into the Revit model?",
            quickCheckOptions = listOf(
                "DiRoots SheetLink",
                "Bluebeam Studio",
                "Navisworks TimeLiner",
                "AutoCAD DesignCenter"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Correct! DiRoots SheetLink (part of DiRootsOne) enables bi-directional sync between Revit model parameters/schedules and Microsoft Excel."
        ),
        TopicLessonModule(
            id = "bluebeam_revu",
            title = "Bluebeam Revu (Advanced QTO & Review)",
            shortName = "Bluebeam Revu",
            badge = "PDF QTO & QA/QC",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("Bluebeam Revu 21", "Bluebeam Revu 20", "Bluebeam Revu 2019"),
            overviewSimple = "Bluebeam Revu is the construction industry standard for smart PDF drawing review, real-time Studio cloud collaboration, and fast Quantity Takeoff (QTO) of concrete areas, wall lengths, and fixture counts directly from 2D PDF drawings.",
            realSiteExample = "A Quantity Surveyor (QS) receives a tender PDF of a warehouse floor plan without a CAD file. They calibrate the PDF scale using a known 6000mm grid dimension, then use Dynamic Fill to extract slab concrete area and perimeter shuttering in seconds.",
            smallSteps = listOf(
                "Step 1: ALWAYS Calibrate & Verify the drawing scale first (Tools > Measure > Calibrate) along BOTH X and Y axes using a known dimension line—never trust printed scale text alone!",
                "Step 2: Use Dynamic Fill (J) to flood-fill complex room/slab boundaries and simultaneously generate Area, Perimeter, and Polylength markups.",
                "Step 3: Open the Markups List (Alt+L) at the bottom and create Custom Columns (e.g., Slab Thickness m, Concrete Volume = Area * Thickness, Unit Rate) for instant BOQ calculation.",
                "Step 4: Use Overlay Pages and Studio Sessions so site engineers, architects, and MEP teams can redline the same PDF simultaneously."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "bb_calibrate",
                    software = "Bluebeam Revu",
                    versionScope = "Revu 20 / Revu 21",
                    taskTitle = "Calibrate PDF Drawing Scale",
                    command = "Calibrate",
                    shortcut = "Ctrl + Alt + M (Measurements Panel)",
                    menuPath = "Tools > Measure > Calibrate",
                    siteExample = "Snapping to two structural grid lines marked '6000 mm' on a foundation PDF before measuring excavation area.",
                    practiceExercise = "Open Measurements panel (Alt+U), click Calibrate, snap to both ends of a known dimension line, and enter the exact millimeter or meter value."
                ),
                SoftwareCommandItem(
                    id = "bb_dynamicfill",
                    software = "Bluebeam Revu",
                    versionScope = "Revu 20 / Revu 21",
                    taskTitle = "Dynamic Fill for Complex Slab & Room Takeoff",
                    command = "Dynamic Fill",
                    shortcut = "J",
                    menuPath = "Tools > Measure > Dynamic Fill",
                    siteExample = "Measuring wet-area waterproofing area and skirting perimeter in an irregularly shaped lobby.",
                    practiceExercise = "Press J, use the boundary tool to close a doorway gap, pour the fill inside a room, select 'Area Measurement' + 'Polylength', and click Apply."
                ),
                SoftwareCommandItem(
                    id = "bb_overlay",
                    software = "Bluebeam Revu",
                    versionScope = "Revu 20 / Revu 21",
                    taskTitle = "Overlay Pages (Revision vs Revision)",
                    command = "Overlay Pages",
                    shortcut = "Ctrl + Alt + O",
                    menuPath = "Document > Comparison > Overlay Pages",
                    siteExample = "Overlaying Structural Rev-01 (Red) and Rev-02 (Green) PDFs—unchanged lines turn black, while moved columns stand out in bright red and green!",
                    practiceExercise = "Go to Document > Overlay Pages, select two drawing revisions, pick 3 alignment points at grid corners, and inspect red/green clouds."
                )
            ),
            practiceExercise = "Calibrate a floor plan PDF to 1:100 metric scale, use Area Measurement (Shift+Alt+A) to measure 3 slab bays, and add a Custom Formula Column in the Markups List (Alt+L) for Concrete Volume (Area * 0.15m).",
            quickCheckQuestion = "Before running any Area (Shift+Alt+A) or Dynamic Fill (J) quantity takeoff on a PDF drawing in Bluebeam Revu, what is the mandatory first step?",
            quickCheckOptions = listOf(
                "Calibrate the page scale against a known dimension in both X and Y directions",
                "Flatten all markups on the sheet",
                "Export the PDF to a Word document",
                "Change page orientation to Portrait"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Always! PDF prints can be stretched or printed 'Fit to Page'. Calibrating against a known dimension line (and checking both X and Y axes) guarantees accurate site takeoff quantities."
        ),
        TopicLessonModule(
            id = "navisworks_4d_5d",
            title = "Navisworks 4D/5D & Clash Detection",
            shortName = "Navisworks 4D/5D",
            badge = "BIM Coordination",
            requiresSoftwareVersion = true,
            supportedVersions = listOf("Navisworks Manage 2025", "Navisworks Manage 2024", "Navisworks Manage 2023"),
            overviewSimple = "Navisworks Manage combines Architecture, Structure, and MEP models into one federated model (.NWF/.NWD). We use Clash Detective to find pipes hitting beams before construction, TimeLiner (4D) to link the 3D model to Primavera/MS Project schedules, and Quantification (5D) for model-based cost/quantity takeoff.",
            realSiteExample = "Before casting the basement roof slab, the BIM engineer runs a Clash Test between 'Structural Beams' and 'HVAC Chilled Water Pipes' with a 25mm tolerance—catching 18 pipe penetrations that need sleeves before concrete is poured!",
            smallSteps = listOf(
                "Step 1: Understand the 3 file types: .NWC (Cache exported from Revit/AutoCAD), .NWF (Working Federated file linking NWCs—use this daily!), and .NWD (Published snapshot containing all geometry).",
                "Step 2: Create smart Search Sets (Find Items: Shift+F3) by Category/Level (e.g., all Structural Framing on Level 1) and save them so they auto-update when the model changes.",
                "Step 3: Open Clash Detective (Ctrl+F2), create a rule-based test (e.g., Structure vs MEP Ducts, Hard Clash), group related clashes, and assign them to the MEP engineer.",
                "Step 4: Open TimeLiner (Ctrl+T) for 4D Simulation (attach Search Sets to schedule tasks with Task Type = Construct) and Quantification Workbook for 5D takeoff."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "nw_finditems",
                    software = "Navisworks Manage",
                    versionScope = "2022–2025",
                    taskTitle = "Create Dynamic Search Sets",
                    command = "Find Items & Sets",
                    shortcut = "Shift + F3 (Find Items)",
                    menuPath = "Home Tab > Select & Search Panel > Find Items / Sets > Manage Sets",
                    siteExample = "Creating a Search Set for 'Category = Structural Foundations' so any new pile added in Revit is automatically included in 4D/5D.",
                    practiceExercise = "Press Shift+F3, set Category = Element, Property = Category, Condition = '=', Value = Structural Columns, click Find All, and click 'Save Search' in the Sets window."
                ),
                SoftwareCommandItem(
                    id = "nw_clash",
                    software = "Navisworks Manage",
                    versionScope = "2022–2025",
                    taskTitle = "Run Hard & Clearance Clash Detective",
                    command = "Clash Detective",
                    shortcut = "Ctrl + F2",
                    menuPath = "Home Tab > Tools Panel > Clash Detective",
                    siteExample = "Testing Selection A (Structural Framing Search Set) against Selection B (MEP Pipes Search Set) with Type = Hard and Tolerance = 0.01m.",
                    practiceExercise = "Press Ctrl+F2, click Add Test, name it 'STR_vs_MEP', select Structural Beams in A and Ducts/Pipes in B, set Type to Hard, and click Run Test."
                ),
                SoftwareCommandItem(
                    id = "nw_timeliner",
                    software = "Navisworks Manage",
                    versionScope = "2022–2025",
                    taskTitle = "4D Construction Simulation (TimeLiner)",
                    command = "TimeLiner",
                    shortcut = "Ctrl + T",
                    menuPath = "Home Tab > Tools Panel > TimeLiner",
                    siteExample = "Linking Primavera P6 / MS Project tasks ('Substructure Footings', 'Ground Floor Columns') to Search Sets and playing a week-by-week construction video.",
                    practiceExercise = "Press Ctrl+T, go to Tasks tab, add 3 tasks (Footing, Column, Slab) with Planned Start/End dates, set Task Type to 'Construct', and click Attach > Current Search/Selection."
                )
            ),
            practiceExercise = "Append a Structural .NWC and an MEP .NWC into Navisworks, save as a Federated .NWF, create Search Sets for Columns, Beams, and Pipes, run a Hard Clash test, and link the sets to 3 TimeLiner tasks.",
            quickCheckQuestion = "In Navisworks TimeLiner (4D Simulation), why do BIM coordinators attach 'Search Sets' to schedule tasks instead of 'Selection Sets'?",
            quickCheckOptions = listOf(
                "Search Sets automatically update to include newly modeled elements when the Revit NWC file is refreshed",
                "Selection Sets cannot be used in Clash Detective",
                "Search Sets make the file size 10x larger",
                "Selection Sets only work on 2D DWG files"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Spot on! A Selection Set only remembers fixed items you clicked once. A Search Set saves the rule (e.g., 'All Level 2 Columns'), so when the design updates next week, new columns are automatically included in your 4D schedule and Clash tests."
        ),
        TopicLessonModule(
            id = "cde_iso19650",
            title = "Common Data Environment (CDE - ISO 19650)",
            shortName = "CDE (ISO 19650)",
            badge = "BIM Management",
            requiresSoftwareVersion = false,
            supportedVersions = listOf("Autodesk Construction Cloud (ACC / BIM 360)", "Bentley ProjectWise", "Trimble Connect / Aconex"),
            overviewSimple = "A Common Data Environment (CDE) is the single source of truth on a construction project so nobody builds from an outdated WhatsApp or email drawing! Under ISO 19650, every drawing or BIM model moves through 4 strict states: WIP, Shared, Published, and Archive.",
            realSiteExample = "Imagine a site supervisor accidentally pouring concrete using 'Rev 01' sent on email while the structural engineer issued 'Rev 03' yesterday. With a CDE, the site tablet only shows approved 'Published (CR)' drawings with revision history.",
            smallSteps = listOf(
                "Step 1: WIP (Work In Progress - S0): The draft model/drawing inside the architectural or structural team's folder—not visible to other disciplines yet.",
                "Step 2: SHARED (S1 to S4): After internal QA/QC check, the model is shared with other consultants for clash coordination (S1), information (S2), or review & comment (S3).",
                "Step 3: PUBLISHED (A1, A2... / CR): Once the client/lead appointed party authorizes the design, it moves to Published—ONLY Published drawings can be used on the construction site!",
                "Step 4: ARCHIVE: Superseded revisions and As-Built records are permanently stored for audit trail and legal history."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "cde_states",
                    software = "CDE (ISO 19650)",
                    versionScope = "ISO 19650-1 & 19650-2",
                    taskTitle = "4 Containers & Suitability Status Codes",
                    command = "S0 -> S1/S2/S3/S4 -> A1/CR -> Archive",
                    shortcut = "WIP | SHARED | PUBLISHED | ARCHIVE",
                    menuPath = "CDE Workflow Gateways: Check/Review/Approve -> Authorize",
                    siteExample = "S0 = Initial WIP; S1 = Suitable for Coordination; S2 = Suitable for Information; S3 = Suitable for Review & Comment; S4 = Suitable for Stage Approval; CR = As-Built Construction Record.",
                    practiceExercise = "Write a standard ISO 19650 file name: [Project]-[Originator]-[Volume]-[Level]-[Type]-[Role]-[Number], e.g., METRO-AEC-Z1-01-M3-S-0001.rvt."
                )
            ),
            practiceExercise = "Map 4 real site documents into their proper CDE state: (1) Draft rebar sketch by junior technician, (2) Structural model uploaded for MEP clash check, (3) Stamped 'Good for Construction (GFC)' footing drawing, and (4) Superseded Rev-00 plan.",
            quickCheckQuestion = "According to ISO 19650 Common Data Environment (CDE) rules, from which container state is a site engineer allowed to take drawings for actual physical construction on site?",
            quickCheckOptions = listOf(
                "Published (Authorized for construction)",
                "Work In Progress (WIP - S0)",
                "Shared (S1 - For coordination)",
                "Personal Desktop Folder"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Correct! Only information in the 'Published' state has been checked, reviewed, and formally authorized for construction on site."
        ),
        TopicLessonModule(
            id = "cobie",
            title = "COBie (Asset Data & Handover)",
            shortName = "COBie",
            badge = "6D Handover",
            requiresSoftwareVersion = false,
            supportedVersions = listOf("COBie 2.4 Spreadsheet / IFC", "Autodesk Interoperability Tools for Revit"),
            overviewSimple = "COBie stands for Construction Operations Building Information Exchange. When a building is finished, the client doesn't just want a 3D model—they need a structured spreadsheet/data drop of every maintainable asset (Pumps, Chillers, Fire Doors, AHUs) with Serial Numbers, Warranty Dates, and Room Locations.",
            realSiteExample = "When a hospital's basement fire pump breaks down 2 years after handover, the Facility Manager checks the COBie sheet to immediately see which room it is in (Space), its manufacturer & model (Type), and its warranty expiration & serial number (Component).",
            smallSteps = listOf(
                "Step 1: Know what goes into COBie—ONLY maintainable/replaceable equipment and spaces (e.g., Pumps, Chillers, Valves, Fire Doors), NOT raw structural concrete slabs, foundations, or rebar!",
                "Step 2: Understand the hierarchy: Facility (Building) -> Floor (Level) -> Space (Room) -> Zone (Fire/HVAC zone).",
                "Step 3: Understand Type vs Component: 'Type' is the catalog product (e.g., Grundfos 15kW Pump Model X), while 'Component' is each individual installed unit on site (Pump #1 in Basement Plant Room with Serial #SN-9921).",
                "Step 4: In Revit, install the BIM Interoperability Tools (COBie Extension), map Rooms to COBie.Space, check required parameters, and export the color-coded COBie Excel workbook."
            ),
            keyCommands = listOf(
                SoftwareCommandItem(
                    id = "cobie_revit",
                    software = "COBie (Revit Interoperability Tools)",
                    versionScope = "COBie 2.4 / ISO 19650",
                    taskTitle = "Setup, Zone & Export COBie Spreadsheet",
                    command = "COBie Extension > Setup / Update / Create Spreadsheet",
                    shortcut = "Interoperability Tools Tab > COBie Panel",
                    menuPath = "Interoperability Tools Tab > COBie Extension > Setup Project -> Select Elements -> Export",
                    siteExample = "Exporting Yellow (Required), Orange (Reference to other sheet), and Purple (External software ID) columns in a COBie spreadsheet.",
                    practiceExercise = "Identify the difference between COBie.Type (Manufacturer, ModelNumber, WarrantyDurationParts) and COBie.Component (SerialNumber, InstallationDate, BarCode)."
                )
            ),
            practiceExercise = "Classify these 5 items as 'Included in COBie' or 'Excluded from COBie': (1) Chilled Water Pump, (2) Cast-in-place RCC Footing, (3) Fire Rated Door, (4) Structural Steel I-Beam, (5) Air Handling Unit (AHU).",
            quickCheckQuestion = "In a COBie deliverable workbook, why are cast-in-place RCC footings and structural steel beams excluded from the Component & Type sheets?",
            quickCheckOptions = listOf(
                "Because COBie tracks maintainable/replaceable facility assets for Operations & Maintenance, not permanent structural elements",
                "Because Excel cannot store concrete volumes",
                "Because Revit cannot export structural elements",
                "Because structural engineers do not use BIM"
            ),
            quickCheckCorrectIndex = 0,
            quickCheckExplanation = "Exactly! COBie is designed for Facility Management (O&M)—tracking equipment that requires regular maintenance, spare parts, or replacement, rather than permanent structural framing."
        )
    )

    fun softwareCategoryForModule(moduleId: String): SoftwareCategory = when (moduleId) {
        "autocad" -> SoftwareCategory.AUTOCAD
        "revit_arch_struct", "revit_plugins" -> SoftwareCategory.REVIT
        "navisworks_4d_5d" -> SoftwareCategory.NAVISWORKS
        "microstation" -> SoftwareCategory.MICROSTATION
        "bluebeam_revu" -> SoftwareCategory.BLUEBEAM
        "cde_iso19650", "cobie" -> SoftwareCategory.CDE_COBIE
        else -> SoftwareCategory.ALL
    }

    fun diplomaTracksForModule(moduleId: String): List<DiplomaTrack> = when (moduleId) {
        "autocad" -> listOf(
            DiplomaTrack.CAD_STRUCTURAL_DRAFTING,
            DiplomaTrack.BIM_MODELING_AUTHORING
        )
        "microstation" -> listOf(
            DiplomaTrack.CAD_STRUCTURAL_DRAFTING
        )
        "revit_arch_struct" -> listOf(
            DiplomaTrack.BIM_MODELING_AUTHORING,
            DiplomaTrack.CAD_STRUCTURAL_DRAFTING
        )
        "revit_plugins" -> listOf(
            DiplomaTrack.BIM_MODELING_AUTHORING,
            DiplomaTrack.BIM_COORDINATION_4D_5D
        )
        "bluebeam_revu" -> listOf(
            DiplomaTrack.CAD_STRUCTURAL_DRAFTING,
            DiplomaTrack.BIM_COORDINATION_4D_5D,
            DiplomaTrack.DIGITAL_DELIVERY_ISO_FM
        )
        "navisworks_4d_5d" -> listOf(
            DiplomaTrack.BIM_COORDINATION_4D_5D,
            DiplomaTrack.DIGITAL_DELIVERY_ISO_FM
        )
        "cde_iso19650" -> listOf(
            DiplomaTrack.DIGITAL_DELIVERY_ISO_FM,
            DiplomaTrack.BIM_COORDINATION_4D_5D
        )
        "cobie" -> listOf(
            DiplomaTrack.DIGITAL_DELIVERY_ISO_FM,
            DiplomaTrack.BIM_MODELING_AUTHORING
        )
        else -> listOf(DiplomaTrack.ALL_TRACKS)
    }

    val studyMaterialIndex: List<StudyMaterialIndexItem> by lazy {
        buildList {
            topics.forEach { module ->
                val category = softwareCategoryForModule(module.id)
                val tracks = diplomaTracksForModule(module.id)
                val versionTag = if (module.requiresSoftwareVersion) {
                    module.supportedVersions.firstOrNull() ?: module.shortName
                } else {
                    module.badge
                }

                // 1. Core Concept & Site Guide
                add(
                    StudyMaterialIndexItem(
                        id = "${module.id}_concept",
                        parentModuleId = module.id,
                        title = "${module.title}: Core Concept & Site Overview",
                        summary = "${module.overviewSimple} Real Site Context: ${module.realSiteExample}",
                        softwareCategory = category,
                        softwareVersionTag = versionTag,
                        materialType = StudyMaterialType.CONCEPT_GUIDE,
                        diplomaTracks = tracks,
                        estimatedMinutes = 8,
                        keywords = listOf(
                            module.shortName,
                            module.badge,
                            category.displayName,
                            "concept",
                            "site example",
                            "overview"
                        ) + tracks.map { it.shortLabel },
                        recommendedMode = StudyMode.LEARN_TOPIC
                    )
                )

                // 2. Step-by-Step Lab Workflow
                add(
                    StudyMaterialIndexItem(
                        id = "${module.id}_workflow",
                        parentModuleId = module.id,
                        title = "${module.shortName} Step-by-Step Lab Workflow (${module.smallSteps.size} Steps)",
                        summary = module.smallSteps.joinToString(" "),
                        softwareCategory = category,
                        softwareVersionTag = versionTag,
                        materialType = StudyMaterialType.STEP_WORKFLOW,
                        diplomaTracks = tracks,
                        estimatedMinutes = 12,
                        keywords = listOf(
                            module.shortName,
                            category.displayName,
                            "workflow",
                            "steps",
                            "tutorial",
                            "lab"
                        ) + module.smallSteps,
                        recommendedMode = StudyMode.PRACTICE_SOFTWARE
                    )
                )

                // 3. Individual Command & Shortcut Sheets
                module.keyCommands.forEach { cmd ->
                    add(
                        StudyMaterialIndexItem(
                            id = "cmd_${cmd.id}",
                            parentModuleId = module.id,
                            title = "${cmd.software}: ${cmd.taskTitle}",
                            summary = "Site Application: ${cmd.siteExample} | Menu Path: ${cmd.menuPath}",
                            softwareCategory = category,
                            softwareVersionTag = cmd.versionScope,
                            materialType = StudyMaterialType.COMMAND_REFERENCE,
                            diplomaTracks = tracks,
                            estimatedMinutes = 5,
                            keywords = listOf(
                                cmd.software,
                                cmd.command,
                                cmd.shortcut,
                                cmd.taskTitle,
                                cmd.menuPath,
                                cmd.siteExample,
                                category.displayName
                            ),
                            commandHighlight = "${cmd.command} (${cmd.shortcut})",
                            recommendedMode = StudyMode.PRACTICE_SOFTWARE
                        )
                    )
                }

                // 4. Hands-on Practice Exercise
                add(
                    StudyMaterialIndexItem(
                        id = "${module.id}_exercise",
                        parentModuleId = module.id,
                        title = "${module.shortName} Hands-On Site Practice Lab",
                        summary = module.practiceExercise,
                        softwareCategory = category,
                        softwareVersionTag = versionTag,
                        materialType = StudyMaterialType.PRACTICE_EXERCISE,
                        diplomaTracks = tracks,
                        estimatedMinutes = 15,
                        keywords = listOf(
                            module.shortName,
                            category.displayName,
                            "practice",
                            "exercise",
                            "assignment",
                            "hands-on"
                        ),
                        recommendedMode = StudyMode.PRACTICE_SOFTWARE
                    )
                )

                // 5. Quick Check & Interview Readiness Quiz
                add(
                    StudyMaterialIndexItem(
                        id = "${module.id}_quiz",
                        parentModuleId = module.id,
                        title = "${module.shortName} Quick Check: ${module.quickCheckQuestion}",
                        summary = "Key Takeaway: ${module.quickCheckExplanation}",
                        softwareCategory = category,
                        softwareVersionTag = versionTag,
                        materialType = StudyMaterialType.QUICK_CHECK,
                        diplomaTracks = tracks,
                        estimatedMinutes = 4,
                        keywords = listOf(
                            module.shortName,
                            category.displayName,
                            "quiz",
                            "quick check",
                            "interview question"
                        ) + module.quickCheckOptions,
                        recommendedMode = StudyMode.MOCK_INTERVIEW
                    )
                )
            }
        }
    }

    fun filterStudyMaterials(
        query: String,
        diplomaTrack: DiplomaTrack,
        softwareCategory: SoftwareCategory,
        materialType: StudyMaterialType = StudyMaterialType.ALL
    ): List<StudyMaterialIndexItem> {
        val trimmedQuery = query.trim().lowercase()
        val queryTokens = trimmedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        return studyMaterialIndex.filter { item ->
            val matchesTrack = diplomaTrack == DiplomaTrack.ALL_TRACKS ||
                item.diplomaTracks.contains(diplomaTrack)

            val matchesSoftware = softwareCategory == SoftwareCategory.ALL ||
                item.softwareCategory == softwareCategory

            val matchesType = materialType == StudyMaterialType.ALL ||
                item.materialType == materialType

            val matchesQuery = if (queryTokens.isEmpty()) {
                true
            } else {
                val searchableBlob = buildString {
                    append(item.title.lowercase()).append(' ')
                    append(item.summary.lowercase()).append(' ')
                    append(item.softwareCategory.displayName.lowercase()).append(' ')
                    append(item.softwareVersionTag.lowercase()).append(' ')
                    append(item.commandHighlight.lowercase()).append(' ')
                    item.keywords.forEach { append(it.lowercase()).append(' ') }
                    item.diplomaTracks.forEach {
                        append(it.title.lowercase()).append(' ')
                        append(it.shortLabel.lowercase()).append(' ')
                    }
                }
                queryTokens.all { token -> searchableBlob.contains(token) }
            }

            matchesTrack && matchesSoftware && matchesType && matchesQuery
        }
    }
}
