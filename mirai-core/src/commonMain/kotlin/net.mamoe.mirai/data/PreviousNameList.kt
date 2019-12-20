package net.mamoe.mirai.data

/**
 * 曾用名列表
 *
 * 曾用名可能是:
 * - 昵称
 * - 共同群内的群名片
 */
class PreviousNameList(
    list: List<String>
) : Packet, List<String> by list {
    override fun toString(): String = this.joinToString(prefix = "PreviousNameList(", postfix = ")", separator = ", ")
}