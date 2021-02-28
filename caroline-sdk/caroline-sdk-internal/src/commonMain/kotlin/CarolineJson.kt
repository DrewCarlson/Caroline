package drewcarlson.caroline.internal

import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
public val carolineJson: Json = Json {
    isLenient = true
    prettyPrint = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    useArrayPolymorphism = false
    classDiscriminator = "__type"
    allowStructuredMapKeys = true
}
