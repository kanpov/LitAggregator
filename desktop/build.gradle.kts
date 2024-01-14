import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
    maven("https://jogamp.org/deployment/maven")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":engine"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.desktop.currentOs)
                implementation(libs.voyager.navigation)
                implementation(libs.voyager.transitions)
                implementation(libs.mpfilepicker)
                implementation(libs.compose.chart)

                implementation(libs.selenium)
                implementation(libs.web.driver.manager)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kermit)
                implementation(libs.apache.commons.lang)
                implementation(libs.apache.commons.text)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.kanpov.litaggregator.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage, // linux
                TargetFormat.Dmg, // mac os
                TargetFormat.Exe, TargetFormat.Msi // windows 10+
            )
            modules("java.compiler", "java.instrument" , "java.sql", "jdk.unsupported")
            packageName = "LitAggregator"
            packageVersion = "1.0.0"
            buildTypes.release.proguard {
                isEnabled = false
            }
        }
    }
}
