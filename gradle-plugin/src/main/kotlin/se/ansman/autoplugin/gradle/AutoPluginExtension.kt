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

import org.gradle.api.provider.Property

/**
 * Configures the [AutoPluginGradlePlugin].
 */
@Suppress("LeakingThis", "UnstableApiUsage")
abstract class AutoPluginExtension {
    /**
     * Whether verification of plugins and plugin ids is enabled.
     *
     * By default the compiler will verify certain things:
     * * The plugin must implement [Plugin].
     * * The plugin ID must be valid:
     *   * Must only contain a-z, A-Z, 0-9, '.' and '-'.
     *   * Must not start or end with '.'.
     *   * Must not contain two consecutive periods ('..').
     *   * Must contain at least one character.
     */
    abstract val verificationEnabled: Property<Boolean>

    /** Enables verbose logging. By default only errors are logged. */
    abstract val verboseLogging: Property<Boolean>

    init {
        verificationEnabled.apply {
            finalizeValueOnRead()
            convention(true)
        }
        verboseLogging.apply {
            finalizeValueOnRead()
            convention(false)
        }
    }
}