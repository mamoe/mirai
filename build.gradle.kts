@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import org.jetbrains.dokka.gradle.DokkaTask
import java.time.Duration
import kotlin.math.pow

buildscript {
    repositories {
        mavenLocal()
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
        jcenter()
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("com.android.tools.build:gradle:${Versions.Android.androidGradlePlugin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.compiler}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.Kotlin.compiler}")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.Kotlin.atomicFU}")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:${Versions.Kotlin.binaryValidator}")
    }
}

plugins {
    id("org.jetbrains.dokka") version Versions.Kotlin.dokka apply false
    id("net.mamoe.kotlin-jvm-blocking-bridge") version Versions.blockingBridge apply false
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
        mavenLocal()
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
        jcenter()
        google()
        mavenCentral()
    }
}

subprojects {
    if (this@subprojects.name == "java-test") {
        return@subprojects
    }
    afterEvaluate {
        apply(plugin = "com.github.johnrengelman.shadow")
        val kotlin =
            runCatching {
                (this as ExtensionAware).extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
            }.getOrNull() ?: return@afterEvaluate

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
            this.manifest {
                this.attributes(
                    "Manifest-Version" to 1,
                    "Implementation-Vendor" to "Mamoe Technologies",
                    "Implementation-Title" to this@afterEvaluate.name.toString(),
                    "Implementation-Version" to this@afterEvaluate.version.toString()
                )
            }
        }

        val githubUpload by tasks.creating {
            group = "mirai"
            dependsOn(shadowJvmJar)

            doFirst {
                timeout.set(Duration.ofHours(3))
                findLatestFile().let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.GitHub.upload(
                            file,
                            project,
                            "mirai-repo",
                            "shadow/${project.name}/$filename"
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("GitHub Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }
        }

        apply(plugin = "org.jetbrains.dokka")
        this.tasks {
            val dokka by getting(DokkaTask::class) {
                outputFormat = "html"
                outputDirectory = "$buildDir/dokka"
            }
            val dokkaMarkdown by creating(DokkaTask::class) {
                outputFormat = "markdown"
                outputDirectory = "$buildDir/dokka-markdown"
            }
            val dokkaGfm by creating(DokkaTask::class) {
                outputFormat = "gfm"
                outputDirectory = "$buildDir/dokka-gfm"
            }
        }

        val dokkaGitHubUpload by tasks.creating {
            group = "mirai"

            val dokkaTaskName = "dokka"

            dependsOn(tasks.getByName(dokkaTaskName))
            doFirst {
                val baseDir = file("./build/$dokkaTaskName/${project.name}")

                timeout.set(Duration.ofHours(6))
                file("build/$dokkaTaskName/").walk()
                    .filter { it.isFile }
                    .map { old ->
                        if (old.name == "index.md") File(old.parentFile, "README.md").also { new -> old.renameTo(new) }
                        else old
                    }
                    // optimize md
                    .forEach { file ->
                        if (file.endsWith(".md")) {
                            file.writeText(
                                file.readText().replace("index.md", "README.md", ignoreCase = true)
                                    .replace(Regex("""```\n([\s\S]*?)```""")) {
                                        "\n" + """
                                    ```kotlin
                                    $it
                                    ```
                                """.trimIndent()
                                    })
                        } /* else if (file.name == "README.md") {
                            file.writeText(file.readText().replace(Regex("""(\n\n\|\s)""")) {
                                "\n\n" + """"
                                    |||
                                    |:----------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
                                    | 
                                """.trimIndent()
                            })
                        }*/
                        val filename = file.toRelativeString(baseDir)
                        println("Uploading file $filename")
                        runCatching {
                            upload.GitHub.upload(
                                file,
                                project,
                                "mirai-doc",
                                "${project.name}/${project.version}/$filename"
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
                findLatestFile().let { (_, file) ->
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

    afterEvaluate {
        tasks.filterIsInstance<DokkaTask>().forEach { task ->
            with(task) {
                configuration {
                    perPackageOption {
                        prefix = "net.mamoe.mirai"
                        skipDeprecated = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.internal"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.event.internal"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.utils.internal"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.qqandroid.utils"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.qqandroid.contact"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.qqandroid.message"
                        suppress = true
                    }
                    perPackageOption {
                        prefix = "net.mamoe.mirai.qqandroid.network"
                        suppress = true
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
        .maxBy { (version, _) ->
            version.split('.').let {
                if (it.size == 2) it + "0"
                else it
            }.reversed().foldIndexed(0) { index: Int, acc: Int, s: String ->
                acc + 100.0.pow(index).toInt() * (s.toIntOrNull() ?: 0)
            }
        } ?: error("cannot find any file to upload")
}
