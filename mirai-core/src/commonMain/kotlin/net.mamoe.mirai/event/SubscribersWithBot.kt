@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.HandlerWithSession
import net.mamoe.mirai.event.internal.Listener
import net.mamoe.mirai.event.internal.subscribeInternal
import net.mamoe.mirai.network.BotSession
import kotlin.reflect.KClass

/**
 * 该文件为所有的含 Bot 的事件的订阅方法
 *
 * 与不含 bot 的相比, 在监听时将会有 `this: Bot`
 * 在 demo 中找到实例可很快了解区别.
 */

// region 顶层方法

suspend inline fun <reified E : BotEvent> Bot.subscribe(noinline handler: suspend BotSession.(E) -> ListeningStatus): Listener<E> =
    E::class.subscribe(this, handler)

suspend inline fun <reified E : BotEvent> Bot.subscribeAlways(noinline listener: suspend BotSession.(E) -> Unit): Listener<E> =
    E::class.subscribeAlways(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeOnce(noinline listener: suspend BotSession.(E) -> Unit): Listener<E> =
    E::class.subscribeOnce(this, listener)

suspend inline fun <reified E : BotEvent, T> Bot.subscribeUntil(valueIfStop: T, noinline listener: suspend BotSession.(E) -> T): Listener<E> =
    E::class.subscribeUntil(this, valueIfStop, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeUntilFalse(noinline listener: suspend BotSession.(E) -> Boolean): Listener<E> =
    E::class.subscribeUntilFalse(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeUntilTrue(noinline listener: suspend BotSession.(E) -> Boolean): Listener<E> =
    E::class.subscribeUntilTrue(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeUntilNull(noinline listener: suspend BotSession.(E) -> Any?): Listener<E> =
    E::class.subscribeUntilNull(this, listener)


suspend inline fun <reified E : BotEvent, T> Bot.subscribeWhile(valueIfContinue: T, noinline listener: suspend BotSession.(E) -> T): Listener<E> =
    E::class.subscribeWhile(this, valueIfContinue, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeWhileFalse(noinline listener: suspend BotSession.(E) -> Boolean): Listener<E> =
    E::class.subscribeWhileFalse(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeWhileTrue(noinline listener: suspend BotSession.(E) -> Boolean): Listener<E> =
    E::class.subscribeWhileTrue(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeWhileNull(noinline listener: suspend BotSession.(E) -> Any?): Listener<E> =
    E::class.subscribeWhileNull(this, listener)

// endregion


// region KClass 的扩展方法 (仅内部使用)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribe(bot: Bot, handler: suspend BotSession.(E) -> ListeningStatus) =
    this.subscribeInternal(HandlerWithSession(bot, handler))

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeAlways(bot: Bot, listener: suspend BotSession.(E) -> Unit) =
    this.subscribeInternal(HandlerWithSession(bot) { listener(it); ListeningStatus.LISTENING })

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeOnce(bot: Bot, listener: suspend BotSession.(E) -> Unit) =
    this.subscribeInternal(HandlerWithSession(bot) { listener(it); ListeningStatus.STOPPED })

@PublishedApi
internal suspend fun <E : BotEvent, T> KClass<E>.subscribeUntil(bot: Bot, valueIfStop: T, listener: suspend BotSession.(E) -> T) =
    subscribeInternal(HandlerWithSession(bot) { if (listener(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeUntilFalse(bot: Bot, noinline listener: suspend BotSession.(E) -> Boolean) =
    subscribeUntil(bot, false, listener)

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeUntilTrue(bot: Bot, noinline listener: suspend BotSession.(E) -> Boolean) =
    subscribeUntil(bot, true, listener)

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeUntilNull(bot: Bot, noinline listener: suspend BotSession.(E) -> Any?) =
    subscribeUntil(bot, null, listener)


@PublishedApi
internal suspend fun <E : BotEvent, T> KClass<E>.subscribeWhile(bot: Bot, valueIfContinue: T, listener: suspend BotSession.(E) -> T) =
    subscribeInternal(HandlerWithSession(bot) { if (listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeWhileFalse(bot: Bot, noinline listener: suspend BotSession.(E) -> Boolean) =
    subscribeWhile(bot, false, listener)

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeWhileTrue(bot: Bot, noinline listener: suspend BotSession.(E) -> Boolean) =
    subscribeWhile(bot, true, listener)

@PublishedApi
internal suspend inline fun <E : BotEvent> KClass<E>.subscribeWhileNull(bot: Bot, noinline listener: suspend BotSession.(E) -> Any?) =
    subscribeWhile(bot, null, listener)

// endregion