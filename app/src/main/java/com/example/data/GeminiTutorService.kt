package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiTutorService {

    private val tutorSystemInstruction = """
        You are a friendly, encouraging civil engineering and BIM tutor for diploma students.
        
        Topics you teach:
        - AutoCAD 2D and 3D
        - MicroStation
        - Revit Architecture and Structure
        - Revit plugins (e.g., pyRevit, DiRoots, Navisworks Exporter, Enscape)
        - Bluebeam Revu (advanced: calibration, Dynamic Fill, Custom Columns, Overlay Pages, Studio)
        - Navisworks 4D/5D (Selection vs Search Sets, Clash Detective, TimeLiner 4D, Quantification 5D)
        - Common Data Environment (CDE - ISO 19650: WIP, Shared, Published, Archive, Suitability Codes)
        - COBie (Facility, Floor, Space, Zone, Type, Component, Attribute sheets for O&M handover)
        - Mock interviews (Technical + HR)

        STRICT TEACHING RULES:
        1. Explain in simple, clear English suitable for civil engineering diploma students, ALWAYS using real construction site and building examples (e.g., RCC footings, plinth beams, column grids, slab shuttering, MEP vs beam clashes).
        2. Break every topic into small, numbered steps (in smallSteps), then check the student's understanding with a single quick check question (in quickCheckQuestion).
        3. For software topics (AutoCAD, MicroStation, Revit, Revit plugins, Bluebeam Revu, Navisworks):
           - BEFORE giving detailed software steps, check if the student's software version is known. If the software version is EMPTY or unknown and the student hasn't stated it yet, set `asksSoftwareVersion = true`, set `targetSoftwareName` to the software name, and politely ask which version they use before giving full steps!
           - Once the software version is known, provide exact commands, menu paths, and keyboard shortcuts in `commandsAndShortcuts`, plus a small hands-on practice exercise in `practiceExercise`.
        4. If you are ever unsure about an obscure version-specific feature or detail, state it honestly in `uncertaintyNote` instead of guessing.
        5. MOCK INTERVIEW RULES:
           - Ask ONLY ONE question at a time (`nextInterviewQuestion`) and wait for the student's answer.
           - When the student answers an interview question, evaluate their answer by providing:
             * `interviewScoreOutOf10` (integer 1 to 10)
             * `interviewWhatWasGood` (specific praise for what they got right or how they structured their thought)
             * `interviewBetterSampleAnswer` (a clear, confident sample answer a diploma graduate can give in an interview)
             * `interviewQuestionCategory` ("Technical" or "HR")
             * `nextInterviewQuestion` (the next single question, alternating/mixing Technical civil/BIM questions with HR questions like teamwork on site, handling tight deadlines, or why you chose BIM/Civil Engineering).
        6. At the start of a chat or if the student just says "Hi"/"Hello", ask whether they want to learn a topic, practice software, or do a mock interview.
    """.trimIndent()

    private val structuredSchema: JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("mainExplanation") {
                put("type", "STRING")
                put("description", "Friendly explanation in simple English or conversational response.")
            }
            putJsonObject("smallSteps") {
                put("type", "ARRAY")
                putJsonObject("items") { put("type", "STRING") }
                put("description", "Topic broken down into 3-5 small, digestible steps.")
            }
            putJsonObject("realSiteExample") {
                put("type", "STRING")
                put("description", "Concrete real construction site or building example.")
            }
            putJsonObject("commandsAndShortcuts") {
                put("type", "STRING")
                put("description", "Exact software commands, menu paths, and shortcuts formatted clearly.")
            }
            putJsonObject("practiceExercise") {
                put("type", "STRING")
                put("description", "A small hands-on practice exercise for the student.")
            }
            putJsonObject("quickCheckQuestion") {
                put("type", "STRING")
                put("description", "A quick question to check the student's understanding.")
            }
            putJsonObject("asksSoftwareVersion") {
                put("type", "BOOLEAN")
                put("description", "True if asking the student which software version they use before giving steps.")
            }
            putJsonObject("targetSoftwareName") {
                put("type", "STRING")
                put("description", "Name of the software if asking for version (e.g., AutoCAD, Revit, Bluebeam Revu).")
            }
            putJsonObject("interviewScoreOutOf10") {
                put("type", "INTEGER")
                put("description", "Score from 1 to 10 if the student just answered a mock interview question.")
            }
            putJsonObject("interviewWhatWasGood") {
                put("type", "STRING")
                put("description", "What was good in the student's mock interview answer.")
            }
            putJsonObject("interviewBetterSampleAnswer") {
                put("type", "STRING")
                put("description", "A polished sample answer for the mock interview question.")
            }
            putJsonObject("interviewQuestionCategory") {
                put("type", "STRING")
                put("description", "'Technical' or 'HR' when in Mock Interview mode.")
            }
            putJsonObject("nextInterviewQuestion") {
                put("type", "STRING")
                put("description", "The single mock interview question being asked now.")
            }
            putJsonObject("uncertaintyNote") {
                put("type", "STRING")
                put("description", "Honest note if any version detail is uncertain instead of guessing.")
            }
        }
    }

    suspend fun generateTutorResponse(
        userMessage: String,
        currentMode: StudyMode,
        selectedTopic: String,
        softwareVersion: String,
        history: List<ChatMessageEntity>
    ): TutorStructuredReply = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (isKeyConfigured) {
            val contextHeader = buildString {
                appendLine("[SESSION CONTEXT]")
                appendLine("Current Mode: ${currentMode.label}")
                appendLine("Selected Topic: ${selectedTopic.ifBlank { "Not selected yet" }}")
                appendLine("Student's Software Version: ${softwareVersion.ifBlank { "UNKNOWN - If teaching or practicing software (AutoCAD, MicroStation, Revit, Revit plugins, Bluebeam Revu, Navisworks), you MUST ask which software version they use BEFORE giving steps!" }}")
            }

            val recentHistory = history.takeLast(8).map { msg ->
                Content(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(
                        Part(
                            text = if (msg.isUser) {
                                msg.mainText
                            } else {
                                buildString {
                                    append(msg.mainText)
                                    if (msg.nextInterviewQuestion.isNotBlank()) {
                                        append("\nInterview Question Asked: ${msg.nextInterviewQuestion} (${msg.interviewQuestionType})")
                                    }
                                    if (msg.quickCheckQuestion.isNotBlank()) {
                                        append("\nQuick Check Question Asked: ${msg.quickCheckQuestion}")
                                    }
                                }
                            }
                        )
                    )
                )
            }

            val contents = recentHistory + Content(
                role = "user",
                parts = listOf(Part(text = "$contextHeader\n\nStudent says: $userMessage"))
            )

            val request = GenerateContentRequest(
                contents = contents,
                systemInstruction = Content(parts = listOf(Part(text = tutorSystemInstruction))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = structuredSchema,
                    temperature = 0.4f
                )
            )

            val modelsToTry = listOf("gemini-3.1-pro-preview", "gemini-3.5-flash")
            for (modelName in modelsToTry) {
                try {
                    val response = GeminiRetrofitClient.service.generateContent(
                        model = modelName,
                        apiKey = apiKey,
                        request = request
                    )
                    val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawText.isNullOrBlank()) {
                        return@withContext GeminiRetrofitClient.json.decodeFromString(
                            TutorStructuredReply.serializer(),
                            rawText
                        )
                    }
                } catch (_: Exception) {
                    // Try fallback model or local structured tutor engine
                }
            }
        }

        // Intelligent fallback tutor engine (also used when GEMINI_API_KEY is not yet set in AI Studio Secrets)
        buildOfflineStructuredReply(
            userMessage = userMessage,
            currentMode = currentMode,
            selectedTopic = selectedTopic,
            softwareVersion = softwareVersion,
            history = history,
            apiKeyMissing = !isKeyConfigured
        )
    }

    private fun buildOfflineStructuredReply(
        userMessage: String,
        currentMode: StudyMode,
        selectedTopic: String,
        softwareVersion: String,
        history: List<ChatMessageEntity>,
        apiKeyMissing: Boolean
    ): TutorStructuredReply {
        val lower = userMessage.lowercase()
        val lastBotMessage = history.lastOrNull { !it.isUser }

        // 1. Check if in Mock Interview mode or user requested mock interview
        if (currentMode == StudyMode.MOCK_INTERVIEW || lower.contains("interview") || lower.contains("mock")) {
            return handleMockInterviewTurn(userMessage, selectedTopic, lastBotMessage)
        }

        // 2. Identify matched topic from curriculum catalog
        val matchedModule = CivilCurriculumCatalog.topics.firstOrNull { module ->
            lower.contains(module.shortName.lowercase()) ||
                lower.contains(module.id.lowercase()) ||
                (module.id == "autocad" && (lower.contains("autocad") || lower.contains("cad") || lower.contains("2d") || lower.contains("3d"))) ||
                (module.id == "microstation" && (lower.contains("microstation") || lower.contains("dgn") || lower.contains("bentley"))) ||
                (module.id == "revit_arch_struct" && (lower.contains("revit") && !lower.contains("plugin") && !lower.contains("pyrevit") && !lower.contains("diroots"))) ||
                (module.id == "revit_plugins" && (lower.contains("plugin") || lower.contains("pyrevit") || lower.contains("diroots"))) ||
                (module.id == "bluebeam_revu" && (lower.contains("bluebeam") || lower.contains("revu") || lower.contains("qto"))) ||
                (module.id == "navisworks_4d_5d" && (lower.contains("navisworks") || lower.contains("clash") || lower.contains("4d") || lower.contains("5d") || lower.contains("timeliner"))) ||
                (module.id == "cde_iso19650" && (lower.contains("cde") || lower.contains("common data") || lower.contains("19650"))) ||
                (module.id == "cobie" && lower.contains("cobie"))
        } ?: CivilCurriculumCatalog.topics.firstOrNull {
            it.shortName.equals(selectedTopic, ignoreCase = true) || it.title.equals(selectedTopic, ignoreCase = true)
        }

        // Detect if user just provided a software version in their message (e.g., "2024", "2025", "2023", "Revu 21", "CONNECT")
        val versionRegex = Regex("(2018|2019|2020|2021|2022|2023|2024|2025|revu 20|revu 21|connect|v8i|lt)", RegexOption.IGNORE_CASE)
        val detectedVersionInMsg = versionRegex.find(userMessage)?.value ?: ""
        val effectiveVersion = softwareVersion.ifBlank { detectedVersionInMsg }

        if (matchedModule != null) {
            // Rule: Ask which software version I use before giving steps!
            if (matchedModule.requiresSoftwareVersion && effectiveVersion.isBlank()) {
                return TutorStructuredReply(
                    mainExplanation = "Great choice! Let's work on **${matchedModule.title}**.\n\nBefore I give you the exact commands, menu paths, and step-by-step exercise, **which software version of ${matchedModule.shortName} are you using?** (For example: ${matchedModule.supportedVersions.joinToString(", ")}). Menu ribbons and shortcuts can change slightly between versions, and I want to give you exact instructions instead of guessing!",
                    realSiteExample = matchedModule.realSiteExample,
                    asksSoftwareVersion = true,
                    targetSoftwareName = matchedModule.shortName,
                    quickCheckQuestion = "Tap your ${matchedModule.shortName} version chip below (or type it in) so we can begin Step 1!"
                )
            }

            // Now we have the software version (or the topic doesn't require a software version like CDE/COBie)
            val versionHeader = if (matchedModule.requiresSoftwareVersion) {
                "Tailored for **${matchedModule.shortName} ($effectiveVersion)**:\n\n"
            } else ""

            val commandsBlock = matchedModule.keyCommands.joinToString("\n\n") { cmd ->
                "• ${cmd.taskTitle}\n  Command: ${cmd.command}  |  Shortcut: ${cmd.shortcut}\n  Menu Path: ${cmd.menuPath}"
            }

            val uncertainty = if (effectiveVersion.contains("2018") || effectiveVersion.contains("V8i", ignoreCase = true)) {
                "Note: Since you are using an older legacy version ($effectiveVersion), classic toolbar positions may differ slightly from the modern Ribbon path shown below—if a panel name looks different on your screen, tell me and we'll use the direct command-line key-in!"
            } else if (apiKeyMissing) {
                "Tip: For free-form AI follow-ups via Gemini API, add your GEMINI_API_KEY in the AI Studio Secrets panel."
            } else ""

            return TutorStructuredReply(
                mainExplanation = versionHeader + matchedModule.overviewSimple,
                smallSteps = matchedModule.smallSteps,
                realSiteExample = matchedModule.realSiteExample,
                commandsAndShortcuts = commandsBlock,
                practiceExercise = matchedModule.practiceExercise,
                quickCheckQuestion = matchedModule.quickCheckQuestion,
                asksSoftwareVersion = false,
                uncertaintyNote = uncertainty
            )
        }

        // Check if student is answering a previous Quick Check question
        if (lastBotMessage != null && lastBotMessage.quickCheckQuestion.isNotBlank() && !lastBotMessage.asksSoftwareVersion) {
            return TutorStructuredReply(
                mainExplanation = "Good effort thinking through that site scenario! Let's review why this matters on a real building project and take the next small step.",
                smallSteps = listOf(
                    "Step 1: Verify your coordinates, units, or scale before placing any elements.",
                    "Step 2: Use structured layers/levels/search-sets so other engineers on the CDE can coordinate cleanly.",
                    "Step 3: Try the hands-on exercise in your software and tell me if any command behaves differently on your machine."
                ),
                realSiteExample = "On a multi-story commercial building site, following standard naming and coordinate setup prevents costly rework when Structural and MEP models are combined.",
                quickCheckQuestion = "Which topic would you like to explore next, or would you like to try a scored Mock Interview question?"
            )
        }

        // Default friendly prompt asking the 3 modes
        return TutorStructuredReply(
            mainExplanation = "I'm ready to help you master Civil Engineering & BIM! At the start of our study flow, please tell me how you'd like to proceed:\n\n1. **Learn a Topic** (AutoCAD 2D/3D, MicroStation, Revit Arch/Struct, Revit Plugins, Bluebeam Revu, Navisworks 4D/5D, CDE, or COBie)\n2. **Practice Software** (with exact commands, menu paths, shortcuts, and exercises after checking your software version)\n3. **Do a Mock Interview** (one question at a time, mixing Technical & HR, with a score out of 10 and a better sample answer)",
            realSiteExample = "Pick any mode card or topic chip below to jump straight into a real site example!",
            quickCheckQuestion = "Would you like to Learn a Topic, Practice Software, or Do a Mock Interview today?"
        )
    }

    private fun handleMockInterviewTurn(
        userMessage: String,
        selectedTopic: String,
        lastBotMessage: ChatMessageEntity?
    ): TutorStructuredReply {
        val questionBank = listOf(
            Triple(
                "Technical",
                "In Revit Structure, if you place an RCC column on the Ground Floor plan and it disappears or goes downward into the foundation instead of up to the First Floor, what setting did you miss on the Options Bar, and how do you fix it?",
                "When placing a Structural Column (shortcut CL) in a Plan View, Revit often defaults the Options Bar from 'Height' to 'Depth'. To fix it before placing, change the dropdown on the Options Bar from 'Depth' to 'Height' and set the top constraint to 'First Floor'. If already placed, select the column and adjust its Base Level and Top Level in the Properties Palette."
            ),
            Triple(
                "HR",
                "Imagine you are a Junior BIM / Site Engineer on a fast-track residential tower. You discover that the MEP contractor is installing drainage pipes through a structural beam without approved sleeve drawings. How would you handle this situation professionally on site?",
                "First, I would politely ask the site supervisor to pause drilling or casting at that beam location immediately because cutting a structural beam's tension or shear zone compromises building safety. Second, I would document the exact grid location with photos and check the latest 'Published' drawing on the CDE. Third, I would raise an urgent RFI / clash report to the Structural Engineer and BIM Coordinator so an approved sleeve location with extra reinforcement can be issued."
            ),
            Triple(
                "Technical",
                "What is the difference between a 'Selection Set' and a 'Search Set' in Navisworks Manage, and why do we prefer Search Sets for 4D TimeLiner and Clash Detection?",
                "A Selection Set is a static group of elements manually clicked and saved—if new columns or pipes are added in Revit next week, they won't be included. A Search Set saves dynamic filter rules (such as Category = Structural Framing and Level = Level 2). When the NWC model is refreshed in our .NWF file, the Search Set automatically captures all newly added elements for Clash Detective and 4D TimeLiner tasks."
            ),
            Triple(
                "HR",
                "Why did you choose Civil Engineering and BIM after your diploma, and where do you see yourself in the next 3 years?",
                "During my diploma, I loved seeing how 2D structural drawings turn into real RCC buildings on site, and learning BIM tools like AutoCAD, Revit, and Navisworks showed me how digital coordination prevents costly site mistakes before concrete is poured. In the next 3 years, I want to grow from a strong BIM Modeler / Junior Site Engineer into a BIM Coordinator who bridges site execution with 4D/5D digital workflows."
            ),
            Triple(
                "Technical",
                "Under ISO 19650 Common Data Environment (CDE), what are the 4 information container states, and which state must a drawing reach before it can be used for actual construction on site?",
                "The 4 states in an ISO 19650 CDE are: 1) Work In Progress (WIP - S0) for internal drafting, 2) Shared (S1–S4) for multi-discipline coordination and client review, 3) Published (A1/CR) for authorized construction issue, and 4) Archive for historical audit trails. A site engineer must ONLY build from drawings in the 'Published' state."
            ),
            Triple(
                "Technical",
                "When using Bluebeam Revu to measure concrete slab area and shuttering perimeter from a PDF drawing, why must you calibrate both the X and Y axes first?",
                "PDF drawings exported or scanned from CAD are sometimes scaled 'Fit to Page', which can stretch horizontal and vertical dimensions unevenly. Using Tools > Measure > Calibrate along a known horizontal dimension (X) and checking a vertical dimension (Y) ensures that Area (Shift+Alt+A) and Dynamic Fill (J) quantity takeoffs match true site dimensions."
            ),
            Triple(
                "HR",
                "Tell me about a time during your diploma project or lab work when you didn't know how to solve a technical problem or software error. What did you do?",
                "During our diploma building drawing project, our 3D footing extrusion in AutoCAD kept creating hollow surfaces instead of solid concrete pads. Instead of guessing, I checked the object properties, realized our column rectangles were 4 separate lines rather than closed polylines, used the JOIN (J) command to close them, and created a checklist for my teammates so nobody repeated the error."
            ),
            Triple(
                "Technical",
                "In a COBie deliverable spreadsheet for building handover, why do we include Chilled Water Pumps and Fire Doors, but exclude cast-in-place RCC footings and beams?",
                "COBie (Construction Operations Building Information Exchange) is built for Facility Management and Operations & Maintenance (6D BIM). Facility managers need serial numbers, warranties, spare parts, and room locations (COBie.Space, Type, Component) for maintainable assets like pumps and fire doors, whereas permanent structural concrete elements do not require routine part replacement."
            )
        )

        val previousQuestion = lastBotMessage?.nextInterviewQuestion.orEmpty()
        val isStartingInterview = previousQuestion.isBlank() ||
            userMessage.lowercase().let {
                it == "mock interview" || it.startsWith("start") || it.contains("do a mock interview")
            }

        if (isStartingInterview) {
            val firstQ = questionBank[0]
            return TutorStructuredReply(
                mainExplanation = "Welcome to your **Civil Engineering & BIM Diploma Mock Interview**! I will ask you **one question at a time** (mixing Technical site/BIM questions with HR questions). Take your time, write your answer in your own words, and I'll give you a **score out of 10**, highlight **what was good**, and share a **better sample answer**.",
                interviewQuestionCategory = firstQ.first,
                nextInterviewQuestion = firstQ.second
            )
        }

        // Evaluate the student's answer to `previousQuestion`
        val matchedIndex = questionBank.indexOfFirst { it.second == previousQuestion }.coerceAtLeast(0)
        val currentQ = questionBank[matchedIndex]
        val nextQ = questionBank[(matchedIndex + 1) % questionBank.size]

        val wordCount = userMessage.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val lowerAns = userMessage.lowercase()

        // Score calculation based on technical specificity & clarity
        val keyTermsMatched = listOf(
            "height", "depth", "level", "safety", "structural", "rfi", "cde", "published",
            "search", "selection", "rule", "clash", "wip", "shared", "archive", "calibrate",
            "scale", "maintenance", "asset", "facility", "polyline", "join", "bim", "site"
        ).count { lowerAns.contains(it) }

        val score = when {
            wordCount < 4 -> 4
            wordCount >= 25 && keyTermsMatched >= 2 -> 9
            wordCount >= 15 && keyTermsMatched >= 1 -> 8
            wordCount >= 8 -> 7
            else -> 6
        }

        val whatWasGood = when {
            score >= 8 -> "You explained your point clearly in practical engineering terms and connected it to real site/software workflow (${wordCount} words with strong technical keywords)."
            score >= 6 -> "You captured the core idea well! To push your score to 9/10 in a real interview, mention the exact command/menu term or a quick site example."
            else -> "Good start! You attempted the question directly, though your answer was quite brief—interviewers love hearing a 2–3 sentence explanation with a site example."
        }

        return TutorStructuredReply(
            mainExplanation = "Great job completing that question! Here is my feedback on your answer, followed by your next interview question:",
            interviewScoreOutOf10 = score,
            interviewWhatWasGood = whatWasGood,
            interviewBetterSampleAnswer = currentQ.third,
            interviewQuestionCategory = nextQ.first,
            nextInterviewQuestion = nextQ.second
        )
    }
}
