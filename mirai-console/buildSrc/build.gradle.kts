plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

dependencies {
    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String = "1.3.2") = "io.ktor:ktor-$id:$version"

    api("org.jsoup:jsoup:1.12.1")

    api("com.google.code.gson:gson:2.8.6")
    api(kotlinx("coroutines-core", "1.3.8"))

    api(ktor("client-core"))
    api(ktor("client-cio"))
    api(ktor("client-json"))

    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
}