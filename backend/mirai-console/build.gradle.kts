import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import upload.Bintray
import java.util.*

plugins {
    id("kotlin")
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
            languageSettings.progressiveMode = true
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

dependencies {
    compileAndRuntime("net.mamoe:mirai-core:${Versions.Mirai.core}")
    compileAndRuntime(kotlin("stdlib"))

    api("net.mamoe.yamlkt:yamlkt:0.3.1")

    api("org.jetbrains:annotations:19.0.0")

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.Mirai.core}")
    testApi(kotlin("stdlib"))
    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))
}

version = Versions.Mirai.console

description = "Console backend for mirai"

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