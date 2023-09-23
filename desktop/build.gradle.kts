import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            val seleniumVersion = project.properties["selenium.version"]
            val driverManagerVersion = project.properties["driver.manager.version"]

            dependencies {
                // Engine
                implementation(project(":engine"))
                // Jetpack Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.desktop.currentOs)
                // Selenium as the desktop backend for browser emulation
                implementation("org.seleniumhq.selenium:selenium-java:$seleniumVersion")
                // Selenium WebDriverManager that detects any of the user's installed browsers and ensures that the
                // appropriate WebDriver is downloaded and discoverable by the Selenium API
                implementation("io.github.bonigarcia:webdrivermanager:$driverManagerVersion")
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
