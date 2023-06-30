package cloud.caroline.user

import cloud.caroline.coreModule
import cloud.caroline.userModule
import com.mongodb.ConnectionString
import guru.zoroark.tegral.openapi.ktor.TegralOpenApiKtor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

open class BaseUserRouteTest {

    @Before
    fun setup() {
        val kmongo = KMongo.createClient(ConnectionString("mongodb://localhost"))
        runBlocking { kmongo.getDatabase("caroline-tests").coroutine.drop() }
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
