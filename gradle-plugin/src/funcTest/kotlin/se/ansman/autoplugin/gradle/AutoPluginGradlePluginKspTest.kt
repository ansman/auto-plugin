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
internal class AutoPluginGradlePluginKspTest : BaseAutoPluginGradlePluginTest() {
    override fun setupBuildScripts() {
        settingsFile.writeText(
            """
            pluginManagement {
              resolutionStrategy {
                eachPlugin {
                  when (requested.id.id) {
                    "se.ansman.autoplugin" -> useModule("se.ansman.autoplugin:gradle-plugin:${'$'}{requested.version}")
                  }
                }
              }
              repositories {
                maven {
                  setUrl("${System.getProperty("localMavenRepo")}")
                }
                gradlePluginPortal()
                google()
              }
            }
            
            rootProject.name = "test"
            """.trimIndent()
        )
        buildFile.writeText(
            """
            plugins {
              kotlin("jvm") version embeddedKotlinVersion
              id("se.ansman.autoplugin") version "${System.getProperty("pluginVersion")}"
            }
                
            buildscript {
              repositories {
                maven {
                  setUrl("${System.getProperty("localMavenRepo")}")
                }
                google()
                jcenter()
              }
            }
            
            repositories {
              maven {
                setUrl("${System.getProperty("localMavenRepo")}")
              }
              google()
              jcenter()
            }
            
            dependencies {
              implementation(gradleApi())
            }
            
            autoPlugin {
              applyKsp()
              // Re-enable this when https://github.com/google/ksp/issues/103 is resolved
              // verboseLogging()
            }
            
            """.trimIndent()
        )
    }

    override fun resourceFile(path: String): File =
        testProjectDir.resolve("build/generated/ksp/src/main/resources/$path")

    override val disableVerification: String
        get() = """
            autoPlugin {
              disableVerification()
            }
        """.trimIndent()
}