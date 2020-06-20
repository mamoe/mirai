@file:Suppress("UnstableApiUsage")

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
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

subprojects {
    afterEvaluate {
        apply<MiraiConsoleBuildPlugin>()
    }
}