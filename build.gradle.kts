@file:Suppress("UnstableApiUsage")

import kotlin.math.pow

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.Kotlin.stdlib}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.stdlib}")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4") // don"t use any other.
    }
}

allprojects {
    group = "net.mamoe"

    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        apply(plugin = "com.github.johnrengelman.shadow")
        val kotlin =
            (this as ExtensionAware).extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
                ?: return@afterEvaluate

        tasks.getByName("shadowJar") {
            doLast {
                this.outputs.files.forEach {
                    if (it.nameWithoutExtension.endsWith("-all")) {
                        val output = File(
                            it.path.substringBeforeLast(File.separator) + File.separator + it.nameWithoutExtension.substringBeforeLast(
                                "-all"
                            ) + "." + it.extension
                        )

                        println("Renaming to ${output.path}")
                        if (output.exists()) {
                            output.delete()
                        }

                        it.renameTo(output)
                    }
                }
            }
        }

        val githubUpload by tasks.creating {
            group = "mirai"
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(java.time.Duration.ofHours(3))
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
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(java.time.Duration.ofHours(3))
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


fun Project.findLatestFile(): Map.Entry<String, File>? {
    return File(projectDir, "build/libs").walk()
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
        }
}
