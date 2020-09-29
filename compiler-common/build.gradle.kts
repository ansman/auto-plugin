plugins {
    `published-library`
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(deps.junit.jupiter)
    testImplementation(deps.truth)
}