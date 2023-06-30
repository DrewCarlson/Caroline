package cloud.caroline

import cloud.caroline.core.CarolineSDK
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CarolineSDKTest {

    @Test
    fun testThrowsWithNoConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            CarolineSDK {}
        }
    }

    @Test
    fun testThrowsWithInvalidServerUrl() {
        val error = assertFailsWith<IllegalArgumentException> {
            CarolineSDK {
                serverUrl = "test"
            }
        }
        assertTrue(
            error.message.orEmpty().contains("serverUrl"),
            "Exception should mention serverUrl",
        )
    }

    @Test
    fun testThrowsWithBlankProjectId() {
        val error = assertFailsWith<IllegalArgumentException> {
            CarolineSDK {
                serverUrl = "https://test"
                projectId = ""
            }
        }
        assertTrue(
            error.message.orEmpty().contains("projectId"),
            "Exception should mention projectId",
        )
    }

    @Test
    fun testThrowsWithBlankApiKey() {
        val error = assertFailsWith<IllegalArgumentException> {
            CarolineSDK {
                serverUrl = "https://test"
                projectId = "abc"
                apiKey = ""
            }
        }
        assertTrue(
            error.message.orEmpty().contains("apiKey"),
            "Exception should mention apiKey",
        )
    }
}
