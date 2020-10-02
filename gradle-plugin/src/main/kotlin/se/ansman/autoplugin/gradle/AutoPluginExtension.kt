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

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configures the [AutoPluginGradlePlugin].
 */
@Suppress("LeakingThis", "UnstableApiUsage")
public abstract class AutoPluginExtension @Inject constructor(private val project: Project) {
    /**
     * Override the AutoPlugin version used. By default the same version as the AutoPlugin Gradle Plugin is used but
     * if you need to test a snapshot version it's convenient to be able to override it.
     */
    public abstract val version: Property<String>

    init {
        with(version) {
            convention(BuildMetadata.VERSION)
            finalizeValueOnRead()
        }
    }

    /** Enables verbose logging. By default only errors are logged. */
    public fun verboseLogging(enabled: Boolean = true) {
        setOption("verbose", enabled.toString())
    }

    /**
     * Disables verification of plugins and plugin ids.
     *
     * By default the compiler will verify certain things:
     * * The plugin must implement [Plugin].
     * * The plugin ID must be valid:
     *   * Must only contain a-z, A-Z, 0-9, '.' and '-'.
     *   * Must not start or end with '.'.
     *   * Must not contain two consecutive periods ('..').
     *   * Must contain at least one character.
     */
    public fun disableVerification() {
        setOption("verify", false.toString())
    }

    /** Re-enabled verification after a previous call to [disableVerification]. */
    public fun enableVerification() {
        setOption("verify", true.toString())
    }

    private fun setOption(name: String, value: String) {
        project.pluginManager.withPlugin("symbol-processing") {
            project.extensions.configure<KspExtension>("ksp") {
                it.arg("autoPlugin.$name", value)
            }
        }
    }

    /** Call this to automatically apply the KSP Gradle Plugin. */
    public fun applyKsp() {
        project.pluginManager.apply(KspGradleSubplugin::class.java)
    }
}