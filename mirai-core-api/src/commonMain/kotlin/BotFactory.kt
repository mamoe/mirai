/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.PlannedRemoval

/**
 * 构造 [Bot] 的工厂. 这是 [Bot] 唯一的构造方式.
 *
 * @see IMirai.BotFactory
 */
public interface BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String): Bot = newBot(qq, password, BotConfiguration.Default)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray): Bot = newBot(qq, passwordMd5, BotConfiguration.Default)


    @Suppress("UNUSED_PARAMETER")
    @PlannedRemoval("2.0-M2")
    @Deprecated(
        "For migration. Use newBot(qq, password, configuration)",
        ReplaceWith("newBot(qq, password, configuration)"),
        DeprecationLevel.ERROR
    )
    public fun Bot(
        context: Any?,
        qq: Long,
        password: String,
        configuration: BotConfiguration = BotConfiguration.Default
    ): Bot = newBot(qq, password, configuration)

    @Suppress("UNUSED_PARAMETER")
    @PlannedRemoval("2.0-M2")
    @Deprecated(
        "For migration. Use newBot(qq, password, configuration)",
        ReplaceWith("newBot(qq, password, configuration)"),
        DeprecationLevel.ERROR
    )
    public fun Bot(
        context: Any?,
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration = BotConfiguration.Default
    ): Bot = newBot(qq, passwordMd5, configuration)

    public companion object INSTANCE : BotFactory {
        override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, password, configuration)
        }

        override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, passwordMd5, configuration)
        }
    }
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun BotFactory.newBot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.newBot(qq, password, BotConfiguration().apply(configuration))

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun BotFactory.newBot(qq: Long, password: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.newBot(qq, password, BotConfiguration().apply(configuration))


// deprecated:


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
        "net.mamoe.mirai.newBot"
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    context: Any?,
    qq: Long,
    password: String,
    configuration: (BotConfiguration.() -> Unit)
): Bot = this.newBot(qq, password, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    context: Any?,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: (BotConfiguration.() -> Unit)
): Bot = this.newBot(qq, passwordMd5, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
        "net.mamoe.mirai.newBot"
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    password: String,
    configuration: (BotConfiguration.() -> Unit)
): Bot = BotFactory.newBot(qq, password, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    password: String,
    configuration: BotConfiguration
): Bot = BotFactory.newBot(qq, password, configuration)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: (BotConfiguration.() -> Unit)
): Bot = BotFactory.newBot(qq, passwordMd5, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: BotConfiguration
): Bot = BotFactory.newBot(qq, passwordMd5, configuration)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
        "net.mamoe.mirai.newBot"
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    qq: Long,
    password: String,
    configuration: (BotConfiguration.() -> Unit)
): Bot = this.newBot(qq, password, BotConfiguration().apply(configuration))


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    qq: Long,
    passwordMd5: ByteArray,
    configuration: (BotConfiguration.() -> Unit)
): Bot = this.newBot(qq, passwordMd5, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
        "net.mamoe.mirai.newBot"
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    password: String,
    configuration: (BotConfiguration.() -> Unit)
): Bot = BotFactory.newBot(qq, password, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password, configuration)",
    ReplaceWith(
        "BotFactory.newBot(qq, password, configuration)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    password: String,
    configuration: BotConfiguration
): Bot = BotFactory.newBot(qq, password, configuration)


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    passwordMd5: ByteArray,
    configuration: (BotConfiguration.() -> Unit)
): Bot = BotFactory.newBot(qq, passwordMd5, BotConfiguration().apply(configuration))

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5, configuration)",
    ReplaceWith("BotFactory.newBot(qq, passwordMd5, configuration)", "net.mamoe.mirai.BotFactory"),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    passwordMd5: ByteArray,
    configuration: BotConfiguration
): Bot = BotFactory.newBot(qq, passwordMd5, configuration)


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password)",
    ReplaceWith(
        "BotFactory.newBot(qq, password)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    context: Any?,
    qq: Long,
    password: String,
): Bot = this.newBot(qq, password)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5)",
    ReplaceWith(
        "BotFactory.newBot(qq, passwordMd5)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    context: Any?,
    qq: Long,
    passwordMd5: ByteArray,
): Bot = this.newBot(qq, passwordMd5)


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password)",
    ReplaceWith(
        "BotFactory.newBot(qq, password)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    qq: Long,
    password: String,
): Bot = this.newBot(qq, password)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5)",
    ReplaceWith(
        "BotFactory.newBot(qq, passwordMd5)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun BotFactory.Bot(
    qq: Long,
    passwordMd5: ByteArray,
): Bot = this.newBot(qq, passwordMd5)


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password)",
    ReplaceWith(
        "BotFactory.newBot(qq, password)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    password: String,
): Bot = BotFactory.newBot(qq, password)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5)",
    ReplaceWith(
        "BotFactory.newBot(qq, passwordMd5)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    context: Any?,
    qq: Long,
    passwordMd5: ByteArray,
): Bot = BotFactory.newBot(qq, passwordMd5)


@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, password)",
    ReplaceWith(
        "BotFactory.newBot(qq, password)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    password: String,
): Bot = BotFactory.newBot(qq, password)

@Suppress("UNUSED_PARAMETER")
@PlannedRemoval("2.0-M2")
@Deprecated(
    "For migration. Use BotFactory.newBot(qq, passwordMd5)",
    ReplaceWith(
        "BotFactory.newBot(qq, passwordMd5)",
        "net.mamoe.mirai.BotFactory",
    ),
    DeprecationLevel.ERROR
)
public inline fun Bot(
    qq: Long,
    passwordMd5: ByteArray,
): Bot = BotFactory.newBot(qq, passwordMd5)

