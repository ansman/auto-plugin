pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "symbol-processing" ->
                    useModule("com.google.devtools.ksp:symbol-processing:${requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "auto-plugin"

include(":api")
include(":compiler")
include(":gradle-plugin")