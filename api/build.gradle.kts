plugins {
    `published-library`
}

dependencies {
    compileOnly(gradleApi())
}

kotlin {
    explicitApi()
}