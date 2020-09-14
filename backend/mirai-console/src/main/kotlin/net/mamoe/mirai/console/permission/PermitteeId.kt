/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.permission

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.internal.permission.parseFromStringImpl
import net.mamoe.mirai.console.permission.AbstractPermitteeId.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User

/**
 * [被许可人][Permittee] 的标识符
 *
 * 一个这样的标识符即可代表特定的单个 [Permittee], 也可以表示多个同类 [Permittee].
 *
 * ### 获取 [PermitteeId]
 * 总是通过 [Permittee.permitteeId].
 */
@PermissionImplementation
public interface PermitteeId {
    /**
     * 直接父 [PermitteeId]. 在检查权限时会首先检查自己, 再递归检查父类.
     *
     * @see allParentsWithSelf
     * @see allParents
     */
    public val directParents: Array<out PermitteeId>

    /**
     * 转换为字符串表示. 用于权限服务识别和指令的解析.
     */
    public fun asString(): String

    public companion object {
        /**
         * 当 [this] 或 [this] 的任意一个直接或间接父 [PermitteeId.asString] 与 `this.asString` 相同时返回 `true`
         */
        @JvmStatic
        public fun PermitteeId.hasChild(child: PermitteeId): Boolean {
            return allParentsWithSelf.any { it.asString() == child.asString() } // asString is for compatibility issue with external implementations
        }

        /**
         * 获取所有直接或间接父类的 [PermitteeId].
         */
        @get:JvmStatic
        public val PermitteeId.allParentsWithSelf: Sequence<PermitteeId>
            get() = sequence {
                yield(this@allParentsWithSelf)
                yieldAll(allParents)
            }

        /**
         * 获取所有直接或间接父类的 [PermitteeId], 返回包含 `this` + 这些父类 的 [Sequence]
         */
        @get:JvmStatic
        public val PermitteeId.allParents: Sequence<PermitteeId>
            get() = directParents.asSequence().flatMap { it.allParentsWithSelf }

        /**
         * 创建 [AbstractPermitteeId.ExactUser]
         */
        @get:JvmSynthetic
        public val User.permitteeId: ExactUser
            get() = ExactUser(id)

        /**
         * 创建 [AbstractPermitteeId.ExactMember]
         */
        @get:JvmSynthetic
        public val Member.permitteeId: ExactMember
            get() = ExactMember(group.id, id)

        /**
         * 创建 [AbstractPermitteeId.ExactGroup]
         */
        @get:JvmSynthetic
        public val Group.permitteeId: ExactGroup
            get() = ExactGroup(id)

        /**
         * 创建 [AbstractPermitteeId.ExactTemp]
         */
        @get:JvmSynthetic
        public val Member.permitteeIdOnTemp: ExactTemp
            get() = ExactTemp(group.id, id)
    }
}

