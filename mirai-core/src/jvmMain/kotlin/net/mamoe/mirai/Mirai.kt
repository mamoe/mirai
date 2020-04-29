package net.mamoe.mirai

/**
 * Mirai 全局环境.
 */
actual object Mirai {
    actual var fileCacheStrategy: FileCacheStrategy
        get() = TODO("Not yet implemented")
        set(value) {}

    actual interface FileCacheStrategy {

    }

}