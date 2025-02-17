import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform") version "1.5.10"
    id("org.jetbrains.compose") version "0.0.0-web-dev-14"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.web.widgets)
                implementation(compose.runtime)
            }
        }
    }
}
