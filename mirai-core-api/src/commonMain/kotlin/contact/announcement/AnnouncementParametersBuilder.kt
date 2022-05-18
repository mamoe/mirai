/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic


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
        @JvmName("image") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.sendToNewMember
     */
    public var sendToNewMember: Boolean = prototype.sendToNewMember
        @JvmName("sendToNewMember") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.isPinned
     */
    public var isPinned: Boolean = prototype.isPinned
        @JvmName("isPinned") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.showEditCard
     */
    public var showEditCard: Boolean = prototype.showEditCard
        @JvmName("showEditCard") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.showPopup
     */
    public var showPopup: Boolean = prototype.showPopup
        @JvmName("showPopup") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.requireConfirmation
     */
    public var requireConfirmation: Boolean = prototype.requireConfirmation
        @JvmName("requireConfirmation") get
        @JvmSynthetic set

    /**
     * @see AnnouncementParameters.image
     */
    public fun image(image: AnnouncementImage?): AnnouncementParametersBuilder {
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
    public fun isPinned(isPinned: Boolean): AnnouncementParametersBuilder {
        this.isPinned = isPinned
        return this
    }

    /**
     * @see AnnouncementParameters.showEditCard
     */
    public fun showEditCard(isShowEditCard: Boolean): AnnouncementParametersBuilder {
        this.showEditCard = isShowEditCard
        return this
    }

    /**
     * @see AnnouncementParameters.showPopup
     */
    public fun showPopup(showPopup: Boolean): AnnouncementParametersBuilder {
        this.showPopup = showPopup
        return this
    }

    /**
     * @see AnnouncementParameters.requireConfirmation
     */
    public fun requireConfirmation(requireConfirmation: Boolean): AnnouncementParametersBuilder {
        this.requireConfirmation = requireConfirmation
        return this
    }

    /**
     * 使用当前参数构造 [AnnouncementParameters].
     */
    public fun build(): AnnouncementParameters =
        AnnouncementParameters(image, sendToNewMember, isPinned, showEditCard, showPopup, requireConfirmation)
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