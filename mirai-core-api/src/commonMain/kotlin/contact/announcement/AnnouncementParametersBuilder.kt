/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact.announcement

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * [AnnouncementParameters] 的构建器. 可以构建一个 [AnnouncementParameters] 实例.
 *
 * ## 获得实例
 *
 * 直接构造实例: `new AnnouncementParametersBuilder()` 或者从已有的公告中获取 [AnnouncementParameters.builder].
 *
 * ## 使用
 *
 * ### 在 Kotlin 使用
 *
 * ```
 * val parameters = buildAnnouncementParameters {
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
 * @see buildAnnouncementParameters
 *
 * @since 2.7
 */
public class AnnouncementParametersBuilder @JvmOverloads constructor(
    prototype: AnnouncementParameters = AnnouncementParameters.DEFAULT
) {
    /**
     * @see AnnouncementParameters.image
     */
    public var image: AnnouncementImage? = prototype.image

    /**
     * @see AnnouncementParameters.sendToNewMember
     */
    public var sendToNewMember: Boolean = prototype.sendToNewMember

    /**
     * @see AnnouncementParameters.isPinned
     */
    public var isPinned: Boolean = prototype.isPinned

    /**
     * @see AnnouncementParameters.isShowEditCard
     */
    public var isShowEditCard: Boolean = prototype.isShowEditCard

    /**
     * @see AnnouncementParameters.isTip
     */
    public var isTip: Boolean = prototype.isTip

    /**
     * @see AnnouncementParameters.needConfirm
     */
    public var needConfirm: Boolean = prototype.needConfirm

    /**
     * @see AnnouncementParameters.image
     */
    public fun image(image: AnnouncementImage): AnnouncementParametersBuilder {
        this.image = image
        return this
    }

    /**
     * @see AnnouncementParameters.sendToNewMember
     */
    public fun sendToNewMember(sendToNewMember: Boolean): AnnouncementParametersBuilder {
        this.sendToNewMember = sendToNewMember
        return this
    }

    /**
     * @see AnnouncementParameters.isPinned
     */
    public fun pinned(isPinned: Boolean): AnnouncementParametersBuilder {
        this.isPinned = isPinned
        return this
    }

    /**
     * @see AnnouncementParameters.isShowEditCard
     */
    public fun showEditCard(isShowEditCard: Boolean): AnnouncementParametersBuilder {
        this.isShowEditCard = isShowEditCard
        return this
    }

    /**
     * @see AnnouncementParameters.isTip
     */
    public fun tip(isTip: Boolean): AnnouncementParametersBuilder {
        this.isTip = isTip
        return this
    }

    /**
     * @see AnnouncementParameters.needConfirm
     */
    public fun needConfirm(needConfirm: Boolean): AnnouncementParametersBuilder {
        this.needConfirm = needConfirm
        return this
    }

    /**
     * 使用当前参数构造 [AnnouncementParameters].
     */
    public fun build(): AnnouncementParameters =
        AnnouncementParameters(image, sendToNewMember, isPinned, isShowEditCard, isTip, needConfirm)
}

/**
 * 使用 [AnnouncementParametersBuilder] 构建 [AnnouncementParameters].
 * @see AnnouncementParametersBuilder
 *
 * @since 2.7
 */
public inline fun buildAnnouncementParameters(
    builderAction: AnnouncementParametersBuilder.() -> Unit
): AnnouncementParameters {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return AnnouncementParametersBuilder().apply(builderAction).build()
}