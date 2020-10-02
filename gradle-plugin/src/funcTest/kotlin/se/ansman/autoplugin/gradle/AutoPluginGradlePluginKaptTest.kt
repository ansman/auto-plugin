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

import java.io.File

@Suppress("FunctionName")
internal class AutoPluginGradlePluginKaptTest : BaseAutoPluginGradlePluginTest() {

    override fun setupBuildScripts() {
        settingsFile.writeText(
            """
            rootProject.name = "test"
            """.trimIndent()
        )
        buildFile.writeText(
            """
            plugins {
              kotlin("jvm") version embeddedKotlinVersion
              kotlin("kapt") version embeddedKotlinVersion
            }
            
            repositories {
              maven {
                setUrl("$localMavenRepo")
              }
              google()
              jcenter()
            }
            
            dependencies {
              implementation(gradleApi())
              implementation("se.ansman.autoplugin:api:$pluginVersion")
              kapt("se.ansman.autoplugin:compiler:$pluginVersion")
            }
            
            kapt {
              arguments {
                arg("autoPlugin.verbose", "true")
              }
            }
            
            """.trimIndent()
        )
    }

    override fun resourceFile(path: String): File = testProjectDir.resolve("build/tmp/kapt3/classes/main/$path")

    override val disableVerification: String
        get() = """
            kapt {
              arguments {
                arg("autoPlugin.verify", "false")
              }
            }
        """.trimIndent()
}