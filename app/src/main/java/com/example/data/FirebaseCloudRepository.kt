package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

data class UserStudyProfile(
    val userId: String = "",
    val displayName: String = "",
    val selectedDiplomaTrack: String = DiplomaTrack.ALL_TRACKS.name,
    val preferredSoftwareVersion: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class CloudBookmarkedCommand(
    val id: String = "",
    val userId: String = "",
    val software: String = "",
    val versionScope: String = "",
    val taskTitle: String = "",
    val command: String = "",
    val shortcut: String = "",
    val menuPath: String = "",
    val siteExample: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toLocalEntity(): BookmarkedCommandEntity = BookmarkedCommandEntity(
        id = id,
        software = software,
        versionScope = versionScope,
        taskTitle = taskTitle,
        command = command,
        shortcut = shortcut,
        menuPath = menuPath,
        siteExample = siteExample,
        savedAt = (updatedAt ?: createdAt)?.toDate()?.time ?: System.currentTimeMillis()
    )
}

data class CloudInterviewAttempt(
    val id: String = "",
    val userId: String = "",
    val topic: String = "",
    val questionType: String = "",
    val question: String = "",
    val userAnswer: String = "",
    val score: Long = 0L,
    val whatWasGood: String = "",
    val betterSampleAnswer: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toLocalEntity(index: Int): InterviewAttemptEntity = InterviewAttemptEntity(
        id = index.toLong() + 1L,
        topic = topic,
        questionType = questionType,
        questionAsked = question,
        studentAnswer = userAnswer,
        scoreOutOf10 = score.toInt(),
        whatWasGood = whatWasGood,
        betterSampleAnswer = betterSampleAnswer,
        timestamp = (updatedAt ?: createdAt)?.toDate()?.time ?: System.currentTimeMillis()
    )
}

class FirebaseCloudRepository(private val db: FirebaseFirestore) {

    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeStudyProfile(): Flow<UserStudyProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserStudyProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    fun observeBookmarkedCommands(): Flow<List<CloudBookmarkedCommand>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/bookmarked_commands"
        emitAll(
            db.collection("users").document(uid).collection("bookmarked_commands")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudBookmarkedCommand::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeInterviewAttempts(): Flow<List<CloudInterviewAttempt>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/interview_attempts"
        emitAll(
            db.collection("users").document(uid).collection("interview_attempts")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudInterviewAttempt::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveStudyProfile(
        displayName: String,
        selectedDiplomaTrack: String,
        preferredSoftwareVersion: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val safeName = displayName.trim().ifBlank { "Civil Engineering Student" }.take(100)
        val safeTrack = selectedDiplomaTrack.trim().ifBlank { DiplomaTrack.ALL_TRACKS.name }.take(60)
        val safeVersion = preferredSoftwareVersion.trim().take(80)

        val existingSnap = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        if (existingSnap != null && existingSnap.exists()) {
            val updateMap = mapOf(
                "displayName" to safeName,
                "selectedDiplomaTrack" to safeTrack,
                "preferredSoftwareVersion" to safeVersion,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updateMap).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val createMap = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "selectedDiplomaTrack" to safeTrack,
                "preferredSoftwareVersion" to safeVersion,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(createMap).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        }
    }

    suspend fun saveBookmarkedCommand(entity: BookmarkedCommandEntity): Result<String> = runCatching {
        val uid = requireUserId()
        val safeId = entity.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifBlank {
            "cmd_${System.currentTimeMillis()}"
        }
        val docRef = db.collection("users").document(uid).collection("bookmarked_commands").document(safeId)
        val payload = mapOf(
            "id" to safeId,
            "userId" to uid,
            "software" to entity.software.ifBlank { "Civil/BIM Software" }.take(100),
            "versionScope" to entity.versionScope.ifBlank { "All Versions" }.take(100),
            "taskTitle" to entity.taskTitle.ifBlank { "Saved Command" }.take(200),
            "command" to entity.command.ifBlank { "COMMAND" }.take(500),
            "shortcut" to entity.shortcut.ifBlank { "Shortcut" }.take(200),
            "menuPath" to entity.menuPath.ifBlank { "Menu Path" }.take(500),
            "siteExample" to entity.siteExample.take(1000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            safeId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getBookmarkedCommandById(targetUserId: String, commandId: String): Result<CloudBookmarkedCommand?> = runCatching {
        val docRef = db.collection("users").document(targetUserId).collection("bookmarked_commands").document(commandId)
        try {
            val snap = docRef.get().await()
            snap.toObject(CloudBookmarkedCommand::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun removeBookmarkedCommand(commandId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val safeId = commandId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
        val docRef = db.collection("users").document(uid).collection("bookmarked_commands").document(safeId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun saveInterviewAttempt(
        attemptId: String,
        attempt: InterviewAttemptEntity
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeId = attemptId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifBlank {
            "attempt_${System.currentTimeMillis()}"
        }
        val docRef = db.collection("users").document(uid).collection("interview_attempts").document(safeId)
        val payload = mapOf(
            "id" to safeId,
            "userId" to uid,
            "topic" to attempt.topic.ifBlank { "General Civil & BIM" }.take(120),
            "questionType" to attempt.questionType.ifBlank { "Technical" }.take(60),
            "question" to attempt.questionAsked.ifBlank { "Interview Question" }.take(1000),
            "userAnswer" to attempt.studentAnswer.ifBlank { "Submitted Answer" }.take(2000),
            "score" to attempt.scoreOutOf10.coerceIn(0, 10),
            "whatWasGood" to attempt.whatWasGood.take(1500),
            "betterSampleAnswer" to attempt.betterSampleAnswer.take(2000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            safeId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
