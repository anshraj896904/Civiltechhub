package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CivilCurriculumCatalog
import com.example.data.DiplomaTrack
import com.example.data.SoftwareCategory
import com.example.data.StudyMaterialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and diploma curriculum topics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Civil Tutor", appName)

        val topicIds = CivilCurriculumCatalog.topics.map { it.id }
        assertTrue(topicIds.contains("autocad"))
        assertTrue(topicIds.contains("microstation"))
        assertTrue(topicIds.contains("revit_arch_struct"))
        assertTrue(topicIds.contains("revit_plugins"))
        assertTrue(topicIds.contains("bluebeam_revu"))
        assertTrue(topicIds.contains("navisworks_4d_5d"))
        assertTrue(topicIds.contains("cde_iso19650"))
        assertTrue(topicIds.contains("cobie"))
    }

    @Test
    fun `verify searchable study index categorizes by software and filters by diploma track`() {
        val allItems = CivilCurriculumCatalog.studyMaterialIndex
        assertTrue(allItems.isNotEmpty())

        // Verify AutoCAD, Revit, and Navisworks software categories are populated
        val autocadItems = CivilCurriculumCatalog.filterStudyMaterials(
            query = "",
            diplomaTrack = DiplomaTrack.ALL_TRACKS,
            softwareCategory = SoftwareCategory.AUTOCAD
        )
        val revitItems = CivilCurriculumCatalog.filterStudyMaterials(
            query = "",
            diplomaTrack = DiplomaTrack.ALL_TRACKS,
            softwareCategory = SoftwareCategory.REVIT
        )
        val navisworksItems = CivilCurriculumCatalog.filterStudyMaterials(
            query = "",
            diplomaTrack = DiplomaTrack.ALL_TRACKS,
            softwareCategory = SoftwareCategory.NAVISWORKS
        )

        assertTrue(autocadItems.isNotEmpty())
        assertTrue(revitItems.isNotEmpty())
        assertTrue(navisworksItems.isNotEmpty())
        assertTrue(autocadItems.all { it.softwareCategory == SoftwareCategory.AUTOCAD })
        assertTrue(revitItems.all { it.softwareCategory == SoftwareCategory.REVIT })
        assertTrue(navisworksItems.all { it.softwareCategory == SoftwareCategory.NAVISWORKS })

        // Verify Diploma Track filtering
        val coordinationTrackItems = CivilCurriculumCatalog.filterStudyMaterials(
            query = "",
            diplomaTrack = DiplomaTrack.BIM_COORDINATION_4D_5D,
            softwareCategory = SoftwareCategory.ALL
        )
        assertTrue(coordinationTrackItems.isNotEmpty())
        assertTrue(coordinationTrackItems.all { it.diplomaTracks.contains(DiplomaTrack.BIM_COORDINATION_4D_5D) })
        assertTrue(coordinationTrackItems.any { it.softwareCategory == SoftwareCategory.NAVISWORKS })
        assertFalse(coordinationTrackItems.any { it.softwareCategory == SoftwareCategory.MICROSTATION })

        // Verify search query + material type filtering
        val clashSearch = CivilCurriculumCatalog.filterStudyMaterials(
            query = "Clash Detective",
            diplomaTrack = DiplomaTrack.BIM_COORDINATION_4D_5D,
            softwareCategory = SoftwareCategory.NAVISWORKS,
            materialType = StudyMaterialType.COMMAND_REFERENCE
        )
        assertEquals(1, clashSearch.size)
        assertEquals("cmd_nw_clash", clashSearch.first().id)

        // Verify 3D Image Teach scenes and filter
        val visual3DItems = CivilCurriculumCatalog.filterStudyMaterials(
            query = "",
            diplomaTrack = DiplomaTrack.ALL_TRACKS,
            softwareCategory = SoftwareCategory.ALL,
            materialType = StudyMaterialType.VISUAL_3D_TEACH
        )
        assertEquals(CivilCurriculumCatalog.topics.size, visual3DItems.size)
        assertEquals(8, CivilCurriculumCatalog.visual3DScenes.size)
        assertTrue(CivilCurriculumCatalog.visual3DScenes.all { it.callouts.isNotEmpty() && it.stageDescriptions.size == 4 })
    }
}
