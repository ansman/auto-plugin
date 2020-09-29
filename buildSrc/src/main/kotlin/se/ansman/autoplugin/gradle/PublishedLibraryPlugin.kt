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

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import setupTestPublishing
import java.util.*

@Suppress("UnstableApiUsage")
abstract class PublishedLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(LibraryPlugin::class.java)
            plugins.apply("org.jetbrains.dokka")
            plugins.apply("maven-publish")
            plugins.apply("com.jfrog.bintray")
            group = "se.ansman.autoplugin"
            version = providers.gradleProperty("version").forUseAtConfigurationTime().get()

            val sourcesJar = tasks.register("sourcesJar", Jar::class.java) { task ->
                task.from(project.sourceSets.getByName("main").allSource)
                task.archiveClassifier.set("sources")
            }

            val dokkaJar = tasks.register("dokkaJavadocJar", Jar::class.java) { task ->
                task.from(tasks.named("dokkaJavadoc"))
                task.archiveClassifier.set("javadoc")
            }

            val publication = publishing.publications.register("library", MavenPublication::class.java) { publication ->
                publication.from(project.components.getByName("java"))
                publication.artifactId = project.path.removePrefix(":").replace(':', '-')
                publication.artifact(sourcesJar)
                publication.artifact(dokkaJar)

                with(publication.pom) {
                    name.set("AutoPlugin")
                    description.set("Generates configuration files for Gradle Plugins.")
                    url.set("https://github.com/ansman/auto-plugin")
                    issueManagement {
                        it.system.set("GitHub Issues")
                        it.url.set("https://github.com/ansman/auto-plugin/issues")
                    }
                    licenses {
                        it.license { license ->
                            license.name.set("Apache 2.0")
                            license.url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        it.developer { developer ->
                            developer.id.set("nicklas.ansman")
                            developer.name.set("Nicklas Ansman Giertz")
                            developer.email.set("nicklas@ansman.se")
                        }
                    }
                    scm { scm ->
                        scm.url.set("https://github.com/ansman/auto-plugin")
                        scm.connection.set("scm:git:git://github.com/ansman/auto-plugin.git")
                        scm.developerConnection.set("scm:git:ssh://github.com/ansman/auto-plugin.git")
                    }
                }
            }

            // This is needed for the gradle publish plugin
            with(target.artifacts) {
                add("archives", dokkaJar)
                add("archives", sourcesJar)
            }

            extensions.configure<BintrayExtension>("bintray") { bintray ->
                with(bintray) {
                    user = providers.gradleProperty("BINTRAY_USER").forUseAtConfigurationTime().orNull
                    key = providers.gradleProperty("BINTRAY_API_KEY").forUseAtConfigurationTime().orNull
                    setPublications(publication.name)
                    with(pkg) {
                        val pub = publication.get()
                        val pom = pub.pom
                        repo = "auto-plugin"
                        name = pub.artifactId
                        desc = pom.description.get()
                        issueTrackerUrl = "https://github.com/ansman/auto-plugin/issues"
                        websiteUrl = pom.url.get()
                        vcsUrl = pom.url.get()
                        publish = true
                        publicDownloadNumbers = true
                        with(version) {
                            desc = pom.description.get()
                            released = Date().toString()
                            with(gpg) {
                                sign = true
                                passphrase = providers.gradleProperty("BINTRAY_GPG_PASSWORD").forUseAtConfigurationTime().orNull
                            }
                        }
                    }
                }
            }

            tasks.named("bintrayUpload") {
                it.doLast { printPublishedPublications() }
            }

            tasks.named("publishLibraryPublicationToMavenLocal") {
                it.doLast { printPublishedPublications() }
            }

            setupTestPublishing()
        }
    }

    private fun Project.printPublishedPublications() {
        extensions.getByType(PublishingExtension::class.java)
            .publications
            .filterIsInstance<MavenPublication>()
            .forEach { publication ->
                println("Published artifact ${publication.groupId}:${publication.artifactId}:${publication.version}")
            }
    }
}