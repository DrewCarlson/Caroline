package cloud.caroline.user

import cloud.caroline.hashPassword
import cloud.caroline.verifyPassword
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHashingTests {

    @Test
    fun testHashPassword() {
        assertNotEquals("test1234", hashPassword("test1234"))
        assertFalse(hashPassword("test1234").contains("test1234"))
    }

    @Test
    fun testVerifyPassword() {
        val hash = "\$2y\$10\$HHIul.9ENQFvn7d0x20TGu4Ag4KhN./QrfObGDNkp1av8saSnVdji"
        assertTrue(verifyPassword("test1234", hash))
        assertFalse(verifyPassword("0test1234", hash))
        assertFalse(verifyPassword("Test1234", hash))
    }
}
