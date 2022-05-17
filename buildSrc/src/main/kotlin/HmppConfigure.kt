/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

private val miraiPlatform = Attribute.of(
    "net.mamoe.mirai.platform",
    String::class.java
)


fun Project.configureHMPPJvm() {
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        jvm("jvmBase") {
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common) // avoid resolving by others
//            attributes.attribute(miraiPlatform, "jvmBase")
        }

        if (isAndroidSDKAvailable) {
            jvm("android") {
                attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
                //   publishAllLibraryVariants()
            }
        } else {
            printAndroidNotInstalled()
        }

        jvm("jvm") {

        }


        val ideaActive = System.getProperty("idea.active") == "true" && System.getProperty("publication.test") != "true"

        val nativeMainSets = mutableListOf<KotlinSourceSet>()
        val nativeTestSets = mutableListOf<KotlinSourceSet>()

        if (ideaActive) {
            when {
                Os.isFamily(Os.FAMILY_MAC) -> if (Os.isArch("aarch64")) macosArm64("native") else macosX64("native")
                Os.isFamily(Os.FAMILY_WINDOWS) -> mingwX64("native")
                else -> linuxX64("native")
            }
        } else {
            // 1.6.0
            val nativeTargets: List<String> = arrayOf(
                // serialization doesn't support those commented targets
//                "androidNativeArm32, androidNativeArm64, androidNativeX86, androidNativeX64",
                "iosArm32, iosArm64, iosX64, iosSimulatorArm64",
                "watchosArm32, watchosArm64, watchosX86, watchosX64, watchosSimulatorArm64",
                "tvosArm64, tvosX64, tvosSimulatorArm64",
                "macosX64, macosArm64",
                "linuxMips32, linuxMipsel32, linuxX64",
                "mingwX64",
//                "wasm32" // linuxArm32Hfp, mingwX86
            ).flatMap { it.split(",") }.map { it.trim() }
            presets.filter { it.name in nativeTargets }
                .forEach { preset ->
                    val target = targetFromPreset(preset, preset.name)
                    nativeMainSets.add(target.compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.first())
                    nativeTestSets.add(target.compilations[TEST_COMPILATION_NAME].kotlinSourceSets.first())
                }

            if (!ideaActive) {
                configure(nativeMainSets) {
                    dependsOn(sourceSets.maybeCreate("nativeMain"))
                }

                configure(nativeTestSets) {
                    dependsOn(sourceSets.maybeCreate("nativeTest"))
                }
            }
        }


        val sourceSets = kotlinSourceSets.orEmpty()
        val commonMain = sourceSets.single { it.name == "commonMain" }
        val commonTest = sourceSets.single { it.name == "commonTest" }
        val jvmBaseMain = sourceSets.single { it.name == "jvmBaseMain" }
        val jvmBaseTest = sourceSets.single { it.name == "jvmBaseTest" }
        val jvmMain = sourceSets.single { it.name == "jvmMain" }
        val jvmTest = sourceSets.single { it.name == "jvmTest" }
        val androidMain = sourceSets.single { it.name == "androidMain" }
        val androidTest = sourceSets.single { it.name == "androidTest" }

        val nativeMain = sourceSets.single { it.name == "nativeMain" }
        val nativeTest = sourceSets.single { it.name == "nativeTest" }


        jvmBaseMain.dependsOn(commonMain)
        jvmBaseTest.dependsOn(commonTest)

        jvmMain.dependsOn(jvmBaseMain)
        androidMain.dependsOn(jvmBaseMain)

        jvmTest.dependsOn(jvmBaseTest)
        androidTest.dependsOn(commonTest)

        nativeMain.dependsOn(commonMain)
        nativeTest.dependsOn(commonTest)
    }
}