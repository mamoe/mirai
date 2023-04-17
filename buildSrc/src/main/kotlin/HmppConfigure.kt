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
import com.google.gradle.osdetector.OsDetector
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import java.util.*

val MIRAI_PLATFORM_ATTRIBUTE = Attribute.of(
    "net.mamoe.mirai.platform", String::class.java
)

/**
 * Flags a target as an HMPP intermediate target
 */
val MIRAI_PLATFORM_INTERMEDIATE = Attribute.of(
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

enum class HostArch {
    X86, X64, ARM32, ARM64
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
    val isNative = name in POSSIBLE_NATIVE_TARGETS
    return when {
        name in ENABLED_TARGETS -> true // explicitly enabled
        "!$name" in ENABLED_TARGETS -> false // explicitly disabled
        "~$name" in ENABLED_TARGETS -> false // explicitly disabled

        "native" in ENABLED_TARGETS && isNative -> true // native targets explicitly enabled
        "!native" in ENABLED_TARGETS && isNative -> false // native targets explicitly disabled
        "~native" in ENABLED_TARGETS && isNative -> false // native targets explicitly disabled

        "!other" in ENABLED_TARGETS -> false // others disabled
        "~other" in ENABLED_TARGETS -> false // others disabled
        "!others" in ENABLED_TARGETS -> false // others disabled
        "~others" in ENABLED_TARGETS -> false // others disabled
        else -> true
    }
}

fun Set<String>.filterTargets() =
    this.filter { isTargetEnabled(it) }.toSet()

val MAC_TARGETS: Set<String> by projectLazy {
    setOf(
//        "watchosX86",
        "macosX64",
        "macosArm64",

        // Failed to generate cinterop for :mirai-core:cinteropOpenSSLIosX64: Process 'command '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java'' finished with non-zero exit value 1
        // Exception in thread "main" java.lang.Error: /var/folders/kw/ndw272ns06s7cys2mcwwlb5c0000gn/T/1181140105365175708.c:1:10: fatal error: 'openssl/ec.h' file not found
        //
        // Note: the openssl/ec.h is actually there, seems Kotlin doesn't support that

//        "iosX64",
//        "iosArm64",
//        "iosArm32",
//        "iosSimulatorArm64",
//        "watchosX64",
//        "watchosArm32",
//        "watchosArm64",
//        "watchosSimulatorArm64",
//        "tvosX64",
//        "tvosArm64",
//        "tvosSimulatorArm64",
    ).filterTargets()
}

val WIN_TARGETS by projectLazy { setOf("mingwX64").filterTargets() }

val LINUX_TARGETS by projectLazy { setOf("linuxX64").filterTargets() }

val UNIX_LIKE_TARGETS by projectLazy { LINUX_TARGETS + MAC_TARGETS }

val NATIVE_TARGETS by projectLazy { UNIX_LIKE_TARGETS + WIN_TARGETS }

private val POSSIBLE_NATIVE_TARGETS by lazy { setOf("mingwX64", "macosX64", "macosArm64", "linuxX64") }

fun Project.configureJvmTargetsHierarchical() {
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
        val commonMain by sourceSets.getting
        val commonTest by sourceSets.getting

        if (IDEA_ACTIVE) {
            jvm("jvmBase") { // dummy target for resolution, not published
                compilations.all {
                    this.compileTaskProvider.configure { // IDE complain
                        enabled = false
                    }
                }
                attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common) // magic
                attributes.attribute(MIRAI_PLATFORM_ATTRIBUTE, "jvmBase") // avoid resolution
                attributes.attribute(MIRAI_PLATFORM_INTERMEDIATE, true)
            }
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
            if (isAndroidSDKAvailable) {
//                apply(plugin = "com.android.library")
                android {
                    if (IDEA_ACTIVE) {
                        attributes.attribute(MIRAI_PLATFORM_ATTRIBUTE, "android") // avoid resolution
                    }
                }
                configureAndroidTarget()
                val androidMain by sourceSets.getting
                for (s in arrayOf("androidMain")) {
                    sourceSets.all { if (name in s) dependsOn(jvmBaseMain) }
                }
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
            } else {
                printAndroidNotInstalled()
            }
        }

        if (isTargetEnabled("jvm")) {
            jvm("jvm") {

            }
            val jvmMain by sourceSets.getting
            val jvmTest by sourceSets.getting
            jvmMain.dependsOn(jvmBaseMain)
            jvmTest.dependsOn(jvmBaseTest)
        }
    }
}

