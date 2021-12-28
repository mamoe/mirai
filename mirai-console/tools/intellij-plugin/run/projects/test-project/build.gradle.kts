plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    id("net.mamoe.mirai-console") version "2.99.0-local"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {

}

repositories {
    mavenCentral()
    mavenLocal()
}