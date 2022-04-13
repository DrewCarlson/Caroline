package cloud.caroline

import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Ignore
import kotlin.test.Test

class BundledServerTest {
    @Test
    @Ignore
    fun testRoot() {
        testApplication {
            environment {
                (config as MapApplicationConfig).apply {
                    put("caroline.mongoUrl", "mongodb://localhost")
                    put("caroline.databaseName", "caroline-tests")
                    put("caroline.jwtIssuer", "localhost")
                    put("caroline.jwtRealm", "Caroline")
                    put("caroline.jwtSecret", "CHANGEMETOASECRET")
                }
            }
            application {
                module(testing = true)
            }
        }
    }
}
