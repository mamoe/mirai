package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.QQ

@Serializable
abstract class ContactDTO : DTO {
    abstract val id: Long
}

@Serializable
data class QQDTO(
    override val id: Long,
    val nickName: String,
    val remark: String
) : ContactDTO()

suspend fun QQDTO(qq: QQ): QQDTO = QQDTO(qq.id, qq.queryProfile().nickname, qq.queryRemark().value)

@Serializable
data class MemberDTO(
    override val id: Long,
    val memberName: String = "",
    val group: GroupDTO,
    val permission: MemberPermission
) : ContactDTO()

fun MemberDTO(member: Member, name: String = ""): MemberDTO = MemberDTO(member.id, name, GroupDTO(member.group), member.permission)

@Serializable
data class GroupDTO(
    override val id: Long,
    val name: String
) : ContactDTO()

fun GroupDTO(group: Group): GroupDTO = GroupDTO(group.id, group.name)