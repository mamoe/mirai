package net.mamoe.mirai

expect object MiraiEnvironment {
    val platform: Platform
}

enum class Platform {
    ANDROID,
    JVM
}