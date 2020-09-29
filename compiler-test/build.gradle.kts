plugins {
    library
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":compiler-common"))
    api(deps.compileTesting.core)
    api(platform(deps.junit.bom))
    api(deps.junit.jupiter)
    api(deps.truth)
}