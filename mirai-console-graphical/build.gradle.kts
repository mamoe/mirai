import java.util.*

plugins {
    id("kotlinx-serialization")
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("kotlin")
    id("java")
    id("com.jfrog.bintray")
    `maven-publish`
}

javafx {
    version = "13.0.2"
    modules = listOf("javafx.controls")
    //mainClassName = "Application"
}

apply(plugin = "com.github.johnrengelman.shadow")


/*
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.graphical.MiraiGraphicalLoader"
    }
}
 */

version = Versions.Mirai.consoleGraphical

description = "Console Graphical Version with plugin support for mirai"
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
        name = "mirai-console-graphical"
        setLicenses("AGPLv3")
        publicDownloadNumbers = true
        vcsUrl = "https://github.com/mamoe/mirai"
    }
}

dependencies {
    compileOnly("net.mamoe:mirai-core-jvm:${Versions.Mirai.core}")
    implementation(project(":mirai-console"))

    api(group = "no.tornado", name = "tornadofx", version = "1.7.19")
    api(group = "com.jfoenix", name = "jfoenix", version = "9.0.8")

    testApi(project(":mirai-console"))
    testApi(group = "org.yaml", name = "snakeyaml", version = "1.25")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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
            artifactId = "mirai-console-graphical"
            version = Versions.Mirai.consoleGraphical

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