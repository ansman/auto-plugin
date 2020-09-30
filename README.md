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
  id("se.ansman.autoplugin") version "0.3.0"
}

// You can optionally configure it:
autoPlugin {
  // This applies the KSP Gradle Plugin for you. This is optional, but you need to apply the KSP plugin manually if
  // you don't call this.
  applyKsp()

  // You can later call enableVerification() to re-enable it
  disableVerification()
  
  // You can later call verboseLogging(enabled = false) to disable verbose logging
  verboseLogging()
}
```

If you do not want to use the Gradle Plugin you need to set everything up yourself:
* Apply the KSP plugin.
* Add `build/generated/ksp/src/main/resources` to your `main` source set.
* Optionally you can pass the KSP compiler options `autoPlugin.verify` and/or `autoPlugin.verbose` (both are booleans).
* Add the required dependencies:
```kotlin
dependencies {
  implementation("se.ansman.autoplugin:api:0.3.0")
  ksp("se.ansman.autoplugin:compiler-ksp:0.3.0")
}
```

### Annotations Processing
If you aren't using Kotlin or does not want to use KSP you can add it as an annotation processor:
```kotlin
dependencies {
  implementation("se.ansman.autoplugin:api:0.3.0")
  annotationsProcessor("se.ansman.autoplugin:compile:0.3.0")
  // For kotlin projects you'll use this instead
  kapt("se.ansman.autoplugin:compile:0.3.0")
}
```

Attribution
---
This library is heavily influenced by [Auto Service](https://github.com/google/auto/tree/master/service).

The KSP implementation is based on [auto-service-ksp](https://github.com/ZacSweers/auto-service-ksp).

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
