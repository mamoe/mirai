import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
    compileOnly("net.mamoe:mirai-core:${Versions.Mirai.core}")
    compileOnly(kotlin("stdlib")) // embedded by core

    api("com.google.code.gson:gson:2.8.6")
    api(group = "org.yaml", name = "snakeyaml", version = "1.25")
    api(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")
    api("org.jsoup:jsoup:1.12.1")

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.Mirai.core}")
    testApi(kotlin("stdlib"))
}

version = Versions.Mirai.console

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
repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}
