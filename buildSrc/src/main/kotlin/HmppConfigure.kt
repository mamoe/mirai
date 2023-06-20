/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import com.google.gradle.osdetector.OsDetector
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

val MIRAI_PLATFORM_ATTRIBUTE: Attribute<String> = Attribute.of(
    "net.mamoe.mirai.platform", String::class.java
)

/**
 * Flags a target as an HMPP intermediate target
 */
val MIRAI_PLATFORM_INTERMEDIATE: Attribute<Boolean> = Attribute.of(
    "net.mamoe.mirai.platform.intermediate", Boolean::class.javaObjectType
)

val IDEA_ACTIVE = System.getProperty("idea.active") == "true" && System.getProperty("publication.test") != "true"

val OS_NAME = System.getProperty("os.name").lowercase()

lateinit var osDetector: OsDetector


// aarch = arm
val OsDetector.isAarch
    get() = osDetector.arch.run {
        contains("aarch", ignoreCase = true) || contains("arm", ignoreCase = true)
    }

@Suppress("ClassName")
sealed class HostKind(
    val targetName: String
) {
    object LINUX : HostKind("linuxX64")
    object WINDOWS : HostKind("mingwX64")

    abstract class MACOS(targetName: String) : HostKind(targetName)

    object MACOS_X64 : MACOS("macosX64")
    object MACOS_ARM64 : MACOS("macosArm64")
}

val HOST_KIND by lazy {
    when {
        OS_NAME.contains("windows", true) -> HostKind.WINDOWS
        OS_NAME.contains("mac", true) -> {
            if (osDetector.isAarch) {
                HostKind.MACOS_ARM64
            } else {
                HostKind.MACOS_X64
            }
        }

        else -> HostKind.LINUX
    }
}

/// eg. "!a;!b" means to enable all targets but a or b
/// eg. "a;b;!other" means to disable all targets but a or b
val ENABLED_TARGETS by projectLazy {

    val targets = getMiraiTargetFromGradle() // enable all by default

    targets.split(';').toSet()
}

fun getMiraiTargetFromGradle() =
    System.getProperty("mirai.target")
        ?: System.getenv("mirai.target")
        ?: rootProject.getLocalProperty("projects.mirai-core.targets")
        ?: "others"

fun isTargetEnabled(name: String): Boolean {
    return when {
        name in ENABLED_TARGETS -> true // explicitly enabled
        "!$name" in ENABLED_TARGETS -> false // explicitly disabled
        "~$name" in ENABLED_TARGETS -> false // explicitly disabled

        "!other" in ENABLED_TARGETS -> false // others disabled
        "~other" in ENABLED_TARGETS -> false // others disabled
        "!others" in ENABLED_TARGETS -> false // others disabled
        "~others" in ENABLED_TARGETS -> false // others disabled
        else -> true
    }
}

fun Set<String>.filterTargets() =
    this.filter { isTargetEnabled(it) }.toSet()

const val JVM_TOOLCHAIN_VERSION = 8

val JVM_TOOLCHAIN_ENABLED by projectLazy {
    rootProject.getLocalProperty("mirai.enable.jvmtoolchain.special", true)
}

/**
 * ## Android Test 结构
 *
 * 如果[启用 Android Instrumented Test][ENABLE_ANDROID_INSTRUMENTED_TESTS], 将会配置使用 Android SDK 配置真 Android target,
 * `androidMain` 将能访问 Android SDK, 也能获得针对 Android 的 IDE 错误检查.
 */
fun Project.configureJvmTargetsHierarchical(androidNamespace: String) {
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        if (JVM_TOOLCHAIN_ENABLED) {
            jvmToolchain(JVM_TOOLCHAIN_VERSION)
        }

        val commonMain by sourceSets.getting
        val commonTest by sourceSets.getting

        if (IDEA_ACTIVE) {
            jvm("jvmBase") { // dummy target for resolution, not published
                compilations.all {
                    // magic to help IDEA
                    this.compileTaskProvider.configure {
                        enabled = false
                    }
                }
                attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common) // magic

                // avoid resolution when other modules dependsOn this project
                attributes.attribute(MIRAI_PLATFORM_ATTRIBUTE, "jvmBase")
                attributes.attribute(MIRAI_PLATFORM_INTERMEDIATE, true) // no shadow
            }
        } else {
            // if not in IDEA, no need to create intermediate targets.
        }

        val jvmBaseMain by lazy {
            sourceSets.maybeCreate("jvmBaseMain").apply {
                dependsOn(commonMain)
            }
        }
        val jvmBaseTest by lazy {
            sourceSets.maybeCreate("jvmBaseTest").apply {
                dependsOn(commonTest)
            }
        }

        if (isTargetEnabled("android")) {
            configureAndroidTarget(androidNamespace)
        }

        if (isTargetEnabled("jvm")) {
            jvm("jvm")
            val jvmMain by sourceSets.getting
            val jvmTest by sourceSets.getting
            jvmMain.dependsOn(jvmBaseMain)
            jvmTest.dependsOn(jvmBaseTest)
        }
    }
}

fun String.titlecase(): String {
    if (this.isEmpty()) return this
    val c = get(0)
    return replaceFirst(c, Character.toTitleCase(c))
}