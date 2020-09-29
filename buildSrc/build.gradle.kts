@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("se.ansman.autoplugin") version "0.2.0"
    id("symbol-processing") version "1.4.10-dev-experimental-20200924"
}

repositories {
    google()
    mavenLocal()
    jcenter()
}

dependencies {
    val kotlinVersion = "1.4.10"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
}