import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
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
                implementation(compose.uiTooling)
                implementation(compose.ui)

                implementation(libs.selenium)
                implementation(libs.web.driver.manager)
                implementation(libs.kotlinx.serialization)
                implementation(libs.kermit)
                implementation(libs.apache.commons.lang)
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
            packageName = "LitAggregator"
            packageVersion = "1.0.0"
        }
    }
}
