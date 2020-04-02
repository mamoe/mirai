plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
}


apply(plugin = "com.github.johnrengelman.shadow")

version = Versions.Mirai.console

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.MiraiConsoleTerminalLoader"
    }
}
dependencies {
    compileOnly("net.mamoe:mirai-core-qqandroid:${Versions.Mirai.core}")
    api(project(":mirai-console"))
    api(group = "com.googlecode.lanterna", name = "lanterna", version = "3.0.2")
}