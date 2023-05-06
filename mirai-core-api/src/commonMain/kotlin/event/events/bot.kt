/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "FunctionName")
@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:OptIn(MiraiInternalApi::class)

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

// note: 若你使用 IntelliJ IDEA, 按 alt + 7 可打开结构


/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 */
public data class BotOnlineEvent @MiraiInternalApi public constructor(
    public override val bot: Bot
) : BotActiveEvent, AbstractEvent()

/**
 * [Bot] 离线时广播的事件. Bot 离线不会 [关闭 Bot][Bot.close], 只会关闭 Bot 的网络层.
 */
public sealed class BotOfflineEvent : BotEvent, AbstractEvent() {
    /**
     * 为 `true` 时会尝试重连. 仅 [BotOfflineEvent.Force] 默认为 `false`, 其他默认为 `true`.
     */
    public open val reconnect: Boolean get() = true

    /**
     * 主动离线.
     *
     * 在调用 [Bot.close] 时, 如果 Bot 连接正常, 将会广播 [Active].
     *
     * 主动广播这个事件也可以让 [Bot] 离线, 但不建议这么做. 建议调用 [Bot.close].
     */
    @OptIn(MiraiExperimentalApi::class)
    public data class Active(
        public override val bot: Bot,
        public override val cause: Throwable?
    ) : BotOfflineEvent(), BotActiveEvent, CauseAware {
        override val reconnect: Boolean get() = false

        override fun toString(): String {
            return "BotOfflineEvent.Active(bot=$bot, cause=$cause, reconnect=$reconnect)"
        }
    }

    /**
     * 被挤下线. 默认不会自动重连. 可将 [reconnect] 改为 `true` 以重连.
     */
    public data class Force @MiraiInternalApi public constructor(
        public override val bot: Bot,
        public val title: String,
        public val message: String,
    ) : BotOfflineEvent(), Packet, BotPassiveEvent {
        override var reconnect: Boolean = bot.configuration.autoReconnectOnForceOffline

        override fun toString(): String {
            return "BotOfflineEvent.Force(bot=$bot, title='$title', message='$message', reconnect=$reconnect)"
        }
    }

    /**
     * 被服务器断开
     */
    @OptIn(MiraiExperimentalApi::class)
    @MiraiInternalApi("This is very experimental and might be changed")
    public data class MsfOffline @MiraiInternalApi public constructor(
        public override val bot: Bot,
        public override val cause: Throwable?
    ) : BotOfflineEvent(), Packet, BotPassiveEvent, CauseAware {
        override var reconnect: Boolean = true

        override fun toString(): String {
            return "BotOfflineEvent.MsfOffline(bot=$bot, cause=$cause, reconnect=$reconnect)"
        }
    }

    /**
     * 因网络问题而掉线
     */
    @OptIn(MiraiExperimentalApi::class)
    public data class Dropped
    @MiraiInternalApi public constructor(
        public override val bot: Bot,
        public override val cause: Throwable?
    ) : BotOfflineEvent(), Packet, BotPassiveEvent, CauseAware {
        override var reconnect: Boolean = true

        override fun toString(): String {
            return "BotOfflineEvent.Dropped(bot=$bot, cause=$cause, reconnect=$reconnect)"
        }
    }

    /**
     * 服务器主动要求更换另一个服务器
     */
    @OptIn(MiraiExperimentalApi::class)
    @MiraiInternalApi
    public data class RequireReconnect @MiraiInternalApi public constructor(
        public override val bot: Bot, override val cause: Throwable?,
    ) : BotOfflineEvent(), Packet, BotPassiveEvent, CauseAware {
        override var reconnect: Boolean = true

        override fun toString(): String {
            return "BotOfflineEvent.RequireReconnect(bot=$bot, cause=$cause, reconnect=$reconnect)"
        }
    }

    @MiraiExperimentalApi
    public interface CauseAware {
        public val cause: Throwable?
    }
}

/**
 * [Bot] 主动或被动重新登录. 在此事件广播前就已经登录完毕.
 */
public data class BotReloginEvent @MiraiInternalApi public constructor(
    public override val bot: Bot,
    public val cause: Throwable?
) : BotEvent, BotActiveEvent, AbstractEvent()

/**
 * [Bot] 头像被修改（通过其他客户端修改了头像）. 在此事件广播前就已经修改完毕.
 * @see FriendAvatarChangedEvent
 */
public data class BotAvatarChangedEvent(
    public override val bot: Bot
) : BotEvent, Packet, AbstractEvent()

/**
 * [Bot] 的昵称被改变事件, 在此事件触发时 bot 已经完成改名
 * @see FriendNickChangedEvent
 */
public data class BotNickChangedEvent(
    public override val bot: Bot,
    public val from: String,
    public val to: String
) : BotEvent, Packet, AbstractEvent()