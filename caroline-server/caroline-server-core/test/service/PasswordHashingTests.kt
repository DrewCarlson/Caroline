package cloud.caroline.service

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHashingTests {

    @Test
    fun testHashPassword() {
        assertNotEquals("test1234", CarolineUserService.hashPassword("test1234"))
        assertFalse(CarolineUserService.hashPassword("test1234").contains("test1234"))
    }

    @Test
    fun testVerifyPassword() {
        val hash = "\$2y\$10\$HHIul.9ENQFvn7d0x20TGu4Ag4KhN./QrfObGDNkp1av8saSnVdji"
        assertTrue(CarolineUserService.verifyPassword("test1234", hash))
        assertFalse(CarolineUserService.verifyPassword("0test1234", hash))
        assertFalse(CarolineUserService.verifyPassword("Test1234", hash))
    }
}
