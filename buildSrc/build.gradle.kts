plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

    api(kotlinx("coroutines-core", "1.3.3"))
    api(ktor("client-core", "1.3.2"))
    api(ktor("client-cio", "1.3.2"))
    api(ktor("client-json", "1.3.2"))
}