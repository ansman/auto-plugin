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

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers.fileNameForPluginId
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers.validatePluginId
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers.writeResourceFile
import java.io.StringWriter

class AutoPluginHelpersTest {
    @Test
    fun `plugin ID is valid`() {
        val logger = { _: String -> fail("Should not log anything") }
        assertThat(validatePluginId("valid", logger))
        assertThat(validatePluginId("valid.id", logger))
        assertThat(validatePluginId("com.example.valid", logger))
        assertThat(validatePluginId("valid-id", logger))
        assertThat(validatePluginId("com.example.valid-id", logger))
    }

    @Test
    fun `plugin ID contains invalid characters`() {
        var callCount = 0
        val logger = { _: String -> callCount += 1 }
        assertThat(validatePluginId("inv@lid", logger))
        assertThat(callCount).isEqualTo(1)
        assertThat(validatePluginId("invalid!", logger))
        assertThat(callCount).isEqualTo(2)
        assertThat(validatePluginId("invalid_id", logger))
        assertThat(callCount).isEqualTo(3)
    }

    @Test
    fun `plugin ID starting or ending with period`() {
        var callCount = 0
        val logger = { _: String -> callCount += 1 }
        assertThat(validatePluginId(".invalid", logger))
        assertThat(callCount).isEqualTo(1)
        assertThat(validatePluginId("invalid.", logger))
        assertThat(callCount).isEqualTo(2)
    }

    @Test
    fun `plugin ID contains double period`() {
        var callCount = 0
        val logger = { _: String -> callCount += 1 }
        assertThat(validatePluginId("invalid..id", logger))
        assertThat(callCount).isEqualTo(1)
    }

    @Test
    fun `file name`() {
        assertThat(fileNameForPluginId("example-plugin")).isEqualTo("META-INF/gradle-plugins/example-plugin.properties")
    }

    @Test
    fun `writing contents`() {
        val contents = with(StringWriter()) {
            writeResourceFile("com.example.SomePlugin")
            toString()
        }
        assertThat(contents).isEqualTo("implementation-class=com.example.SomePlugin")
    }
}