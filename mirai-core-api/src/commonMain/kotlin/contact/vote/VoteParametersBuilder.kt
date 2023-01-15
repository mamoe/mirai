/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact.vote

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.time.Duration


/**
 * [VoteParameters] 的构建器. 可以构建一个 [VoteParameters] 实例.
 *
 * ## 获得实例
 *
 * 直接构造实例: `new VoteParameters()` 或者从已有的公告中获取 [VoteParameters.builder].
 *
 * ## 使用
 *
 * ### 在 Kotlin 使用
 *
 * ```
 * val parameters = buildVoteParameters {
 *     sendToNewMember = true
 *     // ...
 * }
 * ```
 *
 * ### 在 Java 使用
 *
 * ```java
 * AnnouncementParameters parameters = new AnnouncementParametersBuilder()
 *         .sendToNewMember(true)
 *         .pinned(true)
 *         .build();
 * ```
 *
 * @see buildVoteParameters
 *
 * @since 2.7
 */
public class VoteParametersBuilder @JvmOverloads constructor(
    prototype: VoteParameters = VoteParameters.DEFAULT
) {
    /**
     * @see VoteParameters.image
     */
    public var image: VoteImage? = prototype.image
        @JvmName("image") get
        @JvmSynthetic set

    /**
     * @see VoteParameters.anonymous
     */
    public var anonymous: Boolean = prototype.anonymous
        @JvmName("anonymous") get
        @JvmSynthetic set

    /**
     * @see VoteParameters.end
     */
    public var end: Long = prototype.end
        @JvmName("end") get
        @JvmSynthetic set

    /**
     * @see VoteParameters.remind
     */
    public var remind: Long = prototype.remind
        @JvmName("remind") get
        @JvmSynthetic set

    /**
     * @see VoteParameters.type
     */
    public var type: Int = prototype.type
        @JvmName("type") get
        @JvmSynthetic set

    /**
     * @see VoteParameters.image
     */
    public fun image(image: VoteImage?): VoteParametersBuilder {
        this.image = image
        return this
    }

    /**
     * @see VoteParameters.anonymous
     */
    public fun anonymous(anonymous: Boolean): VoteParametersBuilder {
        this.anonymous = anonymous
        return this
    }

    /**
     * @see VoteParameters.end
     */
    public fun end(seconds: Long): VoteParametersBuilder {
        this.end = seconds
        return this
    }

    /**
     * @see VoteParameters.end
     */
    public fun end(duration: Duration): VoteParametersBuilder {
        this.end = duration.inWholeSeconds
        return this
    }

    /**
     * @see VoteParameters.remind
     */
    public fun remind(seconds: Long): VoteParametersBuilder {
        this.remind = seconds
        return this
    }

    /**
     * @see VoteParameters.remind
     */
    public fun remind(duration: Duration): VoteParametersBuilder {
        this.remind = duration.inWholeSeconds
        return this
    }

    /**
     * @see VoteParameters.type
     */
    public fun type(type: Int): VoteParametersBuilder {
        this.type = type
        return this
    }

    /**
     * 使用当前参数构造 [VoteParameters].
     */
    public fun build(): VoteParameters =
        VoteParameters(image, anonymous, end, remind, type)
}

/**
 * 使用 [VoteParametersBuilder] 构建 [VoteParameters].
 * @see VoteParametersBuilder
 *
 * @since 2.7
 */
public inline fun buildVoteParameters(
    builderAction: VoteParametersBuilder.() -> Unit
): VoteParameters {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return VoteParametersBuilder().apply(builderAction).build()
}