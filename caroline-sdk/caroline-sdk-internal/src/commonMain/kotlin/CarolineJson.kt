package cloud.caroline.internal

import kotlinx.serialization.json.Json

public val carolineJson: Json = Json {
    isLenient = true
    prettyPrint = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = false
    classDiscriminator = "__type"
}
