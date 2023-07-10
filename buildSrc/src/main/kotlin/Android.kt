/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE")

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.util.*

const val PROP_MIRAI_ENABLE_ANDROID_INSTRUMENTED_TESTS = "mirai.enable.android.instrumented.tests"

/**
 * Use [usingAndroidInstrumentedTests] instead.
 */
val ENABLE_ANDROID_INSTRUMENTED_TESTS by projectLazy {
    val name = PROP_MIRAI_ENABLE_ANDROID_INSTRUMENTED_TESTS
    (System.getProperty(name)
        ?: System.getenv(name)
        ?: rootProject.getLocalProperty(name)
        ?: "true").toBooleanStrict()
}

val Project.usingAndroidInstrumentedTests
    get() = ENABLE_ANDROID_INSTRUMENTED_TESTS && isAndroidSdkAvailable

fun Project.configureAndroidTarget(androidNamespace: String) {
    if (ENABLE_ANDROID_INSTRUMENTED_TESTS && !isAndroidSdkAvailable) {
        if (!ProjectAndroidSdkAvailability.tryFixAndroidSdk(this)) {
            printAndroidNotInstalled()
        }
    }

    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        if (project.usingAndroidInstrumentedTests) {
            configureAndroidTargetWithSdk(androidNamespace)
        } else {
            configureAndroidTargetWithJvm()
        }
    }
}

private fun Project.configureAndroidTargetWithJvm() {
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        jvm("android") {
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)

            if (IDEA_ACTIVE) {
                attributes.attribute(MIRAI_PLATFORM_ATTRIBUTE, "android") // workaround for IDE bug
            }
        }

        val jvmBaseMain by sourceSets.getting
        val jvmBaseTest by sourceSets.getting

        sourceSets.getByName("androidTest").configureJvmTest("configureAndroidTargetWithJvm")
        sourceSets.getByName("androidTest").kotlin.srcDir(projectDir.resolve("src/androidUnitTest/kotlin"))
        sourceSets.getByName("androidTest").dependsOn(jvmBaseTest)

        sourceSets.getByName("androidMain").apply {
            dependencies {
                compileOnly(`android-runtime`)
            }
            dependsOn(jvmBaseMain)
        }

        tasks.all {
            if (this.name == "androidTest") {
                this as Test
                this.environment(PROP_MIRAI_ANDROID_SDK_KIND, "jdk")
            }
        }
    }
}

private const val PROP_MIRAI_ANDROID_SDK_KIND = "mirai.android.sdk.kind"

@Suppress("UnstableApiUsage")
private fun Project.configureAndroidTargetWithSdk(androidNamespace: String) {
    apply(plugin = "com.android.library")
    apply(plugin = "de.mannodermaus.android-junit5")
    extensions.getByType(LibraryExtension::class).apply {
        namespace = androidNamespace
    }
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        android {
            publishLibraryVariants("release", "debug")
        }

        val jvmBaseMain = sourceSets.maybeCreate("jvmBaseMain")
        val jvmBaseTest = sourceSets.maybeCreate("jvmBaseTest")

        val androidMain by sourceSets.getting
        androidMain.dependsOn(jvmBaseMain)

        // don't use androidTest, deprecated by Kotlin

        // this can cause problems on sync
//                for (s in arrayOf("androidDebug", "androidRelease")) {
//                    sourceSets.all { if (name in s) dependsOn(androidMain) }
//                }

//                 we should have added a "androidBaseTest" (or "androidTest") for "androidUnitTest" and "androidInstrumentedTest",
//                 but this currently cause bugs in IntelliJ (2023.2)
//                val androidBaseTest = sourceSets.maybeCreate("androidBaseTest").apply {
//                    dependsOn(jvmBaseTest)
//                }
        val androidUnitTest by sourceSets.getting {
            dependsOn(jvmBaseTest)
        }
//                for (s in arrayOf("androidUnitTestDebug", "androidUnitTestRelease")) {
//                    sourceSets.all { if (name in s) dependsOn(androidUnitTest) }
//                }
        val androidInstrumentedTest by sourceSets.getting {
            dependsOn(jvmBaseTest)
        }
//                for (s in arrayOf("androidInstrumentedTestDebug")) {
//                    sourceSets.all { if (name in s) dependsOn(androidInstrumentedTest) }
//                }

//                afterEvaluate {
////                    > androidDebug dependsOn commonMain
////                    androidInstrumentedTest dependsOn jvmBaseTest
////                    androidInstrumentedTestDebug dependsOn
////                    androidMain dependsOn commonMain, jvmBaseMain
////                    androidRelease dependsOn commonMain
////                    androidUnitTest dependsOn commonTest, jvmBaseTest
////                    androidUnitTestDebug dependsOn commonTest
////                    androidUnitTestRelease dependsOn commonTest
//                    error(this@apply.sourceSets.joinToString("\n") {
//                        it.name + " dependsOn " + it.dependsOn.joinToString { it.name }
//                    })
//                }

        configure(
            listOf(
                sourceSets.getByName("androidInstrumentedTest"),
                sourceSets.getByName("androidUnitTest"),
            )
        ) {
            dependencies { implementation(kotlin("test-annotations-common"))?.because("configureAndroidTargetWithSdk") }
        }

        tasks.all {
            if (this.name == "testDebugUnitTest" || this.name == "testReleaseUnitTest") {
                this as Test
                this.environment(PROP_MIRAI_ANDROID_SDK_KIND, "adk")
            }
        }
    }

    // trick for compiler bug
    this.sourceSets.apply {
        removeIf { it.name == "androidAndroidTestRelease" }
        removeIf { it.name == "androidTestFixtures" }
        removeIf { it.name == "androidTestFixturesDebug" }
        removeIf { it.name == "androidTestFixturesRelease" }
    }

    extensions.getByType(LibraryExtension::class.java).apply {
        compileSdk = 33
        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        defaultConfig {
            minSdk = rootProject.extra["mirai.android.target.api.level"]!!.toString().toInt()
            targetSdk = 33
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        buildTypes.getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }
    }

    extensions.getByType(LibraryExtension::class.java).apply {
        defaultConfig {
            // 1) Make sure to use the AndroidJUnitRunner, or a subclass of it. This requires a dependency on androidx.test:runner, too!
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            // 2) Connect JUnit 5 to the runner
            testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
        }
    }

