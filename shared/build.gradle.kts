import java.util.Base64

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "1.9.22"

    id("org.gradle.maven-publish")
    id("signing")
}

group = "com.architect.titansocket"
version = libs.versions.titanSocketIoVersion.get()

kotlin {
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

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "shared"
            isStatic = true
        }

    }

    sourceSets {
        val commonMain by getting
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("io.socket:socket.io-client:2.1.0") {
                    // excluding org.json which is provided by Android
                    exclude("org.json", "json")
                }
            }
        }

//        val iosMain by getting {
//            dependsOn(commonMain)
//        }
//        val iosSimulatorArm64Main by getting {
//            dependsOn(iosMain)
//        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name = "TitanSocket"
                description =
                    "A kotlin multiplatform implementation of a websocket. Supports both iOS & Android"
                url = "https://github.com/TheArchitect123/TitanSocket"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/TheArchitect123/TitanSocket/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "danGerchcovich"
                        name = "Dan Gerchcovich"
                        email = "dan.developer789@gmail.com"
                    }
                }
                scm {
                    connection.set("scm:git:ssh://github.com/TheArchitect123/TitanSocket.git")
                    developerConnection.set("scm:git:ssh://github.com/TheArchitect123/TitanSocket.git")
                    url.set("https://github.com/TheArchitect123/TitanSocket.git")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
    signing {
        val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
        val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
        val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
            String(Base64.getDecoder().decode(base64Key))
        }
        if (signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}

android {
    namespace = "com.architect.titansocket"
    compileSdk = libs.versions.droidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.droidMinSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
