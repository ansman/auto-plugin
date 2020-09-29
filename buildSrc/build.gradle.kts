@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    `java-gradle-plugin`
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    val kotlinVersion = "1.4.10"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}

gradlePlugin {
    plugins {
        register("library") {
            id = name
            implementationClass = "se.ansman.autoplugin.gradle.LibraryPlugin"
        }
        register("published-library") {
            id = name
            implementationClass = "se.ansman.autoplugin.gradle.PublishedLibraryPlugin"
        }
    }
}