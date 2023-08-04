/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.vote

import net.mamoe.mirai.utils.annotations.Range
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.time.Duration


/**
 * 构建 [VoteDescription].
 * @sample net.mamoe.mirai.contact.vote.VoteDescriptionBuilderSamples.simple
 * @sample net.mamoe.mirai.contact.vote.VoteDescriptionBuilderSamples.simple2
 * @since 2.15
 */
public inline fun buildVoteDescription(action: VoteDescriptionBuilder.() -> Unit): VoteDescription =
    VoteDescriptionBuilder().apply(action).build()

/**
 * [VoteDescription] 构建器
 *
 * 在 Kotlin 可使用 [buildVoteDescription] 通过 DSL 构建. 在 Java 可参考如下示例:
 *
 * ```java
 * VoteDescription desc = new VoteDescriptionBuilder()
 *         .title("世界上最好的编程语言是什么?")
 *         .isAnonymous(true) // 设置为匿名
 *         .duration(2 * 24 * 3600) // 2 天后结束
 *         .remind(1 * 24 * 3600) // 1 天后提醒
 *         .availableVotes(1) // 每人可投 1 票
 *         .options("Java", "Kotlin", "C", "C++") // 添加多个选项
 *         .option("都不是") // 添加一个选项
 *         .build();
 *
 * desc.publishTo(group); // 发布到群
 * ```
 * @since 2.15
 */
public class VoteDescriptionBuilder @JvmOverloads public constructor(
    prototype: VoteDescription = defaultVoteDescription
) {
    /**
     * 标题
     * @see VoteDescription.title
     */
    public var title: String = prototype.title
        @JvmName("title") get
        @JvmSynthetic set

    /**
     * 选项列表
     * @see VoteDescription.options
     */
    public val options: MutableList<String> = prototype.options.toMutableList()

    /**
     * 群投票的图片, 可通过 [Votes.uploadImage] 上传图片.
     * @see VoteDescription.image
     */
    public var image: VoteImage? = prototype.image
        @JvmName("image") get
        @JvmSynthetic set

    /**
     * 匿名
     * @see VoteDescription.isAnonymous
     */
    public var isAnonymous: Boolean = prototype.isAnonymous
        @JvmName("isAnonymous") get
        @JvmSynthetic set

    /**
     * 从发布到结束的时间间隔，单位秒
     * @see VoteDescription.durationSeconds
     */
    public var durationSeconds: Long = prototype.durationSeconds
        @JvmName("duration") get
        @JvmSynthetic set

    /**
     * 从发布到提醒群内还未完成投票的群员的时间间隔，单位秒
     * @see VoteDescription.remindSeconds
     */
    public var remindSeconds: Long = prototype.remindSeconds
        @JvmName("remind") get
        @JvmSynthetic set

    /**
     * 用户投票时可选的选项数量, 单选为 `1`, 多选为大于 `1`
     * @see VoteDescription.availableVotes
     */
    public var availableVotes: Int = prototype.availableVotes
        @JvmName("availableVotes") get
        @JvmSynthetic set


    /** 设置标题 [title] */
    public fun title(value: String): VoteDescriptionBuilder = apply {
        title = value
    }

    /** 添加一个选项 [options] */
    public fun option(value: String): VoteDescriptionBuilder = apply { options.add(value) }

    /** 添加一些选项 [options] */
    public fun options(vararg value: String): VoteDescriptionBuilder = apply { options.addAll(value) }

    /** 添加一些选项 [options] */
    public fun options(value: Iterable<String>): VoteDescriptionBuilder = apply { options.addAll(value) }

    /** 添加一些选项 [options] */
    public fun options(value: Sequence<String>): VoteDescriptionBuilder = apply { options.addAll(value) }

    /**
     * 设置图片
     * @see VoteDescription.image
     */
    public fun image(image: VoteImage?): VoteDescriptionBuilder = apply {
        this.image = image
    }

    /**
     * 设置匿名
     * @see VoteDescription.isAnonymous
     */
    public fun isAnonymous(anonymous: Boolean): VoteDescriptionBuilder = apply {
        this.isAnonymous = anonymous
    }

    /**
     * 设置从发布到结束的时间间隔, 单位秒
     * @see VoteDescription.durationSeconds
     */
    public fun duration(seconds: Long): VoteDescriptionBuilder = apply {
        this.durationSeconds = seconds
    }

    /**
     * 设置从发布到结束的时间间隔
     * @see VoteDescription.durationSeconds
     */
    public fun duration(duration: Duration): VoteDescriptionBuilder = apply {
        this.durationSeconds = duration.inWholeSeconds
    }

    /**
     * 设置从发布到提醒群内还未完成投票的群员的时间间隔, 单位秒
     * @see VoteDescription.remindSeconds
     */
    public fun remind(seconds: Long): VoteDescriptionBuilder = apply {
        require(seconds >= 0L) { "seconds must >= 0L" }
        this.remindSeconds = seconds
    }

    /**
     * 设置从发布到提醒群内还未完成投票的群员的时间间隔
     * @see VoteDescription.remindSeconds
     */
    public fun remind(duration: Duration): VoteDescriptionBuilder = apply {
        require(duration.inWholeSeconds >= 0L) { "duration must >= 0L" }
        this.remindSeconds = duration.inWholeSeconds
    }

    /**
     * 设置用户投票时可选的选项数量, 单选为 `1`, 多选为大于 `1`
     * @see VoteDescription.availableVotes
     */
    public fun availableVotes(number: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int): VoteDescriptionBuilder =
        apply {
            require(number >= 0L) { "number must >= 1L" }
            this.availableVotes = number
        }

    /** 使用设置的参数构造 [VoteDescription]. 调用此函数不会销毁本构建器, 本构建器仍可以用于之后的构建. */
    public fun build(): VoteDescription = VoteDescription(
        title, options, image, isAnonymous, durationSeconds, remindSeconds, availableVotes
    )
}

private val defaultVoteDescription = VoteDescription("", emptyList())

