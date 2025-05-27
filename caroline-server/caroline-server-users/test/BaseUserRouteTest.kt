package cloud.caroline.user

import cloud.caroline.coreModule
import cloud.caroline.userModule
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.Before

open class BaseUserRouteTest {

    @Before
    fun setup() {
        val kmongo = MongoClient.create("mongodb://localhost")
        runBlocking { kmongo.getDatabase("caroline-tests").drop() }
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
                coreModule()
                userModule()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(cloud.caroline.json)
                }
            }
            body(client)
        }
    }
}
