plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    jcenter()
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

dependencies {
    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

    api("org.jsoup:jsoup:1.12.1")

    api("com.google.code.gson:gson:2.8.6")
    api(kotlinx("coroutines-core", "1.3.3"))
    api(ktor("client-core", "1.3.2"))
    api(ktor("client-cio", "1.3.2"))
    api(ktor("client-json", "1.3.2"))
}