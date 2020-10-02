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
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.COMPILATION_ERROR
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import se.ansman.autoplugin.compiler.internal.Errors
import java.io.ByteArrayOutputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.PrintStream

abstract class BaseAutoPluginProcessorTest {
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

        assertThat(result.exitCode).isEqualTo(OK)
        assertThat(result.getResourceAsText("META-INF/gradle-plugins/some-plugin.properties"))
            .isEqualTo("implementation-class=com.example.SomePlugin")
        assertThat(result.getResourceAsText("META-INF/gradle-plugins/com.example.plugin.properties"))
            .isEqualTo("implementation-class=com.example.SomeOtherPlugin")
    }

    @Test
    fun `class must implement Plugin`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin")
                abstract class SomePlugin {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.missingSuperclass("com.example.SomePlugin"))
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

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.duplicatePlugins("some-plugin", "com.example.SomePlugin"))
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

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.pluginIdFormat("some-plugin!"))
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

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.pluginIdFormat(".some-plugin"))
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

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.pluginIdFormat("some-plugin."))
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

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.pluginIdFormat("some..plugin"))
    }

    @Test
    fun `plugin ids must not be empty`() {
        val result = compile(
            """
                package com.example
                
                import org.gradle.api.Plugin
                import org.gradle.api.Project
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("")
                abstract class SomePlugin : Plugin<Project> {
                    override fun apply(target: Project) {}
                }
            """
        )

        assertThat(result.exitCode).isEqualTo(COMPILATION_ERROR)
        result.assertMessage(Errors.pluginIdFormat(""))
    }

    @Test
    fun `without verification`() {
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
            """,
            verify = false
        )

        assertThat(result.exitCode).isEqualTo(OK)
        assertThat(result.getResourceAsText("META-INF/gradle-plugins/some-plugin!.properties"))
            .isEqualTo("implementation-class=com.example.SomePlugin")
    }

    private fun CompileResult.assertMessage(messages: String) {
        messages.lineSequence().forEach { message -> assertThat(this.messages).contains(message) }
    }

    protected open fun compile(@Language("kotlin") code: String, verify: Boolean = true): CompileResult =
        with(KotlinCompilation()) {
            val output = ByteArrayOutputStream()
            val outputPrinter = PrintStream(output)
            val oldErr = System.err
            System.setErr(outputPrinter)
            try {
                messageOutputStream = object : FilterOutputStream(System.out) {
                    override fun write(b: Int) {
                        super.write(b)
                        output.write(b)
                    }

                    override fun write(b: ByteArray) {
                        super.write(b)
                        output.write(b)
                    }

                    override fun write(b: ByteArray, off: Int, len: Int) {
                        super.write(b, off, len)
                        output.write(b, off, len)
                    }

                    override fun flush() {
                        super.flush()
                        output.flush()
                    }
                }
                sources = listOf(SourceFile.kotlin("Code.kt", code))
                inheritClassPath = true
                correctErrorTypes = true
                configure(verify)

                val result = compile()
                CompileResult(
                    exitCode = result.exitCode,
                    messages = output.toString(),
                    getResourceAsText = { name -> getResourceAsText(this, result, name) }
                )
            } finally {
                System.setErr(oldErr)
            }


        }

    protected open fun KotlinCompilation.configure(verify: Boolean) {}

    @Throws(IOException::class)
    protected abstract fun getResourceAsText(
        compilation: KotlinCompilation,
        result: KotlinCompilation.Result,
        name: String
    ): String

    class CompileResult(
        val exitCode: KotlinCompilation.ExitCode,
        val messages: String,
        val getResourceAsText: (name: String) -> String
    )
}
