package cloud.caroline

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.User
import cloud.caroline.core.models.UserCredentials
import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.Before

open class BaseProjectRouteTest {

    lateinit var testDb: MongoDatabase

    @Before
    fun setup() {
        val kmongo = MongoClient.create("mongodb://localhost")
        testDb = kmongo.getDatabase("caroline-tests")
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
                //install(TegralOpenApiKtor)
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
        val userCollection = testDb.getCollection<User>("users")
        val userCredentialsCollection = testDb.getCollection<UserCredentials>("user-credentials")
        userCollection.insertOne(
            User(ObjectId.get().toString(), "test", "test", "test@test.com"),
        )
        userCredentialsCollection.insertOne(
            UserCredentials(ObjectId.get().toString(), "", permissions),
        )
    }
}
