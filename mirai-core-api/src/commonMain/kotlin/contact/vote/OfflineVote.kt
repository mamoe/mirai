/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.contact.vote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.copy
import net.mamoe.mirai.utils.map
import net.mamoe.mirai.utils.safeCast
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.native.CName

/**
 * 表示在本地构建的 [Vote].
 *
 * 支持序列化, 使用 [serializer].
 *
 * 可以通过 [OfflineVote], [OfflineVote.create] 等方法构建, 然后使用 [OfflineVote.publishTo] 或 [Votes.publish] 发布投票到群.
 *
 * 在 [Vote] 获取更多信息.
 *
 * @see OnlineVote.publishTo
 *
 * @since 2.15
 */
@Serializable(OfflineVote.Companion.Serializer::class)
@SerialName(OfflineVote.SERIAL_NAME)
public sealed interface OfflineVote : Vote {
    public companion object {
        public const val SERIAL_NAME: String = "OfflineVote"

        /**
         * 创建 [OfflineVote]. 若 [vote] 类型为 [OfflineVote] 则直接返回 [vote].
         *
         * 若要转发获取到的投票到一个群, 可直接调用 [Vote.publishTo] 而不需要构造 [OfflineVote].
         *
         * @see OnlineVote.toOffline
         */
        @JvmStatic
        public inline fun from(vote: Vote): OfflineVote =
            vote.safeCast() ?: vote.run { create(title, vote.options, parameters) }

        /**
         * 创建 [OfflineVote].
         * @param title 投票标题
         * @param options 投票选项
         * @param parameters 可选的附加参数
         */
        @JvmOverloads
        @JvmStatic
        public fun create(
            title: String,
            options: List<String>,
            parameters: VoteParameters = VoteParameters.DEFAULT
        ): OfflineVote = OfflineVoteImpl(title, options, parameters)

        /**
         * 创建 [VoteParameters] 并创建 [OfflineVote].
         * @param title 投票标题
         * @param options 投票选项
         * @param parameters 可选的附加参数
         * @see VoteParametersBuilder
         */
        @JvmSynthetic
        public inline fun create(
            title: String,
            options: List<String>,
            parameters: VoteParametersBuilder.() -> Unit
        ): OfflineVote {
            contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
            return create(title, options, buildVoteParameters(parameters))
        }

        internal object Serializer : KSerializer<OfflineVote> by OfflineVoteImpl.serializer().map(
            resultantDescriptor = OfflineVoteImpl.serializer().descriptor.copy(SERIAL_NAME),
            deserialize = { it },
            serialize = { it.safeCast<OfflineVoteImpl>() ?: create(title, options, parameters).cast() }
        )
    }
}

/**
 * 依据 [from] 创建 [OfflineVote]. 若 [from] 类型为 [OfflineVote] 则直接返回 [from].
 * @since 2.15
 */
@CName("", "OfflineVote_new")
public inline fun OfflineVote(from: Vote): OfflineVote =
    OfflineVote.from(from)

/**
 * 创建 [VoteParameters] 并创建 [OfflineVote].
 * @param title 投票标题
 * @param options 投票选项
 * @param parameters 可选的附加参数
 * @since 2.15
 */
@CName("", "OfflineVote_new2")
public inline fun OfflineVote(
    title: String,
    options: List<String>,
    parameters: VoteParameters = VoteParameters.DEFAULT
): OfflineVote = OfflineVote.create(title, options, parameters)

/**
 * 创建 [VoteParameters] 并创建 [OfflineVote].
 * @param title 投票内容
 * @param options 投票选项
 * @param parameters 可选的附加参数
 * @see VoteParametersBuilder
 * @since 2.15
 */
@CName("", "OfflineVote_new3")
public inline fun OfflineVote(
    title: String,
    options: List<String>,
    parameters: VoteParametersBuilder.() -> Unit
): OfflineVote {
    contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
    return OfflineVote.create(title, options, parameters)
}

@SerialName(OfflineVote.SERIAL_NAME)
@Serializable
private data class OfflineVoteImpl(
    override val title: String,
    override val options: List<String>,
    override val parameters: VoteParameters
) : OfflineVote {
    override fun toString() = "OfflineVote(title='$title', options=$options parameters=$parameters)"
}