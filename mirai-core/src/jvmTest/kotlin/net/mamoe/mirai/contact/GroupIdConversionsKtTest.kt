package net.mamoe.mirai.contact

import net.mamoe.mirai.test.shouldBeEqualTo
import org.junit.Test
import kotlin.random.Random

@UseExperimental(ExperimentalUnsignedTypes::class)
internal class GroupIdConversionsKtTest {

    @Test
    fun checkToInternalId() {
        GroupId(221056495u).toInternalId().value shouldBeEqualTo 4111056495u
        //  61 056495
        //4111 056495
    }


    @Test
    fun toInternalId() {
        repeat(1000000) { _ ->
            val it = Random.nextInt()
            try {
                GroupId(it.toUInt()).toInternalId() shouldBeEqualTo GroupId(it.toUInt()).toInternalIdOld()
            } catch (e: Throwable) {
                println(it)
                throw e
            }
        }
    }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    @Test
    fun toId() {
        repeat(1000000) { _ ->
            val it = Random.nextInt()
            try {
                GroupInternalId(it.toUInt()).toId() shouldBeEqualTo GroupInternalId(it.toUInt()).toIdOld()
            } catch (e: Throwable) {
                println(it)
                throw e
            }
        }
    }


}

@UseExperimental(ExperimentalUnsignedTypes::class)
private fun GroupId.toInternalIdOld(): GroupInternalId {//求你别出错
    val left: Long = this.value.toString().let {
        if (it.length <= 6) {
            return GroupInternalId(this.value)
        }
        it.substring(0, it.length - 6).toLong()
    }
    val right: Long = this.value.toString().let {
        it.substring(it.length - 6).toLong()
    }

    return GroupInternalId(
        when (left) {
            in 1..10 -> {
                ((left + 202).toString() + right.toString()).toUInt()
            }
            in 11..19 -> {
                ((left + 469).toString() + right.toString()).toUInt()
            }
            in 20..66 -> {
                ((left + 208).toString() + right.toString()).toUInt()
            }
            in 67..156 -> {
                ((left + 1943).toString() + right.toString()).toUInt()
            }
            in 157..209 -> {
                ((left + 199).toString() + right.toString()).toUInt()
            }
            in 210..309 -> {
                ((left + 389).toString() + right.toString()).toUInt()
            }
            in 310..499 -> {
                ((left + 349).toString() + right.toString()).toUInt()
            }
            else -> this.value
        }
    )
}

@UseExperimental(ExperimentalUnsignedTypes::class)
private fun GroupInternalId.toIdOld(): GroupId = with(value) {
    //求你别出错
    var left: UInt = this.toString().let {
        if (it.length <= 6) {
            return GroupId(value)
        }
        it.substring(0 until it.length - 6).toUInt()
    }

    return GroupId(when (left.toInt()) {
        in 203..212 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 6).toUInt()
            }
            ((left - 202u).toString() + right.toString()).toUInt()
        }
        in 480..488 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 6).toUInt()
            }
            ((left - 469u).toString() + right.toString()).toUInt()
        }
        in 2100..2146 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 7).toUInt()
            }
            left = left.toString().substring(0 until 3).toUInt()
            ((left - 208u).toString() + right.toString()).toUInt()
        }
        in 2010..2099 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 6).toUInt()
            }
            ((left - 1943u).toString() + right.toString()).toUInt()
        }
        in 2147..2199 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 7).toUInt()
            }
            left = left.toString().substring(0 until 3).toUInt()
            ((left - 199u).toString() + right.toString()).toUInt()
        }
        in 4100..4199 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 7).toUInt()
            }
            left = left.toString().substring(0 until 3).toUInt()
            ((left - 389u).toString() + right.toString()).toUInt()
        }
        in 3800..3989 -> {
            val right: UInt = this.toString().let {
                it.substring(it.length - 7).toUInt()
            }
            left = left.toString().substring(0 until 3).toUInt()
            ((left - 349u).toString() + right.toString()).toUInt()
        }
        else -> value
    })
}