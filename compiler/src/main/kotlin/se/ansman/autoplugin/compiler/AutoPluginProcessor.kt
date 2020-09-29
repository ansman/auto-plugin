/*
 * Copyright (c) 2020. Nicklas Ansman Giertz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.ansman.autoplugin.compiler

import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import se.ansman.autoplugin.AutoPlugin
import se.ansman.autoplugin.compiler.AutoPluginHelpers.Errors
import se.ansman.autoplugin.compiler.AutoPluginHelpers.writeResourceFile
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind
import javax.tools.StandardLocation

@Suppress("UnstableApiUsage")
@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class AutoPluginProcessor : AbstractProcessor() {
    /**
     * Maps plugin IDs to the implementing type.
     *
     * For example
     * ```
     * "library" -> "com.example.LibraryPlugin"
     * ```
     */
    private val providers = mutableMapOf<String, TypeElement>()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(AutoPlugin::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion? = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean =
        try {
            processImpl(annotations, roundEnv)
        } catch (e: Exception) {
            fatalError(with(StringWriter()) {
                e.printStackTrace(PrintWriter(this))
                toString()
            })
            true
        }

    private fun processImpl(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            generateConfigFiles()
        } else {
            processAnnotations(annotations, roundEnv)
        }
        return true
    }

    private fun processAnnotations(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
        val elements: Set<Element> = roundEnv.getElementsAnnotatedWith(AutoPlugin::class.java)
        log(annotations.toString())
        log(elements.toString())

        val plugin = processingEnv.elementUtils.getTypeElement("org.gradle.api.Plugin")
            ?.asType()
            ?.let(processingEnv.typeUtils::erasure)
            ?: run {
                fatalError("$GRADLE_PLUGIN not found. Gradle must be included as a dependency.")
                return
            }

        for (e in elements) {
            val typeElement = MoreElements.asType(e)
            val autoPlugin = typeElement.getAnnotation(AutoPlugin::class.java)
            val pluginId = autoPlugin.value

            if (!AutoPluginHelpers.validatePluginId(pluginId) { log(it) }) {
                error(Errors.pluginIdFormat(pluginId), e)
                continue
            }

            if (!processingEnv.typeUtils.isAssignable(typeElement.asType(), plugin)) {
                error(Errors.missingSuperclass(typeElement.getBinaryName()), e)
                continue
            }
            val existing = providers.put(pluginId, typeElement)
            if (existing != null) {
                error("Multiple plugins found with the same ID: '$pluginId' ($existing also implements it)", e)
            }
        }
    }

    private fun generateConfigFiles() {
        val filer = processingEnv.filer
        for ((id, implementationClass) in providers.entries) {
            val resourceFile = AutoPluginHelpers.fileNameForPluginId(id)
            log("Working on resource file: $resourceFile")

            try {
                val existingImplementation = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceFile)
                    .openReader(true)
                    .use { it.readText() }
                    .removePrefix("implementation-class=")
                error("Plugin with ID $id already exists ($existingImplementation)", implementationClass)
                continue
            } catch (e: IOException) {
                // No-op
            }

            try {
                filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile, implementationClass)
                    .openWriter()
                    .use { it.writeResourceFile(implementationClass.getBinaryName()) }
            } catch (e: IOException) {
                fatalError("Unable to create $resourceFile, $e", implementationClass)
            }
        }
    }

    /**
     * Returns the binary name of a reference type. For example, `com.example.Foo$Bar`, instead of
     * `com.example.Foo.Bar`.
     */
    private fun TypeElement.getBinaryName(className: String = simpleName.toString()): String {
        val enclosingElement = enclosingElement
        return when {
            enclosingElement !is PackageElement -> {
                val typeElement = MoreElements.asType(enclosingElement)
                typeElement.getBinaryName("${typeElement.simpleName}$$className")
            }
            enclosingElement.isUnnamed -> className
            else -> "${enclosingElement.qualifiedName}.$className"
        }
    }

    private fun log(msg: String) {
        if (processingEnv.options.containsKey("debug")) {
            processingEnv.messager.printMessage(Kind.NOTE, msg)
        }
    }

    private fun error(msg: String, element: Element) {
        processingEnv.messager.printMessage(Kind.ERROR, msg, element)
    }

    private fun fatalError(msg: String) {
        processingEnv.messager.printMessage(Kind.ERROR, "FATAL ERROR: $msg")
    }

    private fun fatalError(msg: String, element: Element) {
        processingEnv.messager.printMessage(Kind.ERROR, "FATAL ERROR: $msg", element)
    }

    companion object {
        private const val GRADLE_PLUGIN = "org.gradle.api.Plugin"
    }
}