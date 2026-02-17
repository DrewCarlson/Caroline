package cloud.caroline

import cloud.caroline.core.models.CreateSessionResponse
import cloud.caroline.core.models.CreateUserResponse
import cloud.caroline.internal.buildSealedSchema
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.json.Json
import kotlin.test.Test

class SchemaTest {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = false
    }

    @Test
    fun printCreateSessionResponseSealedSchema() {
        val sealed = buildSealedSchema<CreateSessionResponse>()
        println("=== CreateSessionResponse sealed schema ===")
        println(json.encodeToString(JsonSchema.serializer(), sealed.schema))
        println("=== Component schemas ===")
        for ((name, schema) in sealed.componentSchemas) {
            println("--- $name ---")
            println(json.encodeToString(JsonSchema.serializer(), schema))
        }
    }

    @Test
    fun printCreateUserResponseSealedSchema() {
        val sealed = buildSealedSchema<CreateUserResponse>()
        println("=== CreateUserResponse sealed schema ===")
        println(json.encodeToString(JsonSchema.serializer(), sealed.schema))
        println("=== Component schemas ===")
        for ((name, schema) in sealed.componentSchemas) {
            println("--- $name ---")
            println(json.encodeToString(JsonSchema.serializer(), schema))
        }
    }
}
