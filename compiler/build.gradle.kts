plugins {
    `published-library`
    id("kotlin-kapt")
    id("symbol-processing") version deps.kotlin.ksp.version
    id("dev.zacsweers.autoservice.ksp") version "0.1.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":api"))
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(deps.kotlin.ksp.api)
    implementation(deps.kotlinPoet)

    compileOnly(deps.incap.api)
    kapt(deps.incap.compiler)
    implementation(deps.auto.common)

    testImplementation(deps.compileTesting.core)
    testImplementation(deps.kotlin.ksp.api)
    testImplementation(deps.compileTesting.ksp)
    testImplementation(gradleApi())
    testImplementation(platform(deps.junit.bom))
    testImplementation(deps.junit.jupiter)
    testImplementation(deps.truth)
}

kotlin {
    explicitApi()
}

kapt.includeCompileClasspath = false