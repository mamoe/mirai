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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

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
//        jvm("jvmBase") {
//            compilations.all {
//                this.compileKotlinTask.enabled = false // IDE complain
//            }
//            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common) // avoid resolving by others
////            attributes.attribute(miraiPlatform, "jvmBase")
//        }

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
        val nativeTargets = mutableListOf<KotlinNativeTarget>()

        if (ideaActive) {
            val target = when {
                Os.isFamily(Os.FAMILY_MAC) -> if (Os.isArch("aarch64")) macosArm64("native") else macosX64("native")
                Os.isFamily(Os.FAMILY_WINDOWS) -> mingwX64("native")
                else -> linuxX64("native")
            }
            nativeTargets.add(target)
        } else {
            // 1.6.0
            val nativeTargetNames: List<String> = arrayOf(
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
            presets.filter { it.name in nativeTargetNames }
                .forEach { preset ->
                    val target = targetFromPreset(preset, preset.name) as KotlinNativeTarget
                    nativeMainSets.add(target.compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.first())
                    nativeTestSets.add(target.compilations[TEST_COMPILATION_NAME].kotlinSourceSets.first())
                    nativeTargets.add(target)
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

        configureNativeInterop("main", projectDir.resolve("src/nativeMainInterop"), nativeTargets)
        configureNativeInterop("test", projectDir.resolve("src/nativeTestInterop"), nativeTargets)


        val sourceSets = kotlinSourceSets.orEmpty()
        val commonMain = sourceSets.single { it.name == "commonMain" }
        val commonTest = sourceSets.single { it.name == "commonTest" }
        val jvmBaseMain = this.sourceSets.maybeCreate("jvmBaseMain")
        val jvmBaseTest = this.sourceSets.maybeCreate("jvmBaseTest")
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
        androidTest.dependsOn(jvmBaseTest)

        nativeMain.dependsOn(commonMain)
        nativeTest.dependsOn(commonTest)
    }
}

private fun Project.configureNativeInterop(
    compilationName: String,
    nativeInteropDir: File,
    nativeTargets: MutableList<KotlinNativeTarget>
) {
    if (nativeInteropDir.exists() && nativeInteropDir.isDirectory && nativeInteropDir.resolve("build.rs").exists()) {
        val crateName = project.name.replace("-", "_")

        val headerName = "$crateName.h"
        val rustLibDir = nativeInteropDir.resolve("target/debug/")

        configure(nativeTargets) {
            compilations.getByName(compilationName).cinterops.create(compilationName) {
                val headerFile = nativeInteropDir.resolve(headerName)
                if (headerFile.exists()) headers(headerFile)
                defFile(nativeInteropDir.resolve("interop.def"))
            }

            binaries {
                sharedLib {
                    linkerOpts("-v")
                    linkerOpts("-L${rustLibDir.absolutePath.replace("\\", "/")}")
//                    linkerOpts("-lmyrust")
                    linkerOpts("-Wl,-undefined,dynamic_lookup") // resolve symbols in runtime
                    baseName = project.name
                }
            }
        }

        val cbindgen = tasks.register("cbindgen${compilationName.titlecase()}") {
            group = "mirai"
            description = "Generate C Headers from Rust"
            inputs.files(
                project.objects.fileTree().from(nativeInteropDir.resolve("src"))
                    .filterNot { it.name == "bindings.rs" }
            )
            outputs.file(nativeInteropDir.resolve(headerName))
            doLast {
                exec {
                    workingDir(nativeInteropDir)
                    commandLine(
                        "cbindgen",
                        "--config", "cbindgen.toml",
                        "--crate", crateName,
                        "--output", headerName
                    )
                }
            }
        }

        val cinteropTask = tasks.getByName("cinterop${compilationName.titlecase()}Native")
        cinteropTask.mustRunAfter(cbindgen)

        val generateRustBindings = tasks.register("generateRustBindings${compilationName.titlecase()}") {
            group = "mirai"
            description = "Generates Rust bindings for Kotlin"
            dependsOn(cbindgen)
            dependsOn(cinteropTask)
        }

        val bindgen = tasks.register("bindgen${compilationName.titlecase()}") {
            group = "mirai"
            val bindingsPath = nativeInteropDir.resolve("src/bindings.rs")
            val headerFile = buildDir.resolve("bin/native/debugShared/lib${crateName}_api.h")
            inputs.files(headerFile)
            outputs.file(bindingsPath)
            mustRunAfter(tasks.findByName("linkDebugSharedNative"))
            doLast {
                exec {
                    workingDir(nativeInteropDir)
                    // bindgen input.h -o bindings.rs
                    commandLine(
                        "bindgen",
                        headerFile,
                        "-o", bindingsPath,
                    )
                }
            }
        }

        val generateKotlinBindings = tasks.register("generateKotlinBindings${compilationName.titlecase()}") {
            group = "mirai"
            description = "Generates Kotlin bindings for Rust"
            dependsOn(bindgen)
            dependsOn(tasks.findByName("linkDebugSharedNative"))
        }

        var targetCompilation: KotlinNativeCompilation? = null
        configure(nativeTargets) {
            val compilations = compilations.filter { nativeInteropDir.name.contains(it.name, ignoreCase = true) }
            check(compilations.isNotEmpty()) { "Should be at lease one corresponding native compilation, but found 0" }
            targetCompilation = compilations.single()
//            targetCompilation!!.compileKotlinTask.dependsOn(cbindgen)
//            tasks.getByName("cinteropNative$name").dependsOn(cbindgen)
        }
        targetCompilation!!

        val compileRust = tasks.register("compileRust${compilationName.titlecase()}") {
            group = "mirai"
            inputs.files(nativeInteropDir.resolve("src"))
            outputs.file(rustLibDir.resolve("lib$crateName.dylib"))
//            dependsOn(targetCompilation!!.compileKotlinTask)
            dependsOn(bindgen)
            dependsOn(tasks.findByName("linkDebugSharedNative")) // dylib to link
            doLast {
                exec {
                    workingDir(nativeInteropDir)
                    commandLine(
                        "cargo",
                        "build",
                        "--color", "always",
                        "--all",
                        "--", "--color", "always", "2>&1"
                    )
                }
            }
        }

        tasks.getByName("assemble").dependsOn(compileRust)
    }
}

fun String.titlecase(): String {
    if (this.isEmpty()) return this
    val c = get(0)
    return replaceFirst(c, Character.toTitleCase(c))
}