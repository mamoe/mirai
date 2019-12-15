@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.contact

import kotlin.math.pow


@Suppress("ObjectPropertyName")
private val `10EXP6` = 10.0.pow(6).toUInt()


fun GroupId.toInternalId(): GroupInternalId {
    if (this.value <= `10EXP6`) {
        return GroupInternalId(this.value)
    }
    val stringValue = this.value.toString()

    fun plusLeft(leftIncrement: Int, rightLength: Int): String =
        stringValue.let { (it.dropLast(rightLength).toLong() + leftIncrement).toString() + it.takeLast(rightLength) }

    return GroupInternalId(
        when (stringValue.dropLast(6).toInt()) {
            in 1..10 -> plusLeft(202, 6)
            in 11..19 -> plusLeft(469, 6)
            in 20..66 -> plusLeft(208, 7)
            in 67..156 ->  plusLeft(1943, 6)
            in 157..209 -> plusLeft(1997, 7)
            in 210..309 -> plusLeft(389, 7)
            in 310..499 -> plusLeft(349, 7)
            else -> null
        }?.toUInt() ?: this.value
    )
}

fun GroupInternalId.toId(): GroupId = with(value.toString()) {
    if (value < `10EXP6`) {
        return GroupId(value)
    }
    val left: UInt = this.dropLast(6).toUInt()

    return GroupId(
        when (left.toInt()) {
            in 203..212 -> ((left - 202u).toString() + this.takeLast(6).toInt().toString()).toUInt()
            in 480..488 -> ((left - 469u).toString() + this.takeLast(6).toInt().toString()).toUInt()
            in 2100..2146 -> ((left.toString().take(3).toUInt() - 208u).toString() + this.takeLast(7).toInt().toString()).toUInt()
            in 2010..2099 -> ((left - 1943u).toString() + this.takeLast(6).toInt().toString()).toUInt()
            in 2147..2199 -> ((left.toString().take(3).toUInt() - 199u).toString() + this.takeLast(7).toInt().toString()).toUInt()
            in 4100..4199 -> ((left.toString().take(3).toUInt() - 389u).toString() + this.takeLast(7).toInt().toString()).toUInt()
            in 3800..3989 -> ((left.toString().take(3).toUInt() - 349u).toString() + this.takeLast(7).toInt().toString()).toUInt()
            else -> value
        }
    )
}