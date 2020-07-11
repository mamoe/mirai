@file:Suppress("unused")

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.console.encodeToString
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.reflect.KClass

/**
 * 资源容器.
 */
public interface ResourceContainer {
    /**
     * 获取一个资源文件
     */
    public fun getResourceAsStream(name: String): InputStream

    /**
     * 读取一个资源文件并以 [Charsets.UTF_8] 编码为 [String]
     */
    @JvmDefault
    public fun getResource(name: String): String = getResource(name, Charsets.UTF_8)

    /**
     * 读取一个资源文件并以 [charset] 编码为 [String]
     */
    @JvmDefault
    public fun getResource(name: String, charset: Charset): String =
        this.getResourceAsStream(name).use { it.readBytes() }.encodeToString()

    public companion object {
        /**
         * 使用 [Class.getResourceAsStream] 读取资源文件
         *
         * @see asResourceContainer Kotlin 使用
         */
        @JvmStatic
        @JavaFriendlyAPI
        public fun byClass(clazz: Class<*>): ResourceContainer = clazz.asResourceContainer()
    }
}

/**
 * 使用 [Class.getResourceAsStream] 读取资源文件
 */
public fun KClass<*>.asResourceContainer(): ResourceContainer = ClassAsResourceContainer(this.java)

/**
 * 使用 [Class.getResourceAsStream] 读取资源文件
 */
public fun Class<*>.asResourceContainer(): ResourceContainer = ClassAsResourceContainer(this)


internal class ClassAsResourceContainer(
    private val clazz: Class<*>
) : ResourceContainer {
    override fun getResourceAsStream(name: String): InputStream = clazz.getResourceAsStream(name)
}