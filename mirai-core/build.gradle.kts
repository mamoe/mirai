@file:Suppress("UNUSED_VARIABLE")

import com.android.build.gradle.api.AndroidSourceSet

plugins {
    id("kotlinx-atomicfu")
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
    //id("kotlin-android-extensions")
}

group = "net.mamoe.mirai"
version = "0.1.0"

description = "Mirai core"

val kotlinVersion = rootProject.ext["kotlin_version"].toString()
val atomicFuVersion = rootProject.ext["atomicfu_version"].toString()
val coroutinesVersion = rootProject.ext["coroutines_version"].toString()
val kotlinXIoVersion = rootProject.ext["kotlinxio_version"].toString()
val coroutinesIoVersion = rootProject.ext["coroutinesio_version"].toString()

val klockVersion = rootProject.ext["klock_version"].toString()
val ktorVersion = rootProject.ext["ktor_version"].toString()

kotlin {
    android("android") {
        project.plugins.apply("com.android.library")
        //publishLibraryVariants("release", "debug")
        project.android {
            compileSdkVersion(29)
            buildToolsVersion("29.0.2")
            defaultConfig {
                minSdkVersion(15)
                targetSdkVersion(29)
                versionCode = 1
                versionName = "1.0"
                //  testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    //proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
                }
            }

            sourceSets.filterIsInstance(com.android.build.gradle.api.AndroidSourceSet::class.java).forEach {
                it.manifest.srcFile("src/androidMain/res/AndroidManifest.xml")
                it.res.srcDirs(file("src/androidMain/res"))
            }

            (sourceSets["main"] as AndroidSourceSet).java.srcDirs(file("src/androidMain/kotlin"))
        }
    }
    jvm("jvm") {
        //  withJava()
    }

    val commonMain = sourceSets["commonMain"].apply {
        dependencies {
            kotlin("kotlin-reflect", kotlinVersion)
            //api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            implementation("com.soywiz.korlibs.klock:klock:$klockVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")

            implementation("io.ktor:ktor-http-cio:$ktorVersion")
            implementation("io.ktor:ktor-http:$ktorVersion")
            implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")

            //implementation("io.ktor:ktor-io:1.3.0-beta-1")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-network:$ktorVersion")
        }
    }

    sourceSets["androidMain"].apply {
        dependencies {
            dependsOn(commonMain)

            api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

            implementation("io.ktor:ktor-client-android:$ktorVersion")

        }
        languageSettings.enableLanguageFeature("InlineClasses")
    }

    sourceSets["jvmMain"].apply {
        dependencies {
            dependsOn(commonMain)

            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

            api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinXIoVersion")

        }
    }

    sourceSets["jvmTest"].apply {
        dependencies {
        }
        kotlin.outputDir = file("build/classes/kotlin/jvm/test")
        kotlin.setSrcDirs(listOf("src/$name/kotlin"))

    }

    sourceSets.all {
        languageSettings.enableLanguageFeature("InlineClasses")

        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib")
            implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinXIoVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-io:$coroutinesIoVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        }
    }
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
    }
}

/*
publishing {
    publications.withType<MavenPublication>().apply {
        val jvm by getting {}
        val metadata by getting { }
    }
}*/

/*
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mamoe/mirai")
            credentials {

                val local = loadProperties("local.properties")
                username = local["miraiCorePublicationUsername"]?.toString()?:error("Cannot find miraiCorePublicationUsername")
                password = local["miraiCorePublicationKey"].toString()?:error("Cannot find miraiCorePublicationKey")
            }
        }
    }
}


fun loadProperties(filename: String): Properties = Properties().apply { load(DataInputStream(rootProject.file(filename).inputStream())) }
*/