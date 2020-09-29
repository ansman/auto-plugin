plugins {
    `published-library`
    id("kotlin-kapt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":api"))
    implementation(project(":compiler-common"))
    compileOnly(deps.auto.service.api)
    kapt(deps.auto.service.compiler)

    compileOnly(deps.incap.api)
    kapt(deps.incap.compiler)
    implementation(deps.auto.common)

    testImplementation(deps.compileTesting.core)
    testImplementation(project(":compiler-test"))
    testImplementation(gradleApi())
}

kapt.includeCompileClasspath = false