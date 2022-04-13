package cloud.caroline

import cloud.caroline.core.CarolineSDK
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CarolineSDKTest {

    @Test
    fun testThrowsWithInvalidServerUrl() {
        val error = assertFailsWith<IllegalArgumentException> {
            CarolineSDK {
                serverUrl = "test"
            }
        }
        assertTrue(error.message.orEmpty().contains("serverUrl"))
    }
}