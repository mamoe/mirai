@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.contact

import kotlin.math.pow


@Suppress("ObjectPropertyName")
private val `10EXP6` = 10.0.pow(6).toUInt()


fun GroupId.toInternalId(): GroupInternalId {
    if (this.value <= `10EXP6`) {
        return GroupInternalId(this.value)
    }
    val left: Long = this.value.toString().dropLast(6).toLong()
    val right: Long = this.value.toString().takeLast(6).toLong()

    return GroupInternalId(
        when (left) {
            in 1..10 -> ((left + 202).toString() + right.toString()).toUInt()
            in 11..19 -> ((left + 469).toString() + right.toString()).toUInt()
            in 20..66 -> ((left + 208).toString() + right.toString()).toUInt()
            in 67..156 -> ((left + 1943).toString() + right.toString()).toUInt()
            in 157..209 -> ((left + 199).toString() + right.toString()).toUInt()
            in 210..309 -> ((left + 389).toString() + right.toString()).toUInt()
            in 310..499 -> ((left + 349).toString() + right.toString()).toUInt()
            else -> this.value
        }
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