@Suppress("UnstableApiUsage")
fun Project.configureAndroidTarget() {
    extensions.getByType(KotlinMultiplatformExtension::class.java).apply {

        // trick
        this.sourceSets.apply {
            removeIf { it.name == "androidAndroidTestRelease" }
            removeIf { it.name == "androidTestFixtures" }
            removeIf { it.name == "androidTestFixturesDebug" }
            removeIf { it.name == "androidTestFixturesRelease" }
        }
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
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }
    }

//    if (USE_JUNIT5_FOR_ANDROID_TEST) {
//        extensions.getByType(LibraryExtension::class.java).apply {
//            defaultConfig {
//                // 1) Make sure to use the AndroidJUnitRunner, or a subclass of it. This requires a dependency on androidx.test:runner, too!
//                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//                // 2) Connect JUnit 5 to the runner
//                testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
//            }
//        }
//
//        dependencies {
//            // 4) Jupiter API & Test Runner, if you don't have it already
//            "androidTestImplementation"("androidx.test:runner:1.5.2")
//            "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api:5.9.2")
    // runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
//
//            // 5) The instrumentation test companion libraries
//            "androidTestImplementation"("de.mannodermaus.junit5:android-test-core:1.3.0")
//            "androidTestRuntimeOnly"("de.mannodermaus.junit5:android-test-runner:1.3.0")
//        }
//    }

}

private const val USE_JUNIT5_FOR_ANDROID_TEST = true


/**
 * ```
 *                      common
 *                        |
 *        /---------------+---------------\
 *     jvmBase                          native
 *     /    \                          /     \
 *   jvm   android                  unix      \
 *                                 /    \     mingwX64
 *                                /      \
 *                            darwin     linuxX64
 *                               |
 *                               *
 *                        <darwin targets>
 * ```
 *
 * `<darwin targets>`: macosX64, macosArm64, tvosX64, iosArm64, iosArm32...
 */
fun KotlinMultiplatformExtension.configureNativeTargetsHierarchical(
    project: Project
) {
    if (NATIVE_TARGETS.isEmpty()) return

    val nativeMainSets = mutableListOf<KotlinSourceSet>()
    val nativeTestSets = mutableListOf<KotlinSourceSet>()
    val nativeTargets =
        mutableListOf<KotlinTarget>() // actually KotlinNativeTarget, but KotlinNativeTarget is an internal API (complained by IDEA)


    fun KotlinMultiplatformExtension.addNativeTarget(
        preset: KotlinTargetPreset<*>,
    ): KotlinTarget {
        val target = targetFromPreset(preset, preset.name)
        nativeMainSets.add(target.compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.first())
        nativeTestSets.add(target.compilations[TEST_COMPILATION_NAME].kotlinSourceSets.first())
        nativeTargets.add(target)
        return target
    }


    // project.configureNativeInterop("main", project.projectDir.resolve("src/nativeMainInterop"), nativeTargets)
    // project.configureNativeInterop("test", project.projectDir.resolve("src/nativeTestInterop"), nativeTargets)


    val sourceSets = project.objects.domainObjectContainer(KotlinSourceSet::class.java)
        .apply { addAll(project.kotlinSourceSets.orEmpty()) }

    val commonMain by sourceSets.getting
    val commonTest by sourceSets.getting

    val nativeMain by lazy {
        this.sourceSets.maybeCreate("nativeMain").apply {
            dependsOn(commonMain)
        }
    }
    val nativeTest by lazy {
        this.sourceSets.maybeCreate("nativeTest").apply {
            dependsOn(commonTest)
        }
    }

    if (UNIX_LIKE_TARGETS.isNotEmpty()) {
        val unixMain by lazy {
            this.sourceSets.maybeCreate("unixMain").apply {
                dependsOn(nativeMain)
            }
        }
        val unixTest by lazy {
            this.sourceSets.maybeCreate("unixTest").apply {
                dependsOn(nativeTest)
            }
        }

        val darwinMain by lazy {
            this.sourceSets.maybeCreate("darwinMain").apply {
                dependsOn(unixMain)
            }
        }
        val darwinTest by lazy {
            this.sourceSets.maybeCreate("darwinTest").apply {
                dependsOn(unixTest)
            }
        }

        presets.filter { it.name in MAC_TARGETS }.forEach { preset ->
            addNativeTarget(preset).run {
                compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(darwinMain) }
                compilations[TEST_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(darwinTest) }
            }
        }

        presets.filter { it.name in LINUX_TARGETS }.forEach { preset ->
            addNativeTarget(preset).run {
                compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(unixMain) }
                compilations[TEST_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(unixTest) }
            }
        }
    }

    presets.filter { it.name in WIN_TARGETS }.forEach { preset ->
        addNativeTarget(preset).run {
            compilations[MAIN_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(nativeMain) }
            compilations[TEST_COMPILATION_NAME].kotlinSourceSets.forEach { it.dependsOn(nativeTest) }
        }
    }

    // Workaround from https://youtrack.jetbrains.com/issue/KT-52433/KotlinNative-Unable-to-generate-framework-with-Kotlin-1621-and-Xcode-134#focus=Comments-27-6140143.0-0
    project.tasks.withType<KotlinNativeLink>().configureEach {
        val properties = listOf(
            "ios_arm32", "watchos_arm32", "watchos_x86"
        ).joinToString(separator = ";") { "clangDebugFlags.$it=-Os" }
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xoverride-konan-properties=$properties"
        )
    }

