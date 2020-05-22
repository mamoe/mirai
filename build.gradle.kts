@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import java.time.Duration
import kotlin.math.pow

buildscript {
    repositories {
        mavenLocal()
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
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
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:${Versions.Kotlin.binaryValidator}")
    }
}

plugins {
    id("org.jetbrains.dokka") version Versions.Kotlin.dokka apply false
    // id("com.jfrog.bintray") version Versions.Publishing.bintray apply false
}

// https://github.com/kotlin/binary-compatibility-validator
//apply(plugin = "binary-compatibility-validator")


project.ext.set("isAndroidSDKAvailable", false)

// until
// https://youtrack.jetbrains.com/issue/KT-37152,
// are fixed.

/*
runCatching {
    val keyProps = Properties().apply {
        file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    if (keyProps.getProperty("sdk.dir", "").isNotEmpty()) {
        project.ext.set("isAndroidSDKAvailable", true)
    } else {
        project.ext.set("isAndroidSDKAvailable", false)
    }
}.exceptionOrNull()?.run {
    project.ext.set("isAndroidSDKAvailable", false)
}*/

allprojects {
    group = "net.mamoe"
    version = Versions.Mirai.version

    repositories {
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
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

            val compilations =
                kotlin.targets.filter { it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm }
                    .map { it.compilations["main"] }

            compilations.forEach {
                dependsOn(it.compileKotlinTask)
            }

            compilations.forEach {
                from(it.output)
            }
            configurations = compilations.map { it.compileDependencyFiles as Configuration }

            this.exclude { file ->
                file.name.endsWith(".sf", ignoreCase = true)
                    .also { if (it) println("excluded ${file.name}") }
            }
        }

        val githubUpload by tasks.creating {
            group = "mirai"
            dependsOn(shadowJvmJar)

            doFirst {
                timeout.set(Duration.ofHours(3))
                findLatestFile()?.let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.GitHub.upload(
                            file,
                            "https://api.github.com/repos/mamoe/mirai-repo/contents/shadow/${project.name}/$filename",
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("GitHub Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }
        }

        val cuiCloudUpload by tasks.creating {
            group = "mirai"
            dependsOn(shadowJvmJar)

            doFirst {
                timeout.set(Duration.ofHours(3))
                findLatestFile()?.let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.CuiCloud.upload(
                            file,
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("CuiCloud Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }

        }
    }

}


fun Project.findLatestFile(): Map.Entry<String, File> {
    return File(projectDir, "build/libs").walk()
        .filter { it.isFile }
        .onEach { println("all files=$it") }
        .filter { it.name.matches(Regex("""${project.name}-[0-9][0-9]*(\.[0-9]*)*.*\.jar""")) }
        .onEach { println("matched file: ${it.name}") }
        .associateBy { it.nameWithoutExtension.substringAfterLast('-') }
        .onEach { println("versions: $it") }
        .maxBy { (version, file) ->
            version.split('.').let {
                if (it.size == 2) it + "0"
                else it
            }.reversed().foldIndexed(0) { index: Int, acc: Int, s: String ->
                acc + 100.0.pow(index).toInt() * (s.toIntOrNull() ?: 0)
            }
        } ?: error("cannot find any file to upload")
}
