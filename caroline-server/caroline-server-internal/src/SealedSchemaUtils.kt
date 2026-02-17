package cloud.caroline.internal

import io.ktor.openapi.AdditionalProperties
import io.ktor.openapi.Components
import io.ktor.openapi.GenericElementString
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.JsonSchemaDiscriminator
import io.ktor.openapi.JsonType
import io.ktor.openapi.ReferenceOr
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.serializer

/**
 * Result of building a sealed class schema, containing both the top-level schema
 * (with `anyOf` and discriminator) and the component schemas for all subtypes
 * encountered during traversal (including nested sealed classes).
 *
 * Works around a limitation in Ktor's `KotlinxJsonSchemaInference` which generates
 * discriminator metadata for sealed classes but does not include `anyOf` or populate
 * component schemas for the subtypes.
 */
public data class SealedSchema(
    val schema: JsonSchema,
    val componentSchemas: Map<String, JsonSchema>,
)

public inline fun <reified T> buildSealedSchema(): SealedSchema {
    val descriptor = serializer<T>().descriptor
    val collector = SchemaCollector()
    val schema = collector.buildSealedSchema(descriptor)
    return SealedSchema(schema, collector.componentSchemas.toMap())
}

/**
 * Merges multiple [SealedSchema.componentSchemas] maps and creates a [Components] object.
 */
public fun buildComponentsWithSchemas(vararg sealedSchemas: SealedSchema): Components {
    val allSchemas = mutableMapOf<String, JsonSchema>()
    for (sealedSchema in sealedSchemas) {
        allSchemas.putAll(sealedSchema.componentSchemas)
    }
    return Components(schemas = allSchemas)
}

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal class SchemaCollector {
    val componentSchemas = mutableMapOf<String, JsonSchema>()

    fun buildSealedSchema(descriptor: SerialDescriptor): JsonSchema {
        val parentSimpleName = descriptor.serialName.substringAfterLast(".")
        val anyOfRefs = mutableListOf<ReferenceOr<JsonSchema>>()
        val discriminatorMapping = mutableMapOf<String, String>()

        if (descriptor.elementsCount >= 2) {
            val valueDescriptor = descriptor.getElementDescriptor(1)
            for (i in 0 until valueDescriptor.elementsCount) {
                val subtypeDescriptor = valueDescriptor.getElementDescriptor(i)
                val serialName = subtypeDescriptor.serialName
                val childSimpleName = serialName.substringAfterLast(".")
                val qualifiedName = "$parentSimpleName.$childSimpleName"
                val refPath = "#/components/schemas/$qualifiedName"

                componentSchemas[qualifiedName] = buildObjectSchema(subtypeDescriptor)
                anyOfRefs.add(ReferenceOr.Reference(refPath))
                discriminatorMapping[serialName] = refPath
            }
        }

        return JsonSchema(
            anyOf = anyOfRefs,
            discriminator = JsonSchemaDiscriminator(
                propertyName = "type",
                mapping = discriminatorMapping,
            ),
        )
    }

    private fun buildObjectSchema(descriptor: SerialDescriptor): JsonSchema {
        val properties = mutableMapOf<String, ReferenceOr<JsonSchema>>()
        val required = mutableListOf<String>()

        for (i in 0 until descriptor.elementsCount) {
            val elementName = descriptor.getElementName(i)
            val elementDescriptor = descriptor.getElementDescriptor(i)
            val isOptional = descriptor.isElementOptional(i)

            val isNullable = elementDescriptor.isNullable
            val elementSchema = toSchema(elementDescriptor)
            properties[elementName] = ReferenceOr.Value(elementSchema)

            if (!isOptional && !isNullable) {
                required.add(elementName)
            }
        }

        return JsonSchema(
            type = JsonType.OBJECT,
            title = descriptor.serialName,
            required = required.takeIf { it.isNotEmpty() },
            properties = properties.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * Returns a [ReferenceOr] that uses a `$ref` for sealed classes (since they are
     * registered as components) and an inline [JsonSchema] for everything else.
     */
    private fun toSchemaOrRef(descriptor: SerialDescriptor): ReferenceOr<JsonSchema> {
        if (descriptor.kind == PolymorphicKind.SEALED) {
            // Register the sealed subtypes as components and return a ref-less
            // inline anyOf schema. The actual subtype details live in components.
            return ReferenceOr.Value(buildSealedSchema(descriptor))
        }
        return ReferenceOr.Value(toSchema(descriptor))
    }

    private fun toSchema(descriptor: SerialDescriptor): JsonSchema {
        return when {
            descriptor.kind == PrimitiveKind.STRING ->
                JsonSchema(type = JsonType.STRING)

            descriptor.kind == PrimitiveKind.INT ||
                descriptor.kind == PrimitiveKind.LONG ||
                descriptor.kind == PrimitiveKind.SHORT ||
                descriptor.kind == PrimitiveKind.BYTE ->
                JsonSchema(type = JsonType.INTEGER)

            descriptor.kind == PrimitiveKind.FLOAT ||
                descriptor.kind == PrimitiveKind.DOUBLE ->
                JsonSchema(type = JsonType.NUMBER)

            descriptor.kind == PrimitiveKind.BOOLEAN ->
                JsonSchema(type = JsonType.BOOLEAN)

            descriptor.kind == SerialKind.ENUM -> {
                val enumValues = (0 until descriptor.elementsCount).map {
                    GenericElementString(descriptor.getElementName(it))
                }
                JsonSchema(type = JsonType.STRING, enum = enumValues)
            }

            descriptor.kind == StructureKind.LIST -> {
                val itemDescriptor = descriptor.getElementDescriptor(0)
                val itemRef = toSchemaOrRef(itemDescriptor)
                JsonSchema(
                    type = JsonType.ARRAY,
                    items = itemRef,
                )
            }

            descriptor.kind == StructureKind.MAP -> {
                val valueDescriptor = descriptor.getElementDescriptor(1)
                JsonSchema(
                    type = JsonType.OBJECT,
                    additionalProperties = AdditionalProperties.PSchema(
                        ReferenceOr.Value(toSchema(valueDescriptor))
                    ),
                )
            }

            descriptor.kind == PolymorphicKind.SEALED -> {
                buildSealedSchema(descriptor)
            }

            descriptor.kind == StructureKind.CLASS || descriptor.kind == StructureKind.OBJECT -> {
                buildObjectSchema(descriptor)
            }

            else -> JsonSchema()
        }
    }
}