/**
 * 内建的 [PermitteeId].
 *
 * - 若指令 A 的权限被授予给 [AnyMember], 那么一个 [ExactMember] 可以执行这个指令.
 *
 * #### 字符串表示
 *
 * 当使用 [PermitteeId.asString] 时, 不同的类型的返回值如下表所示. 这些格式也适用于 [BuiltInCommands.PermissionCommand].
 *
 * (不区分大小写. 不区分 Bot).
 *
 *
 * |    被许可人类型    | 字符串表示示例 | 备注                                  |
 * |:----------------:|:-----------:|:-------------------------------------|
 * |      控制台       |   console   |                                      |
 * |      精确群       |   g123456   | 表示群, 而不表示群成员                   |
 * |      精确好友      |   f123456   | 必须通过好友消息                        |
 * |    精确临时会话    | t123456.789  | 群 123456 内的成员 789. 必须通过临时会话  |
 * |     精确群成员     | m123456.789 | 群 123456 内的成员 789. 同时包含临时会话. |
 * |      精确用户      |   u123456   | 同时包含群成员, 好友, 临时会话            |
 * |      任意群       |     g*      |                                      |
 * |  任意群的任意群员   |     m*      |                                      |
 * |  精确群的任意群员   |  m123456.*  | 群 123456 内的任意成员. 同时包含临时会话.  |
 * | 任意群的任意临时会话 |     t*      | 必须通过临时会话                        |
 * | 精确群的任意临时会话 |  t123456.*  | 群 123456 内的任意成员. 必须通过临时会话   |
 * |      任意好友      |     f*      |                                      |
 * |      任意用户      |     u*      | 任何人在任何环境                        |
 * |      任意对象      |      *      | 即任何人, 任何群, 控制台                 |
 *
 *
 * #### 关系图
 *
 * ```
 *          Console                               AnyContact
 *                                                     ↑
 *                                                     |
 *                         +---------------------------+------------------------+
 *                         |                                                    |
 *                      AnyUser                                             AnyGroup
 *                         ↑                                                    ↑
 *                         |                                                    |
 *          +--------------+---------------------+                              |
 *          |              |                     |                              |
 *     AnyFriend           |            AnyMemberFromAnyGroup                   |
 *          ↑              |                     ↑                              |
 *          |              |                     |                              |
 *          |              |            +--------+--------------+               |
 *          |              |            |                       |               |
 *          |              |            |              AnyTempFromAnyGroup      |
 *          |              |            |                       ↑               |
 *          |              |        AnyMember                   |               |
 *          |              |            ↑                       |               |
 *          |          ExactUser        |                       |           ExactGroup
 *          |            ↑   ↑          |                       |
 *          |            |   |          |                       |
 *          +------------+   +----------+                       |
 *          |                           |                       |
 *     ExactFriend                 ExactMember                  |
 *                                      ↑                       |
 *                                      |                       |
 *                                      +-----------------------+
 *                                                              |
 *                                                              |
 *                                                          ExactTemp
 * ```
 */
