plugins {
    id("kotlinx-atomicfu")
    kotlin("multiplatform")
    //id("com.android.library")
    //id("kotlin-android-extensions")
}

val kotlinVersion = rootProject.ext["kotlin_version"].toString()
val atomicFuVersion = rootProject.ext["atomicfu_version"].toString()
val coroutinesVersion = rootProject.ext["coroutines_version"].toString()
val kotlinXIoVersion = rootProject.ext["kotlinxio_version"].toString()
val coroutinesIoVersion = rootProject.ext["coroutinesio_version"].toString()

val klockVersion = rootProject.ext["klock_version"].toString()
val ktorVersion = rootProject.ext["ktor_version"].toString()
/*
//apply()
android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    defaultConfig {
        applicationId = "com.youngfeng.kotlindsl"
        minSdkVersion(15)
        targetSdkVersion(27)
        versionCode = 1
        versionName = "1.0"
        //  testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            //proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    sourceSets.forEach {
        println(it)
       // it.languageSettings.enableLanguageFeature("InlineClasses")
    }
}
*/
kotlin {
    // android("android")
    jvm("jvm")

    sourceSets["commonMain"].apply {
        dependencies {

            implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            implementation("com.soywiz.korlibs.klock:klock:$klockVersion")

            api("io.ktor:ktor-client-core:$ktorVersion")
            api("io.ktor:ktor-network:$ktorVersion")
            api("io.ktor:ktor-http:$ktorVersion")

        }
    }

    /*
    sourceSets["androidMain"].apply {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            implementation("io.ktor:ktor-http-cio:$ktorVersion")
            implementation("io.ktor:ktor-http:$ktorVersion")
            implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")

        }
        languageSettings.enableLanguageFeature("InlineClasses")
    }*/

    sourceSets["jvmMain"].apply {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

            implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            implementation("io.ktor:ktor-http-cio:$ktorVersion")
            implementation("io.ktor:ktor-http:$ktorVersion")
            implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")

        }
    }

    sourceSets.forEach {
        it.languageSettings.enableLanguageFeature("InlineClasses")

        it.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-stdlib")
            implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-io:$kotlinXIoVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-io:$coroutinesIoVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        }
    }
}
