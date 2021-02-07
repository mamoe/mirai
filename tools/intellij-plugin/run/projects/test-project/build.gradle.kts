plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
    id("net.mamoe.mirai-console") version "2.4-M1"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}