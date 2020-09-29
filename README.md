AutoPlugin
===
![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/ansman/auto-plugin?include_prereleases)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.ansman.autoplugin/api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.ansman.autoplugin/api)
![Bintray](https://img.shields.io/bintray/v/ansman/auto-plugin/api)
![GitHub](https://img.shields.io/github/license/ansman/auto-plugin.svg?color=green&style=popout)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ansman/auto-plugin/Check)

A configuration/metadata generator for Gradle Plugins.

Gradle uses a special file inside the META-INF metadata to find declared plugins. Keeping these files up to date can be
tedious and error prone. AutoPlugin simplifies this by generating these files at compile time.

Simply annotate your plugin with `@AutoPlugin` and provide the Plugin ID. AutoPlugin will do the rest.

Example
---
```kotlin
package com.example

import org.gradle.api.Plugin
import se.ansman.autoplugin.AutoPlugin

@AutoPlugin("my-plugin")
abstract class MyPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    // …
  }
}
```

AutoPlugin will generate the file `META-INF/gradle-plugins/my-plugin.properties` containing
```plain
implementation-class=com.example.MyPlugin
```

This will allow Gradle to find your plugin, letting users apply it using the ID:
```kotlin
plugins {
  id("my-plugin")
  // or
  `my-plugin`
}
// or
apply(plugin = "my-plugin")
```

Setup
---

### KSP
If using Kotlin it's preferred to use the Gradle plugin which will use [KSP](https://github.com/google/ksp) to generate
the file:
```kotlin
plugins {
  kotlin("jvm")
  id("symbol-processing") version "<version>"
  id("se.ansman.autoplugin") version "0.2.0"
}

// You can optionally configure it:
autoPlugin {
  // By default he plugin verifies that the Plugin ID is valid. If there are issues with the validation it can
  // be disabled like this.
  verificationEnabled.set(false)
}
```

If you do not want to use the plugin you need to duplicate [what the plugin does](https://github.com/ansman/auto-plugin/tree/main/gradle-plugin/src/main/kotlin/se/ansman/autoplugin/gradle/AutoPluginGradlePlugin.kt).

### Annotations Processing
If you aren't using Kotlin or does not want to use KSP you can add it as an annotation processor:
```kotlin
dependencies {
  implementation("se.ansman.autoplugin:api:0.2.0")
  annotationsProcessor("se.ansman.autoplugin:compile:0.2.0")
  // For kotlin projects you'll use this instead
  kapt("se.ansman.autoplugin:compile:0.2.0")
}
```

Attribution
---
This library is heavily influenced by [Auto Service](https://github.com/google/auto/tree/master/service).

License
---
```plain
Copyright 2020 Nicklas Ansman Giertz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
