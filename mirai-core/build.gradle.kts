import com.android.build.gradle.api.AndroidSourceSet

plugins {
    id("kotlinx-atomicfu")
    kotlin("multiplatform")
    id("com.android.library")
    //id("kotlin-android-extensions")
}

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
    jvm("jvm")

    val commonMain = sourceSets["commonMain"].apply {
        dependencies {
            api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            implementation("com.soywiz.korlibs.klock:klock:$klockVersion")

            implementation("io.ktor:ktor-http-cio:$ktorVersion")
            implementation("io.ktor:ktor-http:$ktorVersion")
            implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")

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
}
