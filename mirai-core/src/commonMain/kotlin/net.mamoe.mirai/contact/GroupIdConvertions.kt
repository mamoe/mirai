@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.contact

fun GroupId.toInternalId(): GroupInternalId {
    if (this.value <= 10.0e6) {
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
            in 67..156 -> plusLeft(1943, 6)
            in 157..209 -> plusLeft(199, 7)
            in 210..309 -> plusLeft(389, 7)
            in 310..499 -> plusLeft(349, 7)
            else -> null
        }?.toLong() ?: this.value
    )
}

fun GroupInternalId.toId(): GroupId = with(value.toString()) {
    if (value < 10.0e6) {
        return GroupId(value)
    }
    val left = this.dropLast(6).toLong()

    return GroupId(
        when (left.toInt()) {
            in 203..212 -> ((left - 202).toString() + this.takeLast(6).toInt().toString()).toLong()
            in 480..488 -> ((left - 469).toString() + this.takeLast(6).toInt().toString()).toLong()
            in 2100..2146 -> ((left.toString().take(3).toLong() - 208).toString() + this.takeLast(7).toInt().toString()).toLong()
            in 2010..2099 -> ((left - 1943).toString() + this.takeLast(6).toInt().toString()).toLong()
            in 2147..2199 -> ((left.toString().take(3).toLong() - 199).toString() + this.takeLast(7).toInt().toString()).toLong()
            in 4100..4199 -> ((left.toString().take(3).toLong() - 389).toString() + this.takeLast(7).toInt().toString()).toLong()
            in 3800..3989 -> ((left.toString().take(3).toLong() - 349).toString() + this.takeLast(7).toInt().toString()).toLong()
            else -> value
        }
    )
}