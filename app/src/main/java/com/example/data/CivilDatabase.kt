package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CivilTutorDao {
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Query("UPDATE chat_sessions SET mode = :mode, selectedTopic = :topic, softwareVersion = :version, title = :title WHERE id = :sessionId")
    suspend fun updateSessionMetadata(
        sessionId: Long,
        mode: String,
        topic: String,
        version: String,
        title: String
    )

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesSnapshot(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearSessionMessages(sessionId: Long)

    @Query("SELECT * FROM bookmarked_commands ORDER BY savedAt DESC")
    fun getAllBookmarkedCommands(): Flow<List<BookmarkedCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bookmarkCommand(command: BookmarkedCommandEntity)

    @Query("DELETE FROM bookmarked_commands WHERE id = :id")
    suspend fun removeBookmarkedCommand(id: String)

    @Query("SELECT * FROM interview_attempts ORDER BY timestamp DESC")
    fun getAllInterviewAttempts(): Flow<List<InterviewAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewAttempt(attempt: InterviewAttemptEntity): Long
}

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        BookmarkedCommandEntity::class,
        InterviewAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CivilTutorDatabase : RoomDatabase() {
    abstract fun dao(): CivilTutorDao

    companion object {
        @Volatile
        private var INSTANCE: CivilTutorDatabase? = null

        fun getInstance(context: Context): CivilTutorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CivilTutorDatabase::class.java,
                    "civil_bim_tutor_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class CivilTutorRepository(private val dao: CivilTutorDao) {
    val allSessions: Flow<List<ChatSessionEntity>> = dao.getAllSessions()
    val bookmarkedCommands: Flow<List<BookmarkedCommandEntity>> = dao.getAllBookmarkedCommands()
    val interviewAttempts: Flow<List<InterviewAttemptEntity>> = dao.getAllInterviewAttempts()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForSession(sessionId)

    suspend fun getMessagesSnapshot(sessionId: Long): List<ChatMessageEntity> =
        dao.getMessagesSnapshot(sessionId)

    suspend fun createNewSession(
        title: String,
        mode: StudyMode,
        topic: String,
        softwareVersion: String
    ): Long {
        val sessionId = dao.insertSession(
            ChatSessionEntity(
                title = title,
                mode = mode.name,
                selectedTopic = topic,
                softwareVersion = softwareVersion
            )
        )
        // Always insert the mandatory opening greeting asking whether the student wants to
        // learn a topic, practice software, or do a mock interview!
        val welcomeMsg = ChatMessageEntity(
            sessionId = sessionId,
            isUser = false,
            mainText = "Hello! Welcome to your Civil Engineering & BIM Diploma Studio. I'm your friendly site and BIM tutor.\n\nBefore we jump in, how would you like to start our session today?\n1. **Learn a Topic** (AutoCAD 2D/3D, MicroStation, Revit Arch/Struct, Revit Plugins, Bluebeam Revu, Navisworks 4D/5D, CDE, or COBie)\n2. **Practice Software** (with exact commands, menu paths, shortcuts, and a site exercise for your software version)\n3. **Do a Mock Interview** (one question at a time—mixing Technical & HR—with a score out of 10 and a better sample answer)",
            siteExample = "Tip: Tap one of the 3 Mode Cards below or tell me which topic or software version you are working with!",
            quickCheckQuestion = "Would you like to: (1) Learn a topic, (2) Practice software, or (3) Do a mock interview?"
        )
        dao.insertMessage(welcomeMsg)
        return sessionId
    }

    suspend fun updateSessionMetadata(
        sessionId: Long,
        mode: StudyMode,
        topic: String,
        version: String,
        title: String
    ) {
        dao.updateSessionMetadata(sessionId, mode.name, topic, version, title)
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.clearSessionMessages(sessionId)
        dao.deleteSession(sessionId)
    }

    suspend fun insertMessage(message: ChatMessageEntity): Long = dao.insertMessage(message)

    suspend fun toggleBookmark(item: SoftwareCommandItem, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            dao.removeBookmarkedCommand(item.id)
        } else {
            dao.bookmarkCommand(
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

    suspend fun saveCustomCommandBookmark(
        software: String,
        versionScope: String,
        taskTitle: String,
        commandBlock: String,
        siteExample: String
    ) {
        val id = "custom_${System.currentTimeMillis()}"
        dao.bookmarkCommand(
            BookmarkedCommandEntity(
                id = id,
                software = software.ifBlank { "Civil/BIM Software" },
                versionScope = versionScope.ifBlank { "All Versions" },
                taskTitle = taskTitle,
                command = commandBlock.lines().firstOrNull() ?: commandBlock,
                shortcut = "Saved from AI Tutor",
                menuPath = commandBlock,
                siteExample = siteExample
            )
        )
    }

    suspend fun removeBookmarkedCommand(id: String) {
        dao.removeBookmarkedCommand(id)
    }

    suspend fun recordInterviewAttempt(attempt: InterviewAttemptEntity) {
        dao.insertInterviewAttempt(attempt)
    }
}