//    WIN_TARGETS.forEach { targetName ->
//        val target = targets.getByName(targetName) as KotlinNativeTarget
//        if (!IDEA_ACTIVE && HOST_KIND == HostKind.WINDOWS) {
//            // add release test to run on CI
//            project.afterEvaluate {
//                target.findOrCreateTest(NativeBuildType.RELEASE) {
//                    // use linkReleaseTestMingwX64 for mingwX64Test to save memory
//                    tasks.getByName("mingwX64Test", KotlinNativeTest::class)
//                        .executable(linkTask) { linkTask.binary.outputFile }
//                }
//            }
//        }
//    }

    // Register platform tasks, e.g. linkDebugSharedHost
    project.afterEvaluate {
        val targetName = HOST_KIND.targetName
        val targetNameCapitalized = targetName.capitalize()
        project.tasks.run {
            listOf(
                "compileKotlin",
                "linkDebugTest",
                "linkReleaseTest",
                "linkDebugShared",
                "linkReleaseShared",
                "linkDebugStatic",
                "linkReleaseStatic",
            ).forEach { name ->
                findByName("$name$targetNameCapitalized")?.let { plat ->
                    register("${name}Host") {
                        group = "mirai"
                        description = "Run ${plat.name} which can be run on the current Host."
                        dependsOn(plat)
                    }
                }
            }

            findByName("${targetName}Test")?.let { plat ->
                register("hostTest") {
                    group = "mirai"
                    description = "Run ${plat.name} which can be run on the current Host."
                    dependsOn(plat)
                }
            }
        }
        project.disableCrossCompile() // improve speed
    }
}

fun KotlinMultiplatformExtension.configureNativeTargetBinaries(project: Project) {
    if (!System.getProperty("mirai.native.binaries", "false").toBoolean()) {
        // Must enable KotlinNativeLink by argument "mirai.native.link".

        // :mirai-core:linkReleaseSharedMacosX64
        return
//        disableNativeLinkTasks(project)
    }

    NATIVE_TARGETS.forEach { targetName ->
        val target = targets.getByName(targetName) as KotlinNativeTarget
        target.binaries {
            sharedLib(listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) {
                baseName = project.name.lowercase(Locale.ROOT).replace("-", "")
            }
            staticLib(listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) {
                baseName = project.name.lowercase(Locale.ROOT).replace("-", "")
            }
        }
    }
}

private fun disableNativeLinkTasks(project: Project) {
    project.tasks.withType<KotlinNativeLink>()
        .filter { it.binary.outputKind != NativeOutputKind.TEST }
        .forEach {
            it.enabled = false
            project.logger.warn("Disabling KotlinNativeLink: ${it.path}")
        }
}

private fun KotlinNativeTarget.findOrCreateTest(buildType: NativeBuildType, configure: TestExecutable.() -> Unit) =
    binaries.findTest(buildType)?.apply(configure) ?: binaries.test(listOf(buildType), configure)


// e.g. Linker will try to link curl for mingwX64 but this can't be done on macOS.
fun Project.disableCrossCompile() {
    project.afterEvaluate {
        if (HOST_KIND != HostKind.MACOS_ARM64) {
            disableTargetLink(this, HostKind.MACOS_ARM64.targetName)
        }
        if (HOST_KIND != HostKind.MACOS_X64) {
            disableTargetLink(this, HostKind.MACOS_X64.targetName)
        }
        if (HOST_KIND != HostKind.WINDOWS) {
            WIN_TARGETS.forEach { target -> disableTargetLink(this, target) }
        }
        if (HOST_KIND != HostKind.LINUX) {
            LINUX_TARGETS.forEach { target -> disableTargetLink(this, target) }
        }
    }
}

private fun disableTargetLink(project: Project, target: String) {
    // Don't disable compileKotlin tasks. These tasks ensure code syntax is correct.
    project.tasks.findByName("linkDebugTest${target.titlecase()}")?.enabled = false
    project.tasks.findByName("linkReleaseTest${target.titlecase()}")?.enabled = false
    project.tasks.findByName("linkDebugShared${target.titlecase()}")?.enabled = false
    project.tasks.findByName("linkReleaseShared${target.titlecase()}")?.enabled = false
    project.tasks.findByName("linkDebugStatic${target.titlecase()}")?.enabled = false
    project.tasks.findByName("linkReleaseStatic${target.titlecase()}")?.enabled = false
    project.tasks.findByName("${target}Test")?.enabled = false
}

