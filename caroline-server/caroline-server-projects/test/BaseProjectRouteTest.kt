package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import com.mongodb.ConnectionString
import guru.zoroark.tegral.openapi.ktor.TegralOpenApiKtor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.Before
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

open class BaseProjectRouteTest {

    lateinit var testDb: CoroutineDatabase

    @Before
    fun setup() {
        val kmongo = KMongo.createClient(ConnectionString("mongodb://localhost"))
        testDb = kmongo.getDatabase("caroline-tests").coroutine
        runBlocking { testDb.drop() }
    }

    fun setupTestApp(body: suspend (HttpClient) -> Unit) {
        testApplication {
            environment {
                config = MapApplicationConfig().apply {
                    put("caroline.mongoUrl", "mongodb://localhost")
                    put("caroline.databaseName", "caroline-tests")
                    put("caroline.jwtIssuer", "localhost")
                    put("caroline.jwtRealm", "Caroline")
                    put("caroline.jwtSecret", "CHANGEMETOASECRET")
                }
            }
            application {
                install(TegralOpenApiKtor)
                coreModule()
                projectModule()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(json)
                }
            }
            body(client)
        }
    }

    suspend fun createUser(permissions: Set<Permission> = setOf(Permission.Global)) {
        val userCollection = testDb.getCollection<User>()
        val userCredentialsCollection = testDb.getCollection<UserCredentials>()
        userCollection.insertOne(
            User(ObjectId.get().toString(), "test", "test", "test@test.com"),
        )
        userCredentialsCollection.insertOne(
            UserCredentials(ObjectId.get().toString(), "", permissions),
        )
    }
}
