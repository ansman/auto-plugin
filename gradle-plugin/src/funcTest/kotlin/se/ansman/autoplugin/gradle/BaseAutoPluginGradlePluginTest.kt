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

package se.ansman.autoplugin.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files

@Suppress("FunctionName")
internal abstract class BaseAutoPluginGradlePluginTest {
    @TempDir
    lateinit var testProjectDir: File

    protected lateinit var settingsFile: File
    protected  lateinit var buildFile: File

    protected val localMavenRepo: String get() = System.getProperty("localMavenRepo")
    protected val pluginVersion: String get() = System.getProperty("pluginVersion")

    @BeforeEach
    fun setup() {
        settingsFile = testProjectDir.resolve("settings.gradle.kts")
        buildFile = testProjectDir.resolve("build.gradle.kts")
        setupBuildScripts()
    }

    protected abstract fun setupBuildScripts()

    @Test
    fun `no sources`() {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--stacktrace")
            .forwardOutput()
            .build()
    }

    @Test
    fun `generates resource file`() {
        testProjectDir.resolve("src/main/kotlin/com/example")
            .apply { check(mkdirs()) }
            .resolve("ExamplePlugin.kt")
            .writeText(
                """
                package com.example
                
                import org.gradle.api.*
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("com.example.plugin")
                abstract class ExamplePlugin : Plugin<Project> {
                  override fun apply(target: Project) {}
                }
            """.trimIndent()
            )

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--stacktrace", "compileKotlin")
            .forwardOutput()
            .build()

        assertThat(resourceFile("META-INF/gradle-plugins/com.example.plugin.properties").readText())
            .isEqualTo("implementation-class=com.example.ExamplePlugin")
    }

    @Test
    fun `includes resource file in jar`() {
        testProjectDir.resolve("src/main/kotlin/com/example")
            .apply { check(mkdirs()) }
            .resolve("ExamplePlugin.kt")
            .writeText(
                """
                package com.example
                
                import org.gradle.api.*
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("com.example.plugin")
                abstract class ExamplePlugin : Plugin<Project> {
                  override fun apply(target: Project) {}
                }
            """.trimIndent()
            )

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--stacktrace", "jar")
            .forwardOutput()
            .build()

        val uri = URI.create("jar:file:${testProjectDir.resolve("build/libs/test.jar")}")
        val contents = FileSystems.newFileSystem(uri, emptyMap<String, Any?>()).use { fs ->
            Files.newBufferedReader(fs.getPath("META-INF/gradle-plugins/com.example.plugin.properties")).use {
                it.readText()
            }
        }
        assertThat(contents).isEqualTo("implementation-class=com.example.ExamplePlugin")
    }

    @Test
    fun `without verification`() {
        buildFile.appendText(disableVerification)
        testProjectDir.resolve("src/main/kotlin/com/example")
            .apply { check(mkdirs()) }
            .resolve("ExamplePlugin.kt")
            .writeText(
                """
                package com.example
                
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin(".some..invalid_id!")
                class ExamplePlugin
            """.trimIndent()
            )

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--stacktrace", "compileKotlin")
            .forwardOutput()
            .build()

        assertThat(resourceFile("META-INF/gradle-plugins/.some..invalid_id!.properties").readText())
            .isEqualTo("implementation-class=com.example.ExamplePlugin")
    }

    @Test
    fun `plugins in root package`() {
        buildFile.appendText(disableVerification)
        testProjectDir.resolve("src/main/kotlin/")
            .apply { check(mkdirs()) }
            .resolve("ExamplePlugin.kt")
            .writeText(
                """
                import se.ansman.autoplugin.AutoPlugin
                
                @AutoPlugin("some-plugin")
                class ExamplePlugin
            """.trimIndent()
            )

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("--stacktrace", "compileKotlin")
            .forwardOutput()
            .build()

        assertThat(resourceFile("META-INF/gradle-plugins/some-plugin.properties").readText())
            .isEqualTo("implementation-class=ExamplePlugin")
    }

    protected abstract fun resourceFile(path: String): File

    protected abstract val disableVerification: String
}