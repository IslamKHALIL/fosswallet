package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nz.eloque.foss_wallet.model.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Integration tests for database operations.
 * Tests Pass, Tag, and their relationships through the WalletDb.
 */
@RunWith(AndroidJUnit4::class)
class WalletDbIntegrationTest {

    private lateinit var db: WalletDb
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Use in-memory database for testing
        db = Room.inMemoryDatabaseBuilder(context, WalletDb::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndRetrievePass() = runBlocking {
        val testPass = createTestPass("test-pass-1")
        
        db.passDao().insert(testPass)
        
        val retrieved = db.passDao().findById("test-pass-1")
        assertNotNull("Pass should be retrievable after insertion", retrieved)
        assertEquals("test-pass-1", retrieved?.pass?.id)
        assertEquals("Test Pass", retrieved?.pass?.organizationName)
    }

    @Test
    fun testInsertMultiplePasses() = runBlocking {
        val pass1 = createTestPass("pass-1")
        val pass2 = createTestPass("pass-2")
        
        db.passDao().insert(pass1)
        db.passDao().insert(pass2)
        
        val allPasses = db.passDao().all().first()
        assertTrue("Should have at least 2 passes", allPasses.size >= 2)
    }

    @Test
    fun testDeletePass() = runBlocking {
        val testPass = createTestPass("test-pass-delete")
        
        db.passDao().insert(testPass)
        val beforeDelete = db.passDao().findById("test-pass-delete")
        assertNotNull("Pass should exist before deletion", beforeDelete)
        
        db.passDao().delete(testPass)
        
        val afterDelete = db.passDao().findById("test-pass-delete")
        assertNull("Pass should not exist after deletion", afterDelete)
    }

    @Test
    fun testReplacePass() = runBlocking {
        val pass1 = createTestPass("test-pass-replace", "Original Name")
        val pass2 = createTestPass("test-pass-replace", "Updated Name")
        
        db.passDao().insert(pass1)
        db.passDao().insert(pass2)
        
        val retrieved = db.passDao().findById("test-pass-replace")
        assertEquals("Updated Name", retrieved?.pass?.organizationName)
    }

    @Test
    fun testInsertAndRetrieveTag() = runBlocking {
        val tag = Tag(name = "TestTag", color = 0xFF0000)
        
        val tagId = db.tagDao().insert(tag)
        
        val allTags = db.tagDao().all().first()
        assertTrue("Tag should exist after insertion", allTags.any { it.id == tagId })
        assertTrue("Tag should have correct name", allTags.any { it.name == "TestTag" })
    }

    @Test
    fun testDeleteTag() = runBlocking {
        val tag = Tag(name = "DeleteTag", color = 0x00FF00)
        val tagId = db.tagDao().insert(tag)
        
        val tagToDelete = db.tagDao().all().first().find { it.id == tagId }
        assertNotNull("Tag should exist before deletion", tagToDelete)
        
        db.tagDao().delete(tagToDelete!!)
        
        val allTags = db.tagDao().all().first()
        assertFalse("Tag should not exist after deletion", allTags.any { it.id == tagId })
    }

    @Test
    fun testTagPassRelationship() = runBlocking {
        // Create a pass and a tag
        val testPass = createTestPass("tagged-pass")
        val tag = Tag(name = "Important", color = 0xFF0000)
        
        db.passDao().insert(testPass)
        val tagId = db.tagDao().insert(tag)
        
        // Tag the pass
        val crossRef = PassTagCrossRef(passId = "tagged-pass", tagId = tagId)
        db.passDao().tag(crossRef)
        
        // Retrieve the pass with tags
        val passWithTags = db.passDao().findById("tagged-pass")
        assertNotNull("Pass should be retrievable", passWithTags)
        assertTrue("Pass should have at least one tag", passWithTags!!.tags.isNotEmpty())
        assertEquals("Important", passWithTags.tags[0].name)
    }

    @Test
    fun testPassGroupAssociation() = runBlocking {
        val pass1 = createTestPass("group-pass-1")
        val pass2 = createTestPass("group-pass-2")
        
        db.passDao().insert(pass1)
        db.passDao().insert(pass2)
        
        // Create a group
        val group = PassGroup(id = 0, name = "Test Group", description = "Test Description")
        val groupId = db.passDao().insert(group)
        
        // Associate passes with group
        db.passDao().associate("group-pass-1", groupId)
        db.passDao().associate("group-pass-2", groupId)
        
        // Verify association
        val retrievedPass1 = db.passDao().findById("group-pass-1")
        val retrievedPass2 = db.passDao().findById("group-pass-2")
        
        assertEquals(groupId, retrievedPass1?.pass?.groupId)
        assertEquals(groupId, retrievedPass2?.pass?.groupId)
    }

    @Test
    fun testPassGroupDissociation() = runBlocking {
        val testPass = createTestPass("dissociate-pass")
        db.passDao().insert(testPass)
        
        val group = PassGroup(id = 0, name = "Test Group", description = "Test")
        val groupId = db.passDao().insert(group)
        
        // Associate
        db.passDao().associate("dissociate-pass", groupId)
        val associated = db.passDao().findById("dissociate-pass")
        assertEquals(groupId, associated?.pass?.groupId)
        
        // Dissociate
        db.passDao().dissociate("dissociate-pass")
        val dissociated = db.passDao().findById("dissociate-pass")
        assertNull("Pass should not have a group after dissociation", dissociated?.pass?.groupId)
    }

    @Test
    fun testDeleteGroup() = runBlocking {
        val group = PassGroup(id = 0, name = "Delete Group", description = "Test")
        val groupId = db.passDao().insert(group)
        
        val groupToDelete = PassGroup(id = groupId, name = "Delete Group", description = "Test")
        db.passDao().delete(groupToDelete)
        
        // Note: We can't directly query groups, but we can verify through pass associations
        // This test verifies the delete operation doesn't throw an exception
    }

    @Test
    fun testMultipleTagsOnSinglePass() = runBlocking {
        val testPass = createTestPass("multi-tag-pass")
        db.passDao().insert(testPass)
        
        val tag1 = Tag(name = "Tag1", color = 0xFF0000)
        val tag2 = Tag(name = "Tag2", color = 0x00FF00)
        val tag3 = Tag(name = "Tag3", color = 0x0000FF)
        
        val tagId1 = db.tagDao().insert(tag1)
        val tagId2 = db.tagDao().insert(tag2)
        val tagId3 = db.tagDao().insert(tag3)
        
        db.passDao().tag(PassTagCrossRef(passId = "multi-tag-pass", tagId = tagId1))
        db.passDao().tag(PassTagCrossRef(passId = "multi-tag-pass", tagId = tagId2))
        db.passDao().tag(PassTagCrossRef(passId = "multi-tag-pass", tagId = tagId3))
        
        val passWithTags = db.passDao().findById("multi-tag-pass")
        assertEquals(3, passWithTags?.tags?.size)
    }

    @Test
    fun testFlowUpdatesOnPassInsert() = runBlocking {
        val initialPasses = db.passDao().all().first()
        val initialCount = initialPasses.size
        
        val newPass = createTestPass("flow-test-pass")
        db.passDao().insert(newPass)
        
        val updatedPasses = db.passDao().all().first()
        assertEquals(initialCount + 1, updatedPasses.size)
    }

    @Test
    fun testFindUpdatablePasses() = runBlocking {
        val updatablePass = createTestPass("updatable-pass", webServiceUrl = "https://example.com/api")
        val nonUpdatablePass = createTestPass("non-updatable-pass", webServiceUrl = "")
        
        db.passDao().insert(updatablePass)
        db.passDao().insert(nonUpdatablePass)
        
        val updatablePasses = db.passDao().updatable()
        assertTrue("Should contain updatable pass", updatablePasses.any { it.id == "updatable-pass" })
        assertFalse("Should not contain non-updatable pass", updatablePasses.any { it.id == "non-updatable-pass" })
    }

    // Helper function to create a test pass
    private fun createTestPass(
        id: String, 
        orgName: String = "Test Pass",
        webServiceUrl: String = ""
    ): Pass {
        return Pass(
            id = id,
            type = PassType.Generic,
            organizationName = orgName,
            description = "Test Description",
            createdAt = Date(),
            relevantDate = null,
            expirationDate = null,
            voided = false,
            webServiceUrl = webServiceUrl,
            authenticationToken = null,
            serialNumber = "SN-$id",
            passTypeIdentifier = "pass.test.$id",
            barcode = null,
            groupId = null,
            backgroundColor = null,
            foregroundColor = null,
            labelColor = null,
            compatibilityMode = false
        )
    }
}