plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting  {
            dependencies {
                // Ktor Client for sending HTTP requests (headless)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.logging)
                // Kermit (the log) for multiplatform logging
                implementation(libs.kermit)
                // JSoup for parsing HTML
                implementation(libs.jsoup)
                // Apache Commons Lang3 for some utility classes (such as SystemUtils)
                implementation(libs.apache.commons.lang)
                // kotlinx.serialization for JSON management
                implementation(libs.kotlinx.serialization)
                // kotlin-semver for checking updates
                implementation(libs.semver)
            }
        }
    }
}