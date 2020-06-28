@file:Suppress("UnstableApiUsage")
plugins {
    id("com.jfrog.bintray") version Versions.bintray apply false
}
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

allprojects {
    group = "net.mamoe"

    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        apply<MiraiConsoleBuildPlugin>()

        setJavaCompileTarget()
    }
}