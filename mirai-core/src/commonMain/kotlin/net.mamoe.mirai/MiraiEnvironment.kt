package net.mamoe.mirai

/**
 * 平台相关环境属性
 */
expect object MiraiEnvironment {
    val platform: Platform
}

/**
 * 可用平台列表
 */
enum class Platform {
    ANDROID,
    JVM
}