
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import java.io.File

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


val Project.testMavenRepo: File
    get() = rootProject.buildDir.resolve("repo")

fun Project.setupTestPublishing() {
    with(extensions.getByType(PublishingExtension::class.java)) {
        repositories {
            it.maven { maven ->
                maven.name = "test"
                maven.url = uri(testMavenRepo)
            }
        }

        publications.register("testLibrary", MavenPublication::class.java) { publication ->
            publication.from(project.components.getByName("java"))
            publication.artifactId = project.path.removePrefix(":").replace(':', '-')
        }
    }
    val cleanTestRepo = tasks.register("cleanTestRepo", Delete::class.java) {
        it.delete = setOf(testMavenRepo)
    }
    tasks.named("clean") {
        it.dependsOn(cleanTestRepo)
    }
}