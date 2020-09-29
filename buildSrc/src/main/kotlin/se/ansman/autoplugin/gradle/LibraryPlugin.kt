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

import deps
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

abstract class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply("org.jetbrains.kotlin.jvm")

            extensions.configure(JavaPluginExtension::class.java) {
                it.sourceCompatibility = JavaVersion.VERSION_1_8
                it.targetCompatibility = JavaVersion.VERSION_1_8
            }

            with(dependencies) {
                add("testImplementation", platform(deps.junit.bom))
            }

            tasks.named("test", Test::class.java) { test ->
                test.useJUnitPlatform()
                test.testLogging.events("passed", "skipped", "failed")
            }

            extensions.configure<KotlinJvmProjectExtension>("kotlin") { kotlin ->
                kotlin.target.compilations.configureEach {
                    it.kotlinOptions.jvmTarget = "1.8"
                }
            }
        }
    }
}