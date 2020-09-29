plugins {
    `published-library`
    id("symbol-processing") version deps.kotlin.ksp.version
    id("dev.zacsweers.autoservice.ksp") version "0.1.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("compiler-embeddable"))
    implementation(project(":api"))
    implementation(project(":compiler-common"))
    implementation(deps.kotlin.ksp.api)
    implementation(deps.kotlinPoet)
    testImplementation(deps.compileTesting.ksp)
    testImplementation(project(":compiler-test"))
}