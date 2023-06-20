/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.deps.test

import org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenFileLocations
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import java.io.File

// Copied from mirai-console-gradle
abstract class AbstractTest {
    companion object {
        const val miraiLocalVersion = "2.99.0-deps-test" // do Search Everywhere before changing this
        const val REASON_LOCAL_ARTIFACT_NOT_AVAILABLE = "local artifacts not available"
        private const val MIRAI_DEPS_TEST_MUST_RUN = "mirai.deps.test.must.run" // used by GitHub Actions scripts

        val mavenLocalDir: File by lazy {
            org.gradle.api.internal.artifacts.mvnsettings.DefaultLocalMavenRepositoryLocator(
                org.gradle.api.internal.artifacts.mvnsettings.DefaultMavenSettingsProvider(DefaultMavenFileLocations())
            ).localMavenRepository
        }

        @JvmStatic
        fun isMiraiLocalAvailable(): Boolean {
            val commandLine =
                """./gradlew publishMiraiArtifactsToMavenLocal -Dmirai.build.project.version=$miraiLocalVersion"""

            return if (mavenLocalDir.resolve("net/mamoe/mirai-core/$miraiLocalVersion").exists()) {
                println(
                    """
                [mirai-deps-test] Found local artifacts `$miraiLocalVersion`! 
                Please note that you may need to manually update local artifacts if you have:
                - added/removed a dependency for mirai-core series modules
                - changed version of any of the dependencies for mirai-core series modules
                
                You can update by running the following command: 
                $commandLine
            """.trimIndent()
                )
                true
            } else {
                val message = """
                [mirai-deps-test] ERROR: Test is not run, because there are no local artifacts available for dependencies testing. 
                Please build and publish local artifacts with version `$miraiLocalVersion` before running this test(:mirai-deps-test:test).
                This could have be automated but it will take a huge amount of time for your routine testing.
                 
                You can run this test manually if you have:
                - added/removed a dependency for mirai-core series modules
                - changed version of any of the dependencies for mirai-core series modules
                
                Note that you can ignore this test if you did not change project (dependency) structure.
                And you don't need to worry if you does not run this test — this test is always executed on the CI when you make a PR.

                You can run the following command to publish local artifacts:
                $commandLine
                
                Then you can run this test again. (By your original way or ./gradlew :mirai-deps-test:test)
                """.trimIndent()
                System.err.println(
                    message
                )

                if (System.getProperty(MIRAI_DEPS_TEST_MUST_RUN, "false").toBoolean()) {
                    throw AssertionError("System property `mirai.deps.test.must.run` is `true`, which requires the deps test to be run. \n\n$message")
                } else {
                    false
                }
            }
        }
    }

    @JvmField
    @TempDir
    var tempDirField: File? = null

    val tempDir: File get() = tempDirField!!

    val kotlinVersion = BuildConfig.kotlinVersion

    lateinit var mainSrcDir: File
    lateinit var commonMainSrcDir: File
    lateinit var testDir: File
    lateinit var buildFile: File
    lateinit var settingsFile: File
    lateinit var propertiesFile: File

    private inline fun <reified T> Any?.cast(): T = this as T

    @OptIn(ExperimentalStdlibApi::class)
    fun runGradle(vararg arguments: String) {
        System.gc()
        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withGradleVersion("7.2")
            .forwardOutput()
            .withEnvironment(System.getenv())
            .cast<DefaultGradleRunner>().withJvmArguments(buildList {
                add("-Xmx512m") // Kotlin MPP may need memory to build
                add("-Dfile.encoding=UTF-8")
            })
            .withArguments(buildList {
                addAll(arguments)
                add("-P")
                add("kotlin.compiler.execution.strategy=in-process")
                add("-D")
                add("org.gradle.jvmargs=-Xmx512m")
                add("-D")
                add("file.encoding=UTF-8")
//                add("--stacktrace")
                add("--info")
            })
            .build()
    }

    @BeforeEach
    fun setup() {
        println("Temp path is " + tempDir.absolutePath)

        settingsFile = File(tempDir, "settings.gradle")
        settingsFile.delete()
        settingsFile.writeText(
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    mavenLocal()
                }
            }
        """
        )

        File(tempDir, "gradle.properties").apply {
            delete()
            writeText(
                """
                org.gradle.daemon=false
                org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
            """.trimIndent()
            )
        }
        mainSrcDir = tempDir.resolve("src/main/kotlin").apply { mkdirs() }
        commonMainSrcDir = tempDir.resolve("src/commonMain/kotlin").apply { mkdirs() }
        testDir = tempDir.resolve("src/test/kotlin").apply { mkdirs() }

        buildFile = tempDir.resolve("build.gradle.kts")
        buildFile.writeText(
            """
            import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
            plugins {
                kotlin("jvm") version "1.7.0"
            }
            group = "org.example"
            version = "1.0-SNAPSHOT"
            repositories {
                mavenCentral()
                mavenLocal()
            }
            dependencies {
                testImplementation(kotlin("test"))
                implementation(kotlin("reflect"))
                testImplementation(kotlin("test-junit5"))
                testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
            }
            tasks.test {
                useJUnitPlatform()
            }
            tasks.withType<KotlinCompile> {
                kotlinOptions.jvmTarget = "1.8"
            }
        """.trimIndent() + "\n\n"
        )
    }

    @JvmField
    @RegisterExtension
    internal val after: AfterEachCallback = AfterEachCallback { context ->
        if (context.executionException.isPresent) {
            val inst = context.requiredTestInstance as AbstractTest
            println("====================== build.gradle ===========================")
            println(inst.tempDir.resolveFirstExisting("build.gradle", "build.gradle.kts").readTextIfFound())
            println("==================== settings.gradle ==========================")
            println(inst.tempDir.resolveFirstExisting("settings.gradle", "settings.gradle.kts").readTextIfFound())
        }
    }

    private fun File.resolveFirstExisting(vararg files: String): File? {
        return files.asSequence().map { resolve(it) }.firstOrNull { it.exists() }
    }

    private fun File?.readTextIfFound(): String =
        when {
            this == null -> "(not found)"
            exists() -> readText()
            else -> "($name not found)"
        }
}