import java.util.*

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}

apply(plugin = "com.github.johnrengelman.shadow")

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("kotlin.OptIn")
        }
    }
}
dependencies {
    compileOnly("net.mamoe:mirai-core-jvm:${Versions.Mirai.core}")
    compileOnly("net.mamoe:mirai-core-qqandroid-jvm:${Versions.Mirai.core}")


    compileOnly(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    compileOnly(group = "org.yaml", name = "snakeyaml", version = "1.25")
    compileOnly(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")


    compileOnly(kotlin("stdlib", Versions.Kotlin.stdlib))
    compileOnly(kotlin("serialization", Versions.Kotlin.stdlib))
    compileOnly(kotlin("reflect", Versions.Kotlin.stdlib))

    compileOnly(kotlinx("coroutines-io-jvm", Versions.Kotlin.coroutinesIo))
    compileOnly(kotlinx("coroutines-core", Versions.Kotlin.coroutines))
    compileOnly(kotlinx("serialization-runtime", Versions.Kotlin.serialization))
    compileOnly("org.jetbrains.kotlinx:atomicfu:${Versions.Kotlin.atomicFU}")

    compileOnly("org.bouncycastle:bcprov-jdk15on:1.64")

    compileOnly(ktor("http-cio"))
    compileOnly(ktor("http-jvm"))
    compileOnly(ktor("io-jvm"))
    compileOnly(ktor("client-core-jvm"))
    compileOnly(ktor("client-cio"))
    compileOnly(ktor("network"))
}

val miraiConsoleVersion: String by project.ext
version = miraiConsoleVersion

description = "Console with plugin support for mirai"
bintray {
    val keyProps = Properties()
    val keyFile = file("../keys.properties")
    if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }
    if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }

    user = keyProps.getProperty("bintrayUser")
    key = keyProps.getProperty("bintrayKey")
    setPublications("mavenJava")
    setConfigurations("archives")

    pkg.apply {
        repo = "mirai"
        name = "mirai-console"
        setLicenses("AGPLv3")
        publicDownloadNumbers = true
        vcsUrl = "https://github.com/mamoe/mirai"
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
            version = miraiConsoleVersion

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