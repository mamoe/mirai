/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.data

/*
/**
 * 个人资料
 */
@MiraiExperimentalAPI
@Suppress("PropertyName")
data class Profile(
    val qq: Long,
    val nickname: String,
    val englishName: String?,
    val chineseName: String?,
    val qAge: Int?, // q 龄
    val zipCode: String?,
    val phone: String?,
    val gender: Gender,
    val birthday: GMTDate?,
    val personalStatement: String?,// 个人说明
    val school: String?,
    val homepage: String?,
    val email: String?,
    val company: String?
) {

    override fun toString(): String = "Profile(qq=$qq, " +
            "nickname=$nickname, " +
            "gender=$gender, " +
            (englishName?.let { "englishName=$englishName, " } ?: "") +
            (chineseName?.let { "chineseName=$chineseName, " } ?: "") +
            (qAge?.toString()?.let { "qAge=$qAge, " } ?: "") +
            (zipCode?.let { "zipCode=$zipCode, " } ?: "") +
            (phone?.let { "phone=$phone, " } ?: "") +
            (birthday?.toString()?.let { "birthday=$birthday, " } ?: "") +
            (personalStatement?.let { "personalStatement=$personalStatement, " } ?: "") +
            (school?.let { "school=$school, " } ?: "") +
            (homepage?.let { "homepage=$homepage, " } ?: "") +
            (email?.let { "email=$email, " } ?: "") +
            (company?.let { "company=$company," } ?: "") +
            ")"// 最终会是 ", )", 但这并不影响什么.
}

/**
 * 性别
 */
@MiraiExperimentalAPI
enum class Gender(val value: Byte) {
    SECRET(0),
    MALE(1),
    FEMALE(2)
}*/