@Serializable(with = AbstractPermitteeId.AsStringSerializer::class)
public sealed class AbstractPermitteeId(
    public final override vararg val directParents: PermitteeId,
) : PermitteeId {
    public final override fun toString(): String = asString()

    public companion object {
        /**
         * 由 [AbstractPermitteeId.asString] 解析 [AbstractPermitteeId]
         */
        @JvmStatic
        public fun parseFromString(string: String): AbstractPermitteeId = parseFromStringImpl(string)
    }

    /**
     * 使用 [asString] 序列化 [AbstractPermitteeId]
     */
    @ConsoleExperimentalApi
    public object AsStringSerializer : KSerializer<AbstractPermitteeId> by String.serializer().map(
        serializer = AbstractPermitteeId::asString,
        deserializer = ::parseFromString
    )

    /**
     * 表示任何群对象. (不是指群成员, 而是指这个 '群')
     *
     * - **直接父标识符**: [AnyContact]
     * - **间接父标识符**: 无
     * - 字符串表示: "g*"
     *
     * @see AnyMember
     */
    public object AnyGroup : AbstractPermitteeId(AnyContact) {
        override fun asString(): String = "g*"
    }

    /**
     * 表示一个群
     *
     * - **直接父标识符**: [AnyGroup]
     * - **间接父标识符**: [AnyContact]
     * - 字符串表示: "g$groupId"
     */
    public data class ExactGroup(public val groupId: Long) : AbstractPermitteeId(AnyGroup) {
        override fun asString(): String = "g$groupId"
    }

    /**
     * 表示来自一个群的任意一个成员
     *
     * - **直接父标识符**: [AnyMemberFromAnyGroup]
     * - **间接父标识符**: [AnyUser], [AnyContact]
     * - 字符串表示: "m$groupId.*"
     */
    public data class AnyMember(public val groupId: Long) : AbstractPermitteeId(AnyMemberFromAnyGroup) {
        override fun asString(): String = "m$groupId.*"
    }

    /**
     * 表示来自任意群的任意一个成员
     *
     * - **直接父标识符**: [AnyUser]
     * - **间接父标识符**: [AnyContact]
     * - 字符串表示: "m*"
     */
    public object AnyMemberFromAnyGroup : AbstractPermitteeId(AnyUser) {
        override fun asString(): String = "m*"
    }

    /**
     * 表示唯一的一个群成员
     *
     * - **直接父标识符**: [AnyMember], [ExactUser]
     * - **间接父标识符**: [AnyMemberFromAnyGroup], [AnyUser], [AnyContact]
     * - 字符串表示: "m$groupId.$memberId"
     */
    public data class ExactMember(
        public val groupId: Long,
        public val memberId: Long,
    ) : AbstractPermitteeId(AnyMember(groupId), ExactUser(memberId)) {
        override fun asString(): String = "m$groupId.$memberId"
    }

    /**
     * 表示任何好友
     *
     * - **直接父标识符**: [AnyUser]
     * - **间接父标识符**: [AnyContact]
     * - 字符串表示: "f*"
     */
    public object AnyFriend : AbstractPermitteeId(AnyUser) {
        override fun asString(): String = "f*"
    }

    /**
     * 表示唯一的一个好友
     *
     * - **直接父标识符**: [ExactUser]
     * - **间接父标识符**: [AnyUser], [AnyContact]
     * - 字符串表示: "f$id"
     */
    public data class ExactFriend(
        public val id: Long,
    ) : AbstractPermitteeId(ExactUser(id)) {
        override fun asString(): String = "f$id"
    }

    /**
     * 表示任何一个通过一个群 *在临时会话发送消息的* [群成员][Member]
     *
     * - **直接父标识符**: [AnyMember], [AnyTempFromAnyGroup]
     * - **间接父标识符**: [AnyMemberFromAnyGroup], [AnyUser], [AnyContact]
     * - 字符串表示: "t$groupId.*"
     */
    public data class AnyTemp(
        public val groupId: Long,
    ) : AbstractPermitteeId(AnyMember(groupId), AnyTempFromAnyGroup) {
        override fun asString(): String = "t$groupId.*"
    }

    /**
     * 表示任何一个 *在临时会话发送消息的* [群成员][Member]
     *
     * - **直接父标识符**: [AnyUser]
     * - **间接父标识符**: [AnyContact]
     * - 字符串表示: "t*"
     */
    public object AnyTempFromAnyGroup : AbstractPermitteeId(AnyUser) {
        override fun asString(): String = "t*"
    }

    /**
     * 表示唯一的一个 *在临时会话发送消息的* [群成员][Member]
     *
     * - **直接父标识符**: [ExactMember]
     * - **间接父标识符**: [AnyUser], [AnyMember], [ExactUser], [AnyContact]
     * - 字符串表示: "t$groupId.$memberId"
     */
    public data class ExactTemp(
        public val groupId: Long,
        public val memberId: Long,
    ) : AbstractPermitteeId(ExactMember(groupId, memberId)) {
        override fun asString(): String = "t$groupId.$memberId"
    }


    /**
     * 表示任何 [用户][User]
     *
     * - **直接父标识符**: [AnyContact]
     * - **间接父标识符**: 无
     * - 字符串表示: "u*"
     */
    public object AnyUser : AbstractPermitteeId(AnyContact) {
        override fun asString(): String = "u*"
    }

    /**
     * 表示任何 [用户][User]
     *
     * - **直接父标识符**: [AnyUser]
     * - **间接父标识符**: [AnyContact]
     * - 字符串表示: "u$id"
     */
    public data class ExactUser(
        public val id: Long,
    ) : AbstractPermitteeId(AnyUser) {
        override fun asString(): String = "u$id"
    }

    /**
     * 表示任何 [联系对象][Contact]
     *
     * - **直接父标识符**: 无
     * - **间接父标识符**: 无
     * - 字符串表示: "*"
     */
    public object AnyContact : AbstractPermitteeId() {
        override fun asString(): String = "*"
    }

    /**
     * 表示控制台
     *
     * - **直接父标识符**: 无
     * - **间接父标识符**: 无
     * - 字符串表示: "console"
     */
    public object Console : AbstractPermitteeId() {
        override fun asString(): String = "console"
    }
}