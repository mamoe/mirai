import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import upload.Bintray
import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("java")
    `maven-publish`
    id("com.jfrog.bintray") version Versions.bintray
}

version = Versions.console
description = "Console backend for mirai"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

kotlin {
    sourceSets.all {
        target.compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=enable"
                jvmTarget = "1.8"
            }
        }
        languageSettings.apply {
            enableLanguageFeature("InlineClasses")
            progressiveMode = true

            useExperimentalAnnotation("kotlin.Experimental")
            useExperimentalAnnotation("kotlin.OptIn")

            useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiExperimentalAPI")
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }

    sourceSets {
        getByName("test") {
            languageSettings.apply {
                languageVersion = "1.4"
            }
        }
    }
}

dependencies {
    compileAndRuntime("net.mamoe:mirai-core:${Versions.core}")
    compileAndRuntime(kotlin("stdlib"))

    api("net.mamoe.yamlkt:yamlkt:0.3.1")
    api("org.jetbrains:annotations:19.0.0")
    api(kotlinx("coroutines-jdk8", Versions.coroutines))

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
    testApi(kotlin("stdlib-jdk8"))
    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}
ext {
    // 傻逼 compileAndRuntime 没 exclude 掉
    // 傻逼 gradle 第二次配置 task 会覆盖掉第一次的配置
    val x: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.() -> Unit = {
        dependencyFilter.exclude {
            when ("${it.moduleGroup}:${it.moduleName}") {
                "net.mamoe:mirai-core" -> true
                "net.mamoe:mirai-core-qqandroid" -> true
                else -> false
            }
        }
    }
    this.set("shadowJar", x)
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }

    val compileKotlin by getting {}

    val fillBuildConstants by registering {
        doLast {
            return@doLast //
            (compileKotlin as KotlinCompile).source.filter { it.name == "MiraiConsole.kt" }.single().let { file ->
                file.writeText(file.readText()
                    .replace(Regex("""val buildDate: Date = Date\((.*)\) //(.*)""")) {
                        """
                        val buildDate: Date = Date(${System.currentTimeMillis()}L) // ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
                            timeZone = TimeZone.getTimeZone("GMT+8")
                        }.format(Date())}
                    """.trimIndent()
                    }
                    .replace(Regex("""const val version: String = "(.*)"""")) {
                        """
                        const val version: String = "${Versions.console}"
                    """.trimIndent()
                    }
                )
            }
        }
    }

    "compileKotlin" {
        dependsOn(fillBuildConstants)
    }
}

// region PUBLISHING

tasks.register("ensureBintrayAvailable") {
    doLast {
        if (!Bintray.isBintrayAvailable(project)) {
            error("bintray isn't available. ")
        }
    }
}

if (Bintray.isBintrayAvailable(project)) {
    bintray {
        val keyProps = Properties()
        val keyFile = file("../keys.properties")
        if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }
        if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }

        user = Bintray.getUser(project)
        key = Bintray.getKey(project)
        setPublications("mavenJava")
        setConfigurations("archives")

        pkg.apply {
            repo = "mirai"
            name = "mirai-console"
            setLicenses("AGPLv3")
            publicDownloadNumbers = true
            vcsUrl = "https://github.com/mamoe/mirai-console"
        }
    }

    @Suppress("DEPRECATION")
    val sourcesJar by tasks.registering(Jar::class) {
        classifier = "sources"
        from(sourceSets.main.get().allSource)
    }

    publishing {
        /*
        repositories {
            maven {
                // change to point to your repo, e.g. http://my.org/repo
                url = uri("$buildDir/repo")
            }
        }*/
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])

                groupId = rootProject.group.toString()
                artifactId = "mirai-console"
                version = version

                pom.withXml {
                    val root = asNode()
                    root.appendNode("description", description)
                    root.appendNode("name", project.name)
                    root.appendNode("url", "https://github.com/mamoe/mirai")
                    root.children().last()
                }

                artifact(sourcesJar.get())
            }
        }
    }
} else println("bintray isn't available. NO PUBLICATIONS WILL BE SET")

// endregion