import java.lang.System.getProperty
import java.util.*

buildscript {
    repositories {
        mavenLocal()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven") }
        jcenter()
        mavenCentral()
        google()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
    }

    dependencies {
        val kotlinVersion: String by project
        val atomicFuVersion: String by project

        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        @kotlin.Suppress("GradleDependency") // 1.3.70 有 bug, 无法编译添加 SerialInfo
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.3.61") // 不要用 $kotlinVersion
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion")
    }
}

runCatching {
    val keyProps = Properties().apply {
        file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    if (keyProps.getProperty("sdk.dir", "").isNotEmpty()) {
        project.ext.set("isAndroidSDKAvailable", true)
    } else {
        project.ext.set("isAndroidSDKAvailable", false)
    }
}

allprojects {
    group = "net.mamoe"
    version = getProperty("miraiVersion")

    repositories {
        mavenLocal()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven") }
        jcenter()
        mavenCentral()
        google()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
    }
}