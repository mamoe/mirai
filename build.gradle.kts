buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.Kotlin.stdlib}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.stdlib}")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4") // don"t use any other.
    }
}

allprojects {
    group = "net.mamoe"

    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }
}