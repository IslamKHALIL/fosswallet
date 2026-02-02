package nz.eloque.foss_wallet.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HashTest {

    @Test
    fun testSha256BasicHash() {
        val input = "test"
        val hash = Hash.sha256(input)
        
        // SHA-256 of "test" should be consistent
        assertEquals(
            "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
            hash
        )
    }

    @Test
    fun testSha256EmptyString() {
        val input = ""
        val hash = Hash.sha256(input)
        
        // SHA-256 of empty string
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            hash
        )
    }

    @Test
    fun testSha256Consistency() {
        val input = "consistent_value"
        val hash1 = Hash.sha256(input)
        val hash2 = Hash.sha256(input)
        
        // Same input should always produce same hash
        assertEquals(hash1, hash2)
    }

    @Test
    fun testSha256DifferentInputs() {
        val input1 = "value1"
        val input2 = "value2"
        
        val hash1 = Hash.sha256(input1)
        val hash2 = Hash.sha256(input2)
        
        // Different inputs should produce different hashes
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun testSha256LongString() {
        val input = "a".repeat(1000)
        val hash = Hash.sha256(input)
        
        // Hash length should always be 64 characters (256 bits in hex)
        assertEquals(64, hash.length)
        
        // Hash should only contain hex characters
        assert(hash.all { it in "0123456789abcdef" })
    }

    @Test
    fun testSha256SpecialCharacters() {
        val input = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val hash = Hash.sha256(input)
        
        // Hash should be valid regardless of input characters
        assertEquals(64, hash.length)
        assert(hash.all { it in "0123456789abcdef" })
    }

    @Test
    fun testSha256Unicode() {
        val input = "Hello 世界 🌍"
        val hash = Hash.sha256(input)
        
        // Hash should handle unicode properly
        assertEquals(64, hash.length)
        assert(hash.all { it in "0123456789abcdef" })
    }
}