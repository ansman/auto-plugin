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

package se.ansman.autoplugin.compiler.ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.analyzer.AnalysisResult.CompilationErrorException
import se.ansman.autoplugin.AutoPlugin
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers.validatePluginId
import se.ansman.autoplugin.compiler.internal.AutoPluginHelpers.writeResourceFile
import se.ansman.autoplugin.compiler.internal.Errors
import java.io.IOException


@AutoService(SymbolProcessor::class)
internal class AutoPluginSymbolProcessor : SymbolProcessor {
    private lateinit var codeGenerator: CodeGenerator
    private lateinit var logger: KSPLogger
    private var verify = false
    private var verbose = false
    private val annotationName = AutoPlugin::class.java.name
    private val gradlePluginName = "org.gradle.api.Plugin"

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
        verify = options["autoPlugin.verify"]?.toBoolean() != false
        verbose = options["autoPlugin.verbose"]?.toBoolean() == true
    }

    override fun process(resolver: Resolver) {
        log("Starting processing")
        val autoPluginType = resolver.getClassDeclarationByName(resolver.getKSNameFromString(annotationName))
            ?.asType(emptyList())
            ?: compileError("@$annotationName type not found on the classpath.")

        val gradlePluginType = resolver.getClassDeclarationByName(resolver.getKSNameFromString(gradlePluginName))
            ?.asStarProjectedType()
            ?: compileError("$gradlePluginName not found on the class path.")

        resolver.getSymbolsWithAnnotation(annotationName)
            .asSequence()
            .filterIsInstance<KSClassDeclaration>()
            .fold(mutableMapOf<String, String>()) { providers, providerImplementer ->
                val annotation = providerImplementer.annotations.find { it.annotationType.resolve() == autoPluginType }
                    ?: compileError("@$annotationName annotation not found", providerImplementer)

                val pluginId = annotation.arguments
                    .single { it.name?.getShortName() == "value" }
                    .value as String

                if (verify && !validatePluginId(pluginId)) {
                    compileError(Errors.pluginIdFormat(pluginId), providerImplementer)
                }

                val implementationClass = providerImplementer.toBinaryName()
                if (verify && !gradlePluginType.isAssignableFrom(providerImplementer.asType(emptyList()))) {
                    compileError(Errors.missingSuperclass(implementationClass), providerImplementer)
                } else {
                    val existing = providers.put(pluginId, implementationClass)
                    if (verify && existing != null) {
                        compileError(Errors.duplicatePlugins(pluginId, existing), providerImplementer)
                    }
                }
                providers
            }
            .forEach { (pluginId, implementationClass) ->
                val resourceFile = AutoPluginHelpers.fileNameForPluginId(pluginId)
                log("Working on resource file: $resourceFile")
                try {
                    codeGenerator.createNewFile(packageName = "", fileName = resourceFile, extensionName = "")
                        .bufferedWriter()
                        .use { writer ->
                            writer.writeResourceFile(implementationClass)
                        }
                    log("Wrote to: $resourceFile")
                } catch (e: IOException) {
                    compileError("Unable to create $resourceFile, $e")
                }
            }
    }

    private fun compileError(error: String, node: KSNode? = null): Nothing {
        logger.error(error, node)
        throw CompilationErrorException()
    }

    private fun log(message: String) {
        if (verbose) {
            logger.logging(message)
        }
    }

    /**
     * Returns the binary name of a reference type. For example,
     * {@code com.google.Foo$Bar}, instead of {@code com.google.Foo.Bar}.
     */
    private fun KSClassDeclaration.toBinaryName(): String = toClassName().reflectionName()

    private fun KSClassDeclaration.toClassName(): ClassName {
        require(!isLocal()) { "Local/anonymous classes are not supported!" }
        val pkgName = packageName.asString()
            .takeUnless { it == "<root>" }
            ?: ""
        val typesString = qualifiedName!!.asString().removePrefix("$pkgName.")

        val simpleNames = typesString
            .split(".")
        return ClassName(pkgName, simpleNames)
    }

    override fun finish() {}
}