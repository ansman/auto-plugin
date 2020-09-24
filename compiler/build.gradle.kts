plugins {
    library
    id("kotlin-kapt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":api"))
    compileOnly(deps.auto.service.api)
    kapt(deps.auto.service.compiler)

    compileOnly(deps.incap.api)
    kapt(deps.incap.compiler)
    implementation(deps.auto.common)

    testImplementation(platform(deps.junit.bom))
    testImplementation(deps.junit.jupiter)
    testImplementation(deps.truth)
    testImplementation(deps.compileTesting)
    testImplementation(gradleApi())
}

kapt.includeCompileClasspath = false

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}