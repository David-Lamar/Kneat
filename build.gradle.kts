plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.6.10"
}

repositories {
    mavenCentral()
}

group = "com.david.lamar"
version = "1.0.0-SNAPSHOT"


kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(true)
            reportUndocumented.set(true)
        }
    }
}