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

package se.ansman.autoplugin

import org.gradle.api.Plugin

/**
 * An annotation for [Gradle Plugins][Plugin]. The annotation processor generates the
 * configuration files that allow the annotated class to be used as a plugin.
 *
 * The annotated class must implement [Plugin].
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class AutoPlugin(
    /**
     * The ID of the plugin. This is what consumers will use to include your plugin.
     *
     * Plugin IDs have some limitations, namely:
     * * It must not be an empty string.
     * *
     */
    val value: String
)