//        val sourceSets = arrayOf("androidInstrumentedTest", "androidUnitTest")
//            .map { kotlin.sourceSets.getByName(it) }

//        for (sourceSet in sourceSets) {
//            sourceSet.dependencies {
//                implementation("androidx.test:runner:1.5.2")
//                implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
//                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
//
//                implementation("de.mannodermaus.junit5:android-test-core:1.3.0")
//                implementation("de.mannodermaus.junit5:android-test-runner:1.3.0")
//            }
//        }

    dependencies {
        // 4) Jupiter API & Test Runner, if you don't have it already
        "androidTestImplementation"("androidx.test:runner:1.5.2")
        "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
        "androidTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")

        // 5) The instrumentation test companion libraries
        "androidTestImplementation"("de.mannodermaus.junit5:android-test-core:1.3.0")
        "androidTestRuntimeOnly"("de.mannodermaus.junit5:android-test-runner:1.3.0")
    }
}

private fun Project.printAndroidNotInstalled() {
    logger.warn(
        """
            你设置了启用 Android Instrumented Test, 但是未配置 Android SDK. $name 的 Android 目标将会使用桌面 JVM 编译和测试. 
            Android Instrumented Test 将不会进行. 这不会影响 Android 以外的平台的编译和测试. 
            
            如果你要给 mirai PR 并且你修改了 Android 部分, 建议解决此警告. 
            如果你没有修改 Android 部分, 则可以忽略, 或者在项目根目录 local.properties (如果不存在就创建一个) 添加 `$PROP_MIRAI_ENABLE_ANDROID_INSTRUMENTED_TESTS=false`.
            
            在安装 Android SDK 后, 请在项目根目录 local.properties 中添加 `sdk.dir=/path/to/Android/sdk` 指向本机 Android SDK 安装路径.
            
            若要关闭 Android Instrumented Test, 在项目根目录 local.properties 添加 `$PROP_MIRAI_ENABLE_ANDROID_INSTRUMENTED_TESTS=false`.
            -------
            """.trimIndent()
    )
//    logger.warn(
//        """Android SDK might not be installed. Android target of $name will not be compiled. It does no influence on the compilation of other platforms.
//            """.trimIndent()
//    )
}


private object ProjectAndroidSdkAvailability {
    val map: MutableMap<String, Boolean> by projectLazy { mutableMapOf() }

    @Synchronized
    operator fun get(project: Project): Boolean {
        if (map[project.path] != null) return map[project.path]!!

        val projectAvailable = project.runCatching {
            val keyProps = Properties().apply {
                file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            }
            keyProps.getProperty("sdk.dir", "").isNotEmpty()
        }.getOrElse { false }


        fun impl(): Boolean {
            if (project === project.rootProject) return projectAvailable
            return projectAvailable || get(project.rootProject)
        }
        map[project.path] = impl()
        return map[project.path]!!
    }

    fun tryFixAndroidSdk(project: Project): Boolean {
        val androidHome = System.getenv("ANDROID_HOME") ?: kotlin.run {
            project.logger.info("tryFixAndroidSdk: environment `ANDROID_HOME` does not exist")
            return false
        }

        val escaped = androidHome
            .replace(""":""", """\:""")
            .replace("""\""", """\\""")
            .trim()

        project.rootDir.resolve("local.properties")
            .apply { if (!exists()) createNewFile() }
            .appendText("sdk.dir=$escaped")

        project.logger.info("tryFixAndroidSdk: fixed sdk.dir in local.properties: $escaped")

        map.clear()
        return get(project)
    }
}

private val Project.isAndroidSdkAvailable: Boolean get() = ProjectAndroidSdkAvailability[this]
