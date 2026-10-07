package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirebaseCloudRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveBookmarkedCommand_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirebaseCloudRepository(firestore)
        val uniqueId = "cmd_${UUID.randomUUID().toString().replace("-", "_")}"

        val entity = BookmarkedCommandEntity(
            id = uniqueId,
            software = "Revit",
            versionScope = "Revit 2025",
            taskTitle = "3D Rebar Cover Settings",
            command = "REBAR COVER",
            shortcut = "RC",
            menuPath = "Structure > Reinforcement > Rebar Cover",
            siteExample = "Set 40mm concrete cover for RCC column stirrups"
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveBookmarkedCommand(entity)
        }
        assertTrue(saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBookmarkedCommands().first { list -> list.any { it.id == uniqueId } }
        }
        assertTrue(emitted.any { it.id == uniqueId && it.command == "REBAR COVER" })
    }

    @Test
    fun getBookmarkedCommand_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirebaseCloudRepository(firestore)
        val uniqueId = "cmd_${UUID.randomUUID().toString().replace("-", "_")}"

        val entity = BookmarkedCommandEntity(
            id = uniqueId,
            software = "AutoCAD",
            versionScope = "AutoCAD 2025",
            taskTitle = "Layer Manager",
            command = "LAYER",
            shortcut = "LA",
            menuPath = "Home > Layers",
            siteExample = "Structural grid layers"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.saveBookmarkedCommand(entity).getOrThrow() }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirebaseCloudRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBookmarkedCommandById(aliceUid, uniqueId)
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeBookmarkedCommands_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repository = FirebaseCloudRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeBookmarkedCommands().first()
            }
            fail("Expected IllegalStateException or PermissionDenied when unauthenticated")
        } catch (expected: Exception) {
            assertTrue(expected is IllegalStateException || expected is FirebaseFirestoreException)
        }
    }

    @Test
    fun saveAndObserveStudyProfile_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirebaseCloudRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveStudyProfile(
                displayName = "Alice Civil Engineer",
                selectedDiplomaTrack = DiplomaTrack.BIM_COORDINATION_4D_5D.name,
                preferredSoftwareVersion = "Navisworks Manage 2025"
            )
        }
        assertTrue(saveResult.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudyProfile().first { it != null && it.selectedDiplomaTrack == DiplomaTrack.BIM_COORDINATION_4D_5D.name }
        }
        assertNotNull(profile)
        assertEquals("Alice Civil Engineer", profile?.displayName)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
