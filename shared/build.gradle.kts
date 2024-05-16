import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "1.9.22"

    id("org.gradle.maven-publish")
    id("signing")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

kotlin {
    targetHierarchy.default()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "11.0"
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }

        val commonJvm by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                implementation("dev.icerock.moko:mvvm-livedata:0.16.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }

        val jvmMain by getting {
            dependsOn(commonJvm)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                implementation("dev.icerock.moko:mvvm-livedata:0.16.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }

//        // iOS Targets
        val iosMain by getting {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

afterEvaluate {
    mavenPublishing {
        // Define coordinates for the published artifact
        coordinates(
            groupId = "io.github.thearchitect123",
            artifactId = "titansocket",
            version = "0.1.6"
        )

        // Configure POM metadata for the published artifact
        pom {
            name.set("TitanSocket")
            description.set("A kotlin multiplatform implementation of a websocket. Supports both iOS & Android")
            inceptionYear.set("2024")
            url.set("https://github.com/TheArchitect123/TitanSocket")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            // Specify developers information
            developers {
                developer {
                    id.set("Dan Gerchcovich")
                    name.set("TheArchitect123")
                    email.set("dan.developer789@gmail.com")
                }
            }

            // Specify SCM information
            scm {
                url.set("https://github.com/TheArchitect123/TitanSocket")
            }
        }

        // Configure publishing to Maven Central
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

        // Enable GPG signing for all publications
        signAllPublications()
    }
}

android {
    namespace = "io.github.thearchitect123"
    compileSdk = libs.versions.droidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.droidMinSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
