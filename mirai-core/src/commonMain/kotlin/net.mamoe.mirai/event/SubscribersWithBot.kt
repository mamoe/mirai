@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.HandlerWithBot
import net.mamoe.mirai.event.internal.subscribeInternal
import kotlin.reflect.KClass

/**
 * 该文件为所有的含 Bot 的事件的订阅方法
 *
 * 与不含 bot 的相比, 在监听时将会有 `this: Bot`
 * 在 demo 中找到实例可很快了解区别.
 */

// region 顶层方法

suspend inline fun <reified E : BotEvent> Bot.subscribe(noinline handler: suspend Bot.(E) -> ListeningStatus) = E::class.subscribe(this, handler)

suspend inline fun <reified E : BotEvent> Bot.subscribeAlways(noinline listener: suspend Bot.(E) -> Unit) = E::class.subscribeAlways(this, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeOnce(noinline listener: suspend Bot.(E) -> Unit) = E::class.subscribeOnce(this, listener)

suspend inline fun <reified E : BotEvent, T> Bot.subscribeUntil(valueIfStop: T, noinline listener: suspend Bot.(E) -> T) = E::class.subscribeUntil(this, valueIfStop, listener)
suspend inline fun <reified E : BotEvent> Bot.subscribeUntilFalse(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeUntilFalse(this, listener)
suspend inline fun <reified E : BotEvent> Bot.subscribeUntilTrue(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeUntilTrue(this, listener)
suspend inline fun <reified E : BotEvent> Bot.subscribeUntilNull(noinline listener: suspend Bot.(E) -> Any?) = E::class.subscribeUntilNull(this, listener)


suspend inline fun <reified E : BotEvent, T> Bot.subscribeWhile(valueIfContinue: T, noinline listener: suspend Bot.(E) -> T) =
    E::class.subscribeWhile(this, valueIfContinue, listener)

suspend inline fun <reified E : BotEvent> Bot.subscribeWhileFalse(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeWhileFalse(this, listener)
suspend inline fun <reified E : BotEvent> Bot.subscribeWhileTrue(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeWhileTrue(this, listener)
suspend inline fun <reified E : BotEvent> Bot.subscribeWhileNull(noinline listener: suspend Bot.(E) -> Any?) = E::class.subscribeWhileNull(this, listener)

// endregion


// region KClass 的扩展方法 (仅内部使用)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribe(bot: Bot, handler: suspend Bot.(E) -> ListeningStatus) = this.subscribeInternal(HandlerWithBot(bot, handler))

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeAlways(bot: Bot, listener: suspend Bot.(E) -> Unit) =
    this.subscribeInternal(HandlerWithBot(bot) { listener(it); ListeningStatus.LISTENING })

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeOnce(bot: Bot, listener: suspend Bot.(E) -> Unit) =
    this.subscribeInternal(HandlerWithBot(bot) { listener(it); ListeningStatus.STOPPED })

@PublishedApi
internal suspend fun <E : BotEvent, T> KClass<E>.subscribeUntil(bot: Bot, valueIfStop: T, listener: suspend Bot.(E) -> T) =
    subscribeInternal(HandlerWithBot(bot) { if (listener(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeUntilFalse(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeUntil(bot, false, listener)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeUntilTrue(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeUntil(bot, true, listener)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeUntilNull(bot: Bot, listener: suspend Bot.(E) -> Any?) = subscribeUntil(bot, null, listener)


@PublishedApi
internal suspend fun <E : BotEvent, T> KClass<E>.subscribeWhile(bot: Bot, valueIfContinue: T, listener: suspend Bot.(E) -> T) =
    subscribeInternal(HandlerWithBot(bot) { if (listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeWhileFalse(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeWhile(bot, false, listener)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeWhileTrue(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeWhile(bot, true, listener)

@PublishedApi
internal suspend fun <E : BotEvent> KClass<E>.subscribeWhileNull(bot: Bot, listener: suspend Bot.(E) -> Any?) = subscribeWhile(bot, null, listener)

// endregion