private fun Project.linkerDirs(): List<String> {
    return listOf(
        ":mirai-core",
        ":mirai-core-api",
        ":mirai-core-utils",
    ).map {
        rootProject.project(it).projectDir.resolve("src/nativeMainInterop/target/debug/").absolutePath
    }
}

private fun Project.includeDirs(): List<String> {
    return listOf(
        ":mirai-core",
        ":mirai-core-api",
        ":mirai-core-utils",
    ).map {
        rootProject.project(it).projectDir.resolve("src/nativeMainInterop/").absolutePath
    }
}

private fun Project.configureNativeInterop(
    compilationName: String, nativeInteropDir: File, nativeTargets: MutableList<KotlinNativeTarget>
) {
    val crateName = project.name.replace("-", "_") + "_i"

    if (nativeInteropDir.exists() && nativeInteropDir.isDirectory && nativeInteropDir.resolve("build.rs").exists()) {
        val kotlinDylibName = project.name.replace("-", "_") + "_i"

        val headerName = "$crateName.h"
        val rustLibDir = nativeInteropDir.resolve("target/debug/")

        var interopTaskName = ""

        configure(nativeTargets) {
            interopTaskName = compilations.getByName(compilationName).cinterops.create(compilationName) {
                defFile(nativeInteropDir.resolve("interop.def"))
                val headerFile = nativeInteropDir.resolve(headerName)
                if (headerFile.exists()) headers(headerFile)
            }.interopProcessingTaskName

            binaries {
                sharedLib {
                    linkerOpts("-v")
                    linkerOpts("-L${rustLibDir.absolutePath.replace("\\", "/")}")
//                    linkerOpts("-lmirai_core_utils_i")
                    linkerOpts("-undefined", "dynamic_lookup")
                    baseName = project.name
                }
                getTest(NativeBuildType.DEBUG).apply {
                    linkerOpts("-v")
                    linkerOpts("-L${rustLibDir.absolutePath.replace("\\", "/")}")
                    linkerOpts("-lmirai_core_utils_i")
//                    linkerOpts("-undefined", "dynamic_lookup")
                }
            }
        }

        val cbindgen = tasks.register("cbindgen${compilationName.titlecase()}") {
            group = "mirai"
            description = "Generate C Headers from Rust"
            inputs.files(project.objects.fileTree().from(nativeInteropDir.resolve("src"))
                .filterNot { it.name == "bindings.rs" })
            outputs.file(nativeInteropDir.resolve(headerName))
            doLast {
                exec {
                    workingDir(nativeInteropDir)
                    commandLine(
                        "cbindgen", "--config", "cbindgen.toml", "--crate", crateName, "--output", headerName
                    )
                }
            }
        }

        val generateRustBindings = tasks.register("generateRustBindings${compilationName.titlecase()}") {
            group = "mirai"
            description = "Generates Rust bindings for Kotlin"
            dependsOn(cbindgen)
        }

        afterEvaluate {
            val cinteropTask = tasks.getByName(interopTaskName)
            cinteropTask.mustRunAfter(cbindgen)
            generateRustBindings.get().dependsOn(cinteropTask)
        }

        val bindgen = tasks.register("bindgen${compilationName.titlecase()}") {
            group = "mirai"
            val bindingsPath = nativeInteropDir.resolve("src/bindings.rs")
            val headerFile = buildDir.resolve("bin/native/debugShared/lib${kotlinDylibName}_api.h")
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

        tasks.register("generateKotlinBindings${compilationName.titlecase()}") {
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

        if (targetCompilation != null) {
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
//                        "--", "--color", "always", "2>&1"
                        )
                    }
                }
            }

            tasks.getByName("assemble").dependsOn(compileRust)
        }
    }
}

private fun Project.configureNativeLinkOptions(nativeTargets: MutableList<KotlinNativeTarget>) {
    configure(nativeTargets) {
        binaries {
            for (buildType in NativeBuildType.values()) {
                findTest(buildType)?.apply {
                    linkerOpts("-v")
                    linkerOpts(*linkerDirs().map { "-L$it" }.toTypedArray())
                    linkerOpts("-undefined", "dynamic_lookup") // resolve symbol in runtime
                }
            }
        }
    }
}

fun String.titlecase(): String {
    if (this.isEmpty()) return this
    val c = get(0)
    return replaceFirst(c, Character.toTitleCase(c))
}