plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        jvm()
    }

    sourceSets {
        val commonMain by getting  {
            val ktorClientVersion = project.properties["ktor.client.version"]
            val kermitVersion = project.properties["kermit.version"]
            val jsoupVersion = project.properties["jsoup.version"]
            val apacheCommonsVersion = project.properties["apache.commons.version"]
            val kotlinxSerializationVersion = project.properties["kotlinx.serialization.version"]

            dependencies {
                // Ktor Client for sending HTTP requests (headless)
                implementation("io.ktor:ktor-client-core:$ktorClientVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorClientVersion")
                implementation("io.ktor:ktor-client-logging:$ktorClientVersion")
                // Kermit (the log) for multiplatform logging
                implementation("co.touchlab:kermit:$kermitVersion")
                // JSoup for parsing HTML
                implementation("org.jsoup:jsoup:$jsoupVersion")
                // Apache Commons Lang3 for some utility classes (such as SystemUtils)
                implementation("org.apache.commons:commons-lang3:$apacheCommonsVersion")
                // kotlinx.serialization for JSON management
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
    }
}