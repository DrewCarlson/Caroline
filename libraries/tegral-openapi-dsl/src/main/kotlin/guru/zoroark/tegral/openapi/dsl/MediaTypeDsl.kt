/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.openapi.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * DSL for [media type objects](https://spec.openapis.org/oas/v3.1.0#media-type-object
 *
 * This DSL has extensions that allow to easily define schemas from `KType` objects (including types of arbitrary
 * classes).
 */
@TegralDsl
public interface MediaTypeDsl {
    /**
     * Sets the schema of this object to be of the given `KType`. The type will be converted to a schema.
     *
     * DSLs implementing `MediaTypeDsl` must properly register any non-standard schema using the [OpenApiDslContext],
     * which will provide a proper schema with a `$ref`.
     */
    @TegralDsl
    public fun schema(ktype: KType)

    /**
     * The schema for this object.
     */
    @TegralDsl
    public var schema: Schema<*>?

    /**
     * The example for this object. Should match the given schema.
     */
    @TegralDsl
    public var example: Any?
    // TODO examples, encoding
}

/**
 * Set the schema to be of type `T`.
 *
 * The type `T` will be converted to a schema.
 */
@TegralDsl
public inline fun <reified T> MediaTypeDsl.schema() = schema(typeOf<T>())

/**
 * Set the schema to be of type `T`, with the given object set as the example.
 *
 * The type `T` will be converted to a schema.
 */
@TegralDsl
public inline fun <reified T> MediaTypeDsl.schema(example: T) {
    this.example = example
    schema(typeOf<T>())
}

/**
 * Set the schema to be of type [ktype], with the given object set as the example.
 */
@TegralDsl
public fun <T> MediaTypeDsl.schema(ktype: KType, example: T) {
    this.example = example
    schema(ktype)
}

/**
 * A builder for [MediaTypeDsl].
 */
public class MediaTypeBuilder(private val context: OpenApiDslContext) : MediaTypeDsl, Buildable<MediaType> {
    override var schema: Schema<*>? = null
    private var _example: Any? = null
    private var _exampleWasSet: Boolean = false
    override var example: Any?
        get() = _example
        set(value) {
            _example = value
            _exampleWasSet = true
        }

    override fun build(): MediaType = MediaType().apply {
        if (_exampleWasSet) example = this@MediaTypeBuilder.example
        schema = this@MediaTypeBuilder.schema
    }

    override fun schema(ktype: KType) {
        context.computeAndRegisterSchema(ktype).also { schema = it }
    }
}
