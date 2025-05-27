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
import io.swagger.v3.oas.models.servers.Server

/**
 * DSL for the [server object](https://spec.openapis.org/oas/v3.1.0#server-object).
 */
@TegralDsl
public interface ServerDsl

/**
 * Builder for [ServerDsl].
 */
public class ServerBuilder(private val url: String) : ServerDsl, Buildable<Server> {
    // TODO missing properties here
    public override fun build(): Server = Server().apply {
        url(this@ServerBuilder.url)
    }
}
