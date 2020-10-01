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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import se.ansman.autoplugin.AutoPlugin

/**
 * The plugin for AutoPlugin. Will do the following things:
 * * Install the [AutoPluginExtension] with the name `autoPlugin`
 * * Add `build/generated/ksp/src/main/resources` to the main source set
 * * Make the `processResources` task depend on the `compileKotlin` task
 * * Add an `implementation` dependency on the API
 * * Add a `ksp` dependency on the AutoPlugin compiler
 */
@AutoPlugin("se.ansman.autoplugin")
public abstract class AutoPluginGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.create("autoPlugin", AutoPluginExtension::class.java, target)

            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                with(property("sourceSets") as SourceSetContainer) {
                    with(getByName("main")) {
                        resources.srcDir(target.layout.buildDirectory.dir("generated/ksp/src/main/resources"))
                    }
                }
                dependencies.add("implementation", "se.ansman.autoplugin:api:${BuildMetadata.VERSION}")

                // By default `processResources` doesn't depend on `compileKotlin` so by the time the resource file
                // is generated the resources can have already been processed. This ensures the file is generated first.
                tasks.named("processResources").configure {
                    it.dependsOn("compileKotlin")
                }
            }

            pluginManager.withPlugin("symbol-processing") {
                dependencies.add("ksp", "se.ansman.autoplugin:compiler:${BuildMetadata.VERSION}")
            }
        }
    }
}