import java.util.*

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        google()
    }

    dependencies {
        // Do try to waste your time.
        @kotlin.Suppress("GradleDependency") // 1.8.4 不能跑
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.0")
    }
}

plugins {
    kotlin("jvm")
    java
    `maven-publish`
    // maven
    id("com.jfrog.bintray") version "1.8.0"
}

val kotlinVersion: String by rootProject.ext
val atomicFuVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext
val kotlinXIoVersion: String by rootProject.ext
val coroutinesIoVersion: String by rootProject.ext
val serializationVersion: String by rootProject.ext

val klockVersion: String by rootProject.ext
val ktorVersion: String by rootProject.ext

description = "Java helper for Mirai"

@Suppress("PropertyName")
val mirai_japt_version: String by rootProject.ext
version = mirai_japt_version

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

dependencies {
    api(project(":mirai-core"))
    runtimeOnly(files("../mirai-core/build/classes/kotlin/jvm/main")) // classpath is not added correctly by IDE

    api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "1.3.2")

    api(kotlin("stdlib", kotlinVersion))
    api(kotlinx("io-jvm", kotlinXIoVersion))
    api(kotlinx("io", kotlinXIoVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))
    api(kotlin("stdlib-jdk7", kotlinVersion))
    api(kotlin("stdlib-jdk8", kotlinVersion))
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

bintray {
    val keyProps = Properties()
    val keyFile = file("../keys.properties")
    if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }

    user = keyProps.getProperty("bintrayUser")
    key = keyProps.getProperty("bintrayKey")
    setPublications("mavenJava")
    setConfigurations("archives")

    pkg.apply {
        repo = "mirai"
        name = "mirai-japt"
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
            artifactId = "mirai-japt"
            version = mirai_japt_version

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