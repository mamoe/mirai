import java.util.*

plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}


apply(plugin = "com.github.johnrengelman.shadow")

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

val serializationVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.pure.MiraiConsolePureLoader"
    }
}


val miraiVersion: String by rootProject.ext

dependencies {
    compileOnly("net.mamoe:mirai-core-jvm:$miraiVersion")
    compileOnly("net.mamoe:mirai-core-qqandroid-jvm:$miraiVersion")

    api(kotlin("serialization"))


    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    api(group = "org.yaml", name = "snakeyaml", version = "1.25")
    api(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")


    api(kotlin("stdlib", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))

    api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))
    api(ktor("client-core-jvm", ktorVersion))
    api(kotlinx("serialization-runtime", serializationVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-io-jvm", coroutinesIoVersion))
    api(kotlinx("io-jvm", coroutinesIoVersion))

    api("org.bouncycastle:bcprov-jdk15on:1.64")

    api(kotlin("reflect", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))
    api(kotlinx("coroutines-core-common", coroutinesVersion))
    api(kotlinx("serialization-runtime-common", serializationVersion))

    api(ktor("http-cio", ktorVersion))
    api(ktor("http", ktorVersion))
    api(ktor("client-core-jvm", ktorVersion))
    api(ktor("client-cio", ktorVersion))
    api(ktor("client-core", ktorVersion))
    api(ktor("network", ktorVersion))
}

val mirai_console_version: String by project.ext
version = mirai_console_version

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
            version = mirai_console_version

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