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
package se.ansman.autoplugin.compiler

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException

/** Tests the [AutoPluginProcessor]. */
class AutoPluginProcessorTest {
    @Test
    fun `resource file should be generated`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
                
                @AutoPlugin("com.example.plugin")
                abstract class SomeOtherPlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
        assertThat(result.classLoader.getResourceAsText("META-INF/gradle-plugins/some-plugin.properties"))
            .isEqualTo("implementation-class=com.example.SomePlugin")
        assertThat(result.classLoader.getResourceAsText("META-INF/gradle-plugins/com.example.plugin.properties"))
            .isEqualTo("implementation-class=com.example.SomeOtherPlugin")
    }

    @Test
    fun `plugin ids must be unique`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
                
                @AutoPlugin("some-plugin")
                abstract class SomeOtherPlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Plugin IDs must be unique.")
    }

    @Test
    fun `plugin ids must only have valid characters`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin!")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Plugin ID some-plugin! is not valid. Plugin IDs must:")
    }

    @Test
    fun `plugin ids must not start with period`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin(".some-plugin")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Plugin ID .some-plugin is not valid. Plugin IDs must:")
    }

    @Test
    fun `plugin ids must not end with period`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin.")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Plugin ID some-plugin. is not valid. Plugin IDs must:")
    }

    @Test
    fun `plugin ids must not contains double period`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some..plugin")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Plugin ID some..plugin is not valid. Plugin IDs must:")
    }

    private fun compile(@Language("kotlin") code: String) =
        with(KotlinCompilation()) {
            sources = listOf(
                SourceFile.kotlin("Code.kt", code)
            )
            annotationProcessors = listOf(AutoPluginProcessor())
            inheritClassPath = true
            compile()
        }
}

@Throws(IOException::class)
private fun ClassLoader.getResourceAsText(name: String): String =
    (getResourceAsStream(name) ?: throw FileNotFoundException(name)).reader().use { it.readText() }
