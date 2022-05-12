package cloud.caroline

import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.Test
import kotlin.test.assertEquals

class ProjectRouteTests : BaseProjectRouteTest() {

    @Test
    fun test() = setupTestApp { client ->
        createUser()
        val token = client.get("/api/core/token") {
            header("Authorization", "")
        }

        val response = client.get("/api/project") {
            header("Authorization", "")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
