package cloud.caroline.user

import cloud.caroline.core.models.CreateUserBody
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.core.models.Permission
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.Test
import kotlin.test.*

class CreateUserRouteTests : BaseUserRouteTest() {

    @Test
    fun testFirstUserHasGlobalPermissions() = setupTestApp { client ->
        val response = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test",
                password = "test1234",
                inviteCode = null,
                email = "test@test.com",
            )
            setBody(body)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CreateUserResponse>()

        assertIs<CreateUserResponse.Success>(body)
    }

    @Test
    fun testSecondUserDoesNotHaveGlobalPermissions() = setupTestApp { client ->
        val response1 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test",
                password = "test1234",
                inviteCode = null,
                email = "test@test.com",
            )
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, response1.status)

        val response2 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test2",
                password = "test1234",
                inviteCode = null,
                email = "test@test2.com",
            )
            setBody(body)
        }

        assertEquals(HttpStatusCode.OK, response2.status)
        val body = response2.body<CreateUserResponse>()
        assertIs<CreateUserResponse.Success>(body)

        assertFalse(body.permissions.contains(Permission.Global))
    }

    @Test
    fun testCannotCreateUserWithDuplicateUsername() = setupTestApp { client ->
        val response1 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test",
                password = "test1234",
                inviteCode = null,
                email = "test@test.com",
            )
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, response1.status)

        val response2 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test",
                password = "test1234",
                inviteCode = null,
                email = "test@test2.com",
            )
            setBody(body)
        }

        assertEquals(HttpStatusCode.OK, response2.status)
        val body = response2.body<CreateUserResponse>()
        assertIs<CreateUserResponse.Failed>(body)

        assertEquals(body.usernameError, CreateUserResponse.UsernameError.ALREADY_EXISTS)
        assertNull(body.passwordError)
        assertNull(body.emailError)
    }

    @Test
    fun testCannotUserWithDuplicateEmail() = setupTestApp { client ->
        val response1 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test",
                password = "test1234",
                inviteCode = null,
                email = "test@test.com",
            )
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, response1.status)

        val response2 = client.post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = "test2",
                password = "test1234",
                inviteCode = null,
                email = "test@test.com",
            )
            setBody(body)
        }

        assertEquals(HttpStatusCode.OK, response2.status)
        val body = response2.body<CreateUserResponse>()
        assertIs<CreateUserResponse.Failed>(body)

        assertEquals(body.emailError, CreateUserResponse.EmailError.ALREADY_EXISTS)
        assertNull(body.usernameError)
        assertNull(body.passwordError)
    }
}
