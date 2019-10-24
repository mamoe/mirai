@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
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

suspend inline fun <reified E : Event> Bot.subscribe(noinline handler: suspend Bot.(E) -> ListeningStatus) = E::class.subscribe(this, handler)

suspend inline fun <reified E : Event> Bot.subscribeAlways(noinline listener: suspend Bot.(E) -> Unit) = E::class.subscribeAlways(this, listener)

suspend inline fun <reified E : Event> Bot.subscribeOnce(noinline listener: suspend Bot.(E) -> Unit) = E::class.subscribeOnce(this, listener)

suspend inline fun <reified E : Event, T> Bot.subscribeUntil(valueIfStop: T, noinline listener: suspend Bot.(E) -> T) = E::class.subscribeUntil(this, valueIfStop, listener)
suspend inline fun <reified E : Event> Bot.subscribeUntilFalse(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeUntilFalse(this, listener)
suspend inline fun <reified E : Event> Bot.subscribeUntilTrue(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeUntilTrue(this, listener)
suspend inline fun <reified E : Event> Bot.subscribeUntilNull(noinline listener: suspend Bot.(E) -> Any?) = E::class.subscribeUntilNull(this, listener)


suspend inline fun <reified E : Event, T> Bot.subscribeWhile(valueIfContinue: T, noinline listener: suspend Bot.(E) -> T) = E::class.subscribeWhile(this, valueIfContinue, listener)
suspend inline fun <reified E : Event> Bot.subscribeWhileFalse(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeWhileFalse(this, listener)
suspend inline fun <reified E : Event> Bot.subscribeWhileTrue(noinline listener: suspend Bot.(E) -> Boolean) = E::class.subscribeWhileTrue(this, listener)
suspend inline fun <reified E : Event> Bot.subscribeWhileNull(noinline listener: suspend Bot.(E) -> Any?) = E::class.subscribeWhileNull(this, listener)

// endregion


// region KClass 的扩展方法 (不推荐)

suspend fun <E : Event> KClass<E>.subscribe(bot: Bot, handler: suspend Bot.(E) -> ListeningStatus) = this.subscribeInternal(HandlerWithBot(bot, handler))

suspend fun <E : Event> KClass<E>.subscribeAlways(bot: Bot, listener: suspend Bot.(E) -> Unit) =
    this.subscribeInternal(HandlerWithBot(bot) { listener(it); ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeOnce(bot: Bot, listener: suspend Bot.(E) -> Unit) = this.subscribeInternal(HandlerWithBot(bot) { listener(it); ListeningStatus.STOPPED })

suspend fun <E : Event, T> KClass<E>.subscribeUntil(bot: Bot, valueIfStop: T, listener: suspend Bot.(E) -> T) =
    subscribeInternal(HandlerWithBot(bot) { if (listener(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeUntilFalse(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeUntil(bot, false, listener)
suspend fun <E : Event> KClass<E>.subscribeUntilTrue(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeUntil(bot, true, listener)
suspend fun <E : Event> KClass<E>.subscribeUntilNull(bot: Bot, listener: suspend Bot.(E) -> Any?) = subscribeUntil(bot, null, listener)


suspend fun <E : Event, T> KClass<E>.subscribeWhile(bot: Bot, valueIfContinue: T, listener: suspend Bot.(E) -> T) =
    subscribeInternal(HandlerWithBot(bot) { if (listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeWhileFalse(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeWhile(bot, false, listener)
suspend fun <E : Event> KClass<E>.subscribeWhileTrue(bot: Bot, listener: suspend Bot.(E) -> Boolean) = subscribeWhile(bot, true, listener)
suspend fun <E : Event> KClass<E>.subscribeWhileNull(bot: Bot, listener: suspend Bot.(E) -> Any?) = subscribeWhile(bot, null, listener)

// endregion