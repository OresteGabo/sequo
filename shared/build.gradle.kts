import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import javax.xml.parsers.DocumentBuilderFactory

abstract class CheckLocalizedStringsTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stringFiles: ConfigurableFileCollection

    @TaskAction
    fun checkStrings() {
        val filesByName = stringFiles.files.associateBy { it.nameWithoutExtension to it.parentFile.name }
        val defaultFile = filesByName["strings" to "values"]
            ?: error("Missing default strings.xml")
        val frenchFile = filesByName["strings" to "values-fr"]
            ?: error("Missing French strings.xml")

        fun stringNames(file: java.io.File): Set<String> {
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)
            val nodes = document.getElementsByTagName("string")
            return buildSet {
                for (index in 0 until nodes.length) {
                    val name = nodes.item(index).attributes.getNamedItem("name")?.nodeValue
                    if (!name.isNullOrBlank()) add(name)
                }
            }
        }

        val english = stringNames(defaultFile)
        val french = stringNames(frenchFile)
        val missingFrench = english - french
        val extraFrench = french - english

        check(missingFrench.isEmpty() && extraFrench.isEmpty()) {
            buildString {
                appendLine("Localized string XML files are out of sync.")
                if (missingFrench.isNotEmpty()) appendLine("Missing in values-fr: ${missingFrench.sorted().joinToString()}")
                if (extraFrench.isNotEmpty()) appendLine("Only in values-fr: ${extraFrench.sorted().joinToString()}")
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}


// All clients in this framework share one explicit build target. Defaults preserve normal builds.
val sequoOverrides = listOf("sequoApiBaseUrl", "sequoReleaseId", "sequoEnvironment")
    .associateWith { providers.gradleProperty(it).orNull }
require(sequoOverrides.values.all { it == null } || sequoOverrides.values.all { !it.isNullOrBlank() }) {
    "Supply sequoApiBaseUrl, sequoReleaseId and sequoEnvironment together."
}
val sequoApiBaseUrl = sequoOverrides["sequoApiBaseUrl"]?.trimEnd('/') ?: "https://api.sequoservice.com"
val sequoReleaseId = sequoOverrides["sequoReleaseId"] ?: "development"
val sequoEnvironment = sequoOverrides["sequoEnvironment"] ?: "PRODUCTION"
require(sequoEnvironment in setOf("PRODUCTION", "STAGING", "LOCAL_FIXTURE")) { "Unsupported sequoEnvironment." }
require(sequoReleaseId.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,79}"))) { "Invalid sequoReleaseId." }
val sequoTarget = runCatching { URI(sequoApiBaseUrl) }.getOrElse { error("Invalid sequoApiBaseUrl.") }
require(sequoTarget.host != null && sequoTarget.rawUserInfo == null && sequoTarget.rawQuery == null &&
    sequoTarget.rawFragment == null && sequoTarget.rawPath.orEmpty().isEmpty() &&
    (sequoTarget.port == -1 || sequoTarget.port in 1..65535)) { "API target must be an origin without credentials, path, query or fragment." }
val sequoLocalHost = sequoTarget.host.lowercase() in setOf("localhost", "127.0.0.1", "10.0.2.2", "[::1]", "::1")
require(if (sequoEnvironment == "LOCAL_FIXTURE") sequoLocalHost && sequoTarget.scheme in setOf("http", "https")
    else sequoTarget.scheme == "https" && !sequoLocalHost) { "LOCAL_FIXTURE requires a loopback/emulator target; other environments require nonlocal HTTPS." }
val generateSequoBuildConfig = tasks.register("generateSequoBuildConfig") {
    val apiBaseUrl = sequoApiBaseUrl
    val releaseId = sequoReleaseId
    val environment = sequoEnvironment
    val output = layout.buildDirectory.dir("generated/sequoBuildConfig/commonMain")
    inputs.property("apiBaseUrl", sequoApiBaseUrl)
    inputs.property("releaseId", sequoReleaseId)
    inputs.property("environment", sequoEnvironment)
    outputs.dir(output)
    doLast {
        val target = output.get().file("dev/orestegabo/sequo/core/network/GeneratedSequoBuildConfig.kt").asFile
        target.parentFile.mkdirs()
        target.writeText("""
            package dev.orestegabo.sequo.core.network

            internal object GeneratedSequoBuildConfig {
                const val ApiBaseUrl = "$apiBaseUrl"
                const val ReleaseId = "$releaseId"
                const val Environment = "$environment"
            }
        """.trimIndent() + "\n")
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "dev.orestegabo.sequo.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.security.crypto)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val checkLocalizedStrings by tasks.registering(CheckLocalizedStringsTask::class) {
    val defaultStrings = layout.projectDirectory.file("src/commonMain/composeResources/values/strings.xml")
    val frenchStrings = layout.projectDirectory.file("src/commonMain/composeResources/values-fr/strings.xml")
    stringFiles.from(defaultStrings, frenchStrings)
}

tasks.matching { it.name.startsWith("generateResourceAccessorsFor") || it.name == "generateComposeResClass" }
    .configureEach {
        dependsOn(checkLocalizedStrings)
    }

// A task-backed source directory keeps Android and both iOS frameworks on the same target.
kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generateSequoBuildConfig)
