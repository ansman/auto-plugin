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

@Suppress("ClassName", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")
object deps {

    object auto {
        const val common = "com.google.auto:auto-common:0.11"
        object service {
            const val version = "1.0-rc7"
            const val api = "com.google.auto.service:auto-service-annotations:$version"
            const val compiler = "com.google.auto.service:auto-service:$version"
        }
    }

    object incap {
        const val version = "0.3"
        const val api = "net.ltgt.gradle.incap:incap:$version"
        const val compiler = "net.ltgt.gradle.incap:incap-processor:$version"
    }

    object junit {
        const val bom = "org.junit:junit-bom:5.7.0"
        const val jupiter = "org.junit.jupiter:junit-jupiter"
    }

    const val truth = "com.google.truth:truth:1.0.1"
    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.11"
}