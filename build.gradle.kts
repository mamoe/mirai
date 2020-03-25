@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import java.time.Duration
import java.util.*
import kotlin.math.pow

buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        google()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("com.android.tools.build:gradle:${Versions.Android.androidGradlePlugin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.stdlib}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.Kotlin.stdlib}")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.Kotlin.atomicFU}")
    }
}

plugins {
    id("org.jetbrains.dokka") version Versions.Kotlin.dokka apply false
    // id("com.jfrog.bintray") version Versions.Publishing.bintray apply false
}

runCatching {
    val keyProps = Properties().apply {
        file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    if (keyProps.getProperty("sdk.dir", "").isNotEmpty()) {
        project.ext.set("isAndroidSDKAvailable", true)
    } else {
        project.ext.set("isAndroidSDKAvailable", false)
    }
}

allprojects {
    group = "net.mamoe"
    version = Versions.Mirai.version

    repositories {
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        google()
    }
}

subprojects {
    afterEvaluate {
        apply(plugin = "com.github.johnrengelman.shadow")
        val kotlin =
            (this as ExtensionAware).extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
                ?: return@afterEvaluate

        val shadowJvmJar by tasks.creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
            group = "mirai"

            val compilation =
                kotlin.targets.first { it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm }.compilations["main"]

            dependsOn(compilation.compileKotlinTask)

            configurations = mutableListOf(compilation.compileDependencyFiles as Configuration)
        }

        val githubUpload by tasks.creating {
            group = "mirai"
            dependsOn(shadowJvmJar)

            doFirst {
                timeout.set(Duration.ofMinutes(10))
                File(projectDir, "build/libs").walk()
                    .filter { it.isFile }
                    .onEach { println("all files=$it") }
                    .filter { it.name.matches(Regex("""${project.name}-([0-9]|\.)*\.jar""")) }
                    .onEach { println("matched file: ${it.name}") }
                    .associateBy { it.nameWithoutExtension.substringAfterLast('-') }
                    .onEach { println("versions: $it") }
                    .maxBy {
                        it.key.split('.').foldRightIndexed(0) { index: Int, s: String, acc: Int ->
                            acc + 100.0.pow(2 - index).toInt() * (s.toIntOrNull() ?: 0)
                        }
                    }?.let { (_, file) ->
                        val filename = file.name
                        println("Uploading file $filename")
                        runCatching {
                            upload.GitHub.upload(
                                file,
                                "https://api.github.com/repositories/249670490/contents/shadow/${project.name}/$filename"
                            )
                        }.exceptionOrNull()?.let {
                            System.err.println("Upload failed")
                            it.printStackTrace()
                        }
                    }
            }

        }
    }

}