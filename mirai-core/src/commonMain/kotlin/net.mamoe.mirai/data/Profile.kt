@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.data

import io.ktor.util.date.GMTDate

/**
 * 个人资料
 */
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
 */ // 在 Android 中 enum 的使用被严格控制.
inline class Gender(val value: Byte) {
    companion object {
        val SECRET = Gender(0)
        val MALE = Gender(2)
        val FEMALE = Gender(1)
    }
}