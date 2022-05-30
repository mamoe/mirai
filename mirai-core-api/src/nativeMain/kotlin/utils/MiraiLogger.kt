/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.reflect.KClass

/**
 * 日志记录器.
 *
 * ## Mirai 日志系统
 *
 * Mirai 内建简单的日志系统, 即 [MiraiLogger]. [MiraiLogger] 的实现有 [SimpleLogger], [PlatformLogger], [SilentLogger].
 *
 * ## 实现或使用 [MiraiLogger]
 *
 * 不建议实现或使用 [MiraiLogger]. 请优先考虑使用上述第三方框架. [MiraiLogger] 仅应用于兼容旧版本代码.
 *
 * @see SimpleLogger 简易 logger, 它将所有的日志记录操作都转移给 lambda `(String?, Throwable?) -> Unit`
 * @see PlatformLogger 各个平台下的默认日志记录实现.
 * @see SilentLogger 忽略任何日志记录操作的 logger 实例.
 *
 * @see MiraiLoggerPlatformBase 平台通用基础实现. 若 Mirai 自带的日志系统无法满足需求, 请继承这个类并实现其抽象函数.
 */
public actual interface MiraiLogger {
    /**
     * 可以 service 实现的方式覆盖.
     *
     * @since 2.7
     */
    public actual interface Factory {
        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象的 class
         * @param identity 对象标记 (备注)
         */
        public actual fun create(requester: KClass<*>, identity: String?): MiraiLogger {
            return create(requester)
        }

        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象
         */
        public actual fun create(requester: KClass<*>): MiraiLogger {
            throw UnsupportedOperationException() // Cannot be abstract since on JVM it is open
        }

        public actual companion object INSTANCE : Factory by loadService(Factory::class, fallbackImplementation = {
            object : Factory {
                override fun create(requester: KClass<*>): MiraiLogger {
                    return PlatformLogger(requester.simpleName ?: requester.qualifiedName)
                }

                override fun create(requester: KClass<*>, identity: String?): MiraiLogger {
                    return PlatformLogger(identity ?: requester.simpleName ?: requester.qualifiedName)
                }
            }
        })
    }

    public actual companion object;

    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     *
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    public actual val identity: String?

    /**
     * 获取 [MiraiLogger] 是否已开启
     *
     * 除 [MiraiLoggerWithSwitch] 可控制开关外, 其他的所有 [MiraiLogger] 均一直开启.
     */
    public actual val isEnabled: Boolean

    /**
     * 当 VERBOSE 级别的日志启用时返回 `true`.
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isVerboseEnabled: Boolean get() = isEnabled

    /**
     * 当 DEBUG 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isDebugEnabled: Boolean get() = isEnabled

    /**
     * 当 INFO 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isInfoEnabled: Boolean get() = isEnabled

    /**
     * 当 WARNING 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isWarningEnabled: Boolean get() = isEnabled

    /**
     * 当 ERROR 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isErrorEnabled: Boolean get() = isEnabled

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    public actual fun verbose(message: String?)

    public actual fun verbose(e: Throwable?): Unit = verbose(null, e)
    public actual fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    public actual fun debug(message: String?)

    public actual fun debug(e: Throwable?): Unit = debug(null, e)
    public actual fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 _信息_ 级别的日志.
     */
    public actual fun info(message: String?)

    public actual fun info(e: Throwable?): Unit = info(null, e)
    public actual fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 _警告_ 级别的日志.
     */
    public actual fun warning(message: String?)

    public actual fun warning(e: Throwable?): Unit = warning(null, e)
    public actual fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 _错误_ 级别的日志.
     */
    public actual fun error(message: String?)

    public actual fun error(e: Throwable?): Unit = error(null, e)
    public actual fun error(message: String?, e: Throwable?)

    /** 根据优先级调用对应函数 */
    public actual fun call(priority: SimpleLogger.LogPriority, message: String?, e: Throwable?): Unit =
        priority.correspondingFunction(this, message, e)
}
