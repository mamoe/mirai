@file:Suppress("UNUSED_VARIABLE")

import com.android.build.gradle.api.AndroidSourceSet

plugins {
    id("kotlinx-atomicfu")
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlinx-serialization")

    `maven-publish`
}

group = "net.mamoe.mirai"
version = "0.1.0"

description = "Mirai core"

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.kotlinx(id: String, version: String) {
    implementation("org.jetbrains.kotlinx:$id:$version")
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler.ktor(id: String, version: String) {
    implementation("io.ktor:$id:$version")
}

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
                    // proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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

    /*
    val proto  = sourceSets["proto"].apply {

    }*/

    val commonMain = sourceSets["commonMain"].apply {
        dependencies {
            kotlin("kotlin-reflect", kotlinVersion)
            //kotlin("kotlin-serialization", kotlinVersion)

            kotlinx("kotlinx-coroutines-core-common", coroutinesVersion)
            kotlinx("kotlinx-serialization-runtime-common", serializationVersion)

            api("com.soywiz.korlibs.klock:klock:$klockVersion")

            ktor("ktor-http-cio", ktorVersion)
            ktor("ktor-http", ktorVersion)
            ktor("ktor-client-core-jvm", ktorVersion)
            ktor("ktor-client-cio", ktorVersion)
            ktor("ktor-client-core", ktorVersion)
            ktor("ktor-network", ktorVersion)
            //implementation("io.ktor:ktor-io:1.3.0-beta-1")
        }
    }

    sourceSets["androidMain"].apply {
        dependencies {
            dependsOn(commonMain)

            kotlin("kotlin-reflect", kotlinVersion)

            kotlinx("kotlinx-serialization-runtime", serializationVersion)
            kotlinx("kotlinx-coroutines-android", coroutinesVersion)

            ktor("ktor-client-android", ktorVersion)
        }
    }

    sourceSets["jvmMain"].apply {
        dependencies {
            dependsOn(commonMain)

            kotlin("kotlin-stdlib-jdk8", kotlinVersion)
            kotlin("kotlin-stdlib-jdk7", kotlinVersion)
            kotlin("kotlin-reflect", kotlinVersion)

            ktor("ktor-client-core-jvm", ktorVersion)
            kotlinx("kotlinx-io-jvm", kotlinXIoVersion)
            kotlinx("kotlinx-serialization-runtime", serializationVersion)
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
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")

        dependencies {
            kotlin("kotlin-stdlib", kotlinVersion)
            kotlin("kotlin-serialization", kotlinVersion)

            kotlinx("atomicfu", atomicFuVersion)
            kotlinx("kotlinx-io", kotlinXIoVersion)
            kotlinx("kotlinx-coroutines-io", coroutinesIoVersion)
            kotlinx("kotlinx-coroutines-core", coroutinesVersion)
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