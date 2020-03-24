plugins {
    id("kotlin")
}

apply(plugin = "com.github.johnrengelman.shadow")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.wrapper.WrapperMain"
    }
}


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
    //core && protocol
    api(kotlin("stdlib", Versions.Kotlin.stdlib))
    api(kotlin("serialization", Versions.Kotlin.stdlib))
    api(kotlin("reflect", Versions.Kotlin.stdlib))

    api(kotlinx("coroutines-core", Versions.Kotlin.coroutines))
    api(kotlinx("serialization-runtime-common", serializationVersion))
    api(kotlinx("serialization-protobuf-common", serializationVersion))
    api(kotlinx("io", kotlinXIoVersion))
    api(kotlinx("coroutines-io", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))

    api("org.jetbrains.kotlinx:atomicfu-common:$atomicFuVersion")

    api(ktor("client-cio", ktorVersion))
    api(ktor("client-core", ktorVersion))
    api(ktor("network", ktorVersion))
    api(kotlin("reflect", kotlinVersion))

    api(ktor("client-core-jvm", ktorVersion))
    api(kotlinx("io-jvm", kotlinXIoVersion))
    api(kotlinx("serialization-runtime", serializationVersion))
    api(kotlinx("serialization-protobuf", serializationVersion))
    api(kotlinx("coroutines-io-jvm", coroutinesIoVersion))
    api(kotlinx("coroutines-core", coroutinesVersion))

    api("org.bouncycastle:bcprov-jdk15on:1.64")

    api("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
    api(kotlinx("serialization-runtime-common", serializationVersion))
    api(kotlinx("serialization-protobuf-common", serializationVersion))
    api(kotlinx("serialization-runtime", serializationVersion))

    //for slf4j[ktor used]
   // api(group = "org.apache.cassandra", name = "cassandra-all", version = "0.8.1")

    //mirai-console
    api(group = "com.alibaba", name = "fastjson", version = "1.2.62")
    api(group = "org.yaml", name = "snakeyaml", version = "1.25")
    api(group = "com.moandjiezana.toml", name = "toml4j", version = "0.7.2")

}

version = Versions.Mirai.consoleWrapper

description = "Console with plugin support for mirai"