AutoPlugin
===
A configuration/metadata generator for Gradle Plugins.

Gradle uses a special file inside the META-INF metadata to find declared plugins. Keeping these files up to date can be 
tedious and error prone. AutoPlugin simplifies this by generating these files at compile time.

Simply annotated your plugin with `@AutoPlugin` and provide the Plugin ID. AutoPlugin will do the rest.

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
To get started you'll need to include the api as regular dependency and the compiler as an annotation processor:
```kotlin
dependencies {
  implementation("se.ansman.autoplugin:api:0.1.0")
  kapt("se.ansman.autoplugin:compile:0.1.0")
  // For non kotlin projects you'll use something like this
  annotationsProcessor("se.ansman.autoplugin:compile:0.1.0")
}
```

Then simply annotate each plugin with `@AutoPlugin` and assign it an ID.

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