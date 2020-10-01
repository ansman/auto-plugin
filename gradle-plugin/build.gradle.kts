@file:Suppress("UnstableApiUsage")

import com.squareup.kotlinpoet.*

plugins {
    `published-library`
    id("symbol-processing") version deps.kotlin.ksp.version
    id("com.gradle.plugin-publish") version "0.12.0"
}

buildscript {
    dependencies {
        classpath(deps.kotlinPoet)
    }
}

kotlin {
    explicitApi()
}

val funcTestSourceSet: NamedDomainObjectProvider<SourceSet> = sourceSets.register("funcTest") {
    java.srcDir(file("src/funcTest/kotlin"))
    resources.srcDir(file("src/funcTest/resources"))
    compileClasspath += sourceSets.getByName("main").output +
            configurations.getByName("testRuntimeClasspath") +
            configurations.getByName("testCompileClasspath")
    runtimeClasspath += output + compileClasspath
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(project(":api"))
    implementation(gradleApi())
    implementation(deps.kotlin.ksp.gradlePlugin)
    ksp(project(":compiler-ksp"))

    "funcTestImplementation"(project(":api"))
    "funcTestImplementation"(platform(deps.junit.bom))
    "funcTestImplementation"(deps.junit.jupiter)
    "funcTestImplementation"(deps.truth)
    "funcTestImplementation"(gradleTestKit())
}

pluginBundle {
    website = "https://github.com/ansman/auto-plugin"
    vcsUrl = "https://github.com/ansman/auto-plugin"
    description = "Generates configuration files for Gradle Plugins."
    tags = listOf("plugin-development")

    plugins {
        create("autoPlugin") {
            displayName = "AutoPlugin"
            id = "se.ansman.autoplugin"
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = providers.gradleProperty("version").forUseAtConfigurationTime().get()
    }
}

tasks.withType<Javadoc>().configureEach { isEnabled = false }

val generatedBuildDir: Provider<Directory> = layout.buildDirectory.dir("generated/build-metadata")
sourceSets {
    getByName("main") {
        java.srcDir(generatedBuildDir)
        resources.srcDir(layout.buildDirectory.dir("generated/ksp/src/main/resources"))
    }
}

val generateBuildMetadata = tasks.register("generateBuildMetadata") {
    @Suppress("UnstableApiUsage")
    val version = project.providers.gradleProperty("version")
    inputs.property("version", version)
    outputs.dir(generatedBuildDir)
    doFirst {
        FileSpec.builder("se.ansman.autoplugin.gradle", "BuildMetadata")
            .addType(
                TypeSpec.objectBuilder("BuildMetadata")
                    .addModifiers(KModifier.INTERNAL)
                    .addProperty(
                        PropertySpec.builder("VERSION", STRING, KModifier.CONST)
                            .initializer("%S", version.get())
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(generatedBuildDir.get().asFile)
    }
}

tasks.named("compileKotlin").configure { dependsOn(generateBuildMetadata) }

val funcTest = tasks.register("funcTest", Test::class.java) {
    useJUnitPlatform()
    testLogging.events("passed", "skipped", "failed")
    testClassesDirs = funcTestSourceSet.get().output.classesDirs
    classpath = funcTestSourceSet.get().runtimeClasspath
    dependsOn(
        "publishTestLibraryPublicationToTestRepository",
        ":api:publishTestLibraryPublicationToTestRepository",
        ":compiler-common:publishTestLibraryPublicationToTestRepository",
        ":compiler-ksp:publishTestLibraryPublicationToTestRepository"
    )
    val pluginVersion = providers.gradleProperty("version")
        .forUseAtConfigurationTime()
        .get()
    systemProperty("pluginVersion", pluginVersion)
    systemProperty("symbolProcessingVersion", deps.kotlin.ksp.version)
    systemProperty("localMavenRepo", testMavenRepo.absolutePath)
}

tasks.named("check") { dependsOn(funcTest) }

sourceSets.main.configure {
    resources.srcDir(layout.buildDirectory.dir("generated/ksp/src/main/resources"))
}

tasks.named("processResources").configure {
    dependsOn("compileKotlin")
}