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

import java.io.Writer


internal object AutoPluginHelpers {
    private val validPluginCharacters = Regex("[a-zA-Z0-9.-]+")

    fun validatePluginId(pluginId: String, logger: (String) -> Unit = {}): Boolean {
        return when {
            pluginId.isEmpty() -> {
                logger("Plugin IDs must not be empty: $pluginId")
                false
            }
            !validPluginCharacters.matches(pluginId) -> {
                logger("Plugin IDs must only contain a-z, A-Z, 0-9, '.' and '-': $pluginId")
                false
            }
            pluginId.first() == '.' || pluginId.last() == '.' -> {
                logger("Plugin IDs must not start or end with '.': $pluginId")
                false
            }
            ".." in pluginId -> {
                logger("Plugin IDs cannot contain '..': $pluginId")
                false
            }
            else -> true
        }
    }

    fun fileNameForPluginId(pluginId: String): String = "META-INF/gradle-plugins/$pluginId.properties"

    fun Writer.writeResourceFile(implementationClass: String) {
        write("implementation-class=$implementationClass")
    }
}