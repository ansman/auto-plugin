/*
 * Copyright (c) 2020. Nicklas Ansman Giertz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.ansman.autoplugin.compiler.internal

internal object Errors {
    fun pluginIdFormat(pluginId: String): String =
        """
            Gradle plugin ID '$pluginId' is not valid. Plugin IDs must:
              • Contain at least one character
              • Only contain alphanumeric characters, '.', and '-'.
              • Not start or end with a '.' character.
              • Not contain consecutive '.' characters (i.e. '..').
        """.trimIndent()

    fun duplicatePlugins(pluginId: String, existingImplementation: String): String =
        "Multiple plugins found with the same ID: '$pluginId' ($existingImplementation also implements it)"


    fun missingSuperclass(implementationClass: String): String =
        "Class $implementationClass does not implement org.gradle.api.Plugin. All classes annotated with @AutoPlugin must be a Gradle Plugin."
}