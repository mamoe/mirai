package net.mamoe.mirai.console.data

/**
 * 序列化之后的名称.
 *
 * 例:
 * ```
 * @ValueName("accounts")
 * object AccountPluginData : PluginData by ... {
 *    @ValueName("info")
 *    val map: Map<String, String> by value("a" to "b")
 * }
 * ```
 *
 * 将被保存为配置 (YAML 作为示例):
 * ```yaml
 * accounts:
 *   info:
 *     a: b
 * ```
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class ValueName(val value: String)
