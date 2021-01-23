package drewcarlson.caroline

import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class BundledServerTest {
    @Test
    fun testRoot() {
        withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("caroline.mongoUrl", "mongodb://localhost")
                put("caroline.databaseName", "caroline-tests")
                put("caroline.jwtIssuer", "localhost")
                put("caroline.jwtRealm", "Caroline")
                put("caroline.jwtSecret", "CHANGEMETOASECRET")
            }
            module(testing = true)
        }) {
            //handleRequest(HttpMethod.Get, "/").apply {
            //assertEquals(HttpStatusCode.OK, response.status())
            //assertEquals("HELLO WORLD!", response.content)
            //}
        }
    }
}
