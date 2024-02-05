package cloud.caroline

import cloud.caroline.core.CarolineSdk
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CarolineSdkTest {

    @Test
    fun testThrowsWithNoConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            CarolineSdk {}
        }
    }

    @Test
    fun testThrowsWithInvalidServerUrl() {
        val error = assertFailsWith<IllegalArgumentException> {
            CarolineSdk {
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
            CarolineSdk {
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
            CarolineSdk {
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
