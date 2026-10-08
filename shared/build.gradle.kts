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
