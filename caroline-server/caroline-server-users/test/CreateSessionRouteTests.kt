package cloud.caroline.user

import cloud.caroline.core.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateSessionRouteTests : BaseUserRouteTest() {

    @Test
    fun testCreateSession() = setupTestApp { client ->
        client.createUser()
        val response = client.post("/api/user/session") {
            contentType(ContentType.Application.Json)
            setBody(CreateSessionBody("test", "test1234"))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val sessionResponse = response.body<CreateSessionResponse>()
        assertIs<CreateSessionResponse.Success>(sessionResponse)

        assertEquals("test", sessionResponse.user.displayName)
        assertEquals("test", sessionResponse.user.username)
        assertEquals("test@test.com", sessionResponse.user.email)
        assertEquals(setOf(), sessionResponse.permissions)
    }

    @Test
    fun testCreateSessionFailsWithIncorrectPassword() = setupTestApp { client ->
        client.createUser()
        val response = client.post("/api/user/session") {
            contentType(ContentType.Application.Json)
            setBody(CreateSessionBody("test", "1234567890"))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val sessionResponse = response.body<CreateSessionResponse>()
        assertIs<CreateSessionResponse.Failed>(sessionResponse)
        assertTrue(sessionResponse.errors.contains(CreateSessionResponse.SessionError.PASSWORD_INCORRECT))
    }

    @Test
    fun testCreateSessionFailsWithInvalidPassword() = setupTestApp { client ->
        client.createUser()
        val response = client.post("/api/user/session") {
            contentType(ContentType.Application.Json)
            setBody(CreateSessionBody("test", "0"))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val sessionResponse = response.body<CreateSessionResponse>()
        assertIs<CreateSessionResponse.Failed>(sessionResponse)
        assertTrue(sessionResponse.errors.contains(CreateSessionResponse.SessionError.PASSWORD_INVALID))
    }

    @Test
    fun testCreateSessionFailsWithUnknownUsername() = setupTestApp { client ->
        client.createUser()
        val response = client.post("/api/user/session") {
            contentType(ContentType.Application.Json)
            setBody(CreateSessionBody("notauser", "test1234"))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val sessionResponse = response.body<CreateSessionResponse>()
        assertIs<CreateSessionResponse.Failed>(sessionResponse)
        assertTrue(sessionResponse.errors.contains(CreateSessionResponse.SessionError.USERNAME_NOT_FOUND))
    }

    @Test
    fun testCreateSessionFailsWithInvalidUsername() = setupTestApp { client ->
        client.createUser()
        val response = client.post("/api/user/session") {
            contentType(ContentType.Application.Json)
            setBody(CreateSessionBody("0", "test1234"))
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val sessionResponse = response.body<CreateSessionResponse>()
        assertIs<CreateSessionResponse.Failed>(sessionResponse)
        assertTrue(sessionResponse.errors.contains(CreateSessionResponse.SessionError.USERNAME_INVALID))
    }

    suspend fun HttpClient.createUser() = createUser("test", "test1234", "test@test.com")

    suspend fun HttpClient.createUser(username: String, password: String, email: String) {
        post("/api/user") {
            contentType(ContentType.Application.Json)
            val body = CreateUserBody(
                username = username,
                password = password,
                inviteCode = null,
                email = email,
            )
            setBody(body)
        }.body<CreateUserResponse.Success>()
    }
}
