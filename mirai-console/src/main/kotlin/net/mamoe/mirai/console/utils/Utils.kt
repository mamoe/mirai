package net.mamoe.mirai.console.utils
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 执行N次 builder
 * 成功一次就会结束
 * 否则就会throw
 */
@OptIn(ExperimentalContracts::class)
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
inline fun <R> retryCatching(n: Int, block: () -> R): Result<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    require(n >= 0) { "param n for retryCatching must not be negative" }
    var exception: Throwable? = null
    repeat(n){
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            exception?.addSuppressedMirai(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}

@OptIn(ExperimentalContracts::class)
inline fun <T> tryNTimes(n: Int = 2, block: () -> T):T {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    require(n >= 0) { "param n for tryNTimes must not be negative" }
    var last:Exception? = null
    repeat(n){
        try {
            return block.invoke()
        }catch (e:Exception){
            last = e
        }
    }

    //给我编译

    throw last!!
}

@PublishedApi
internal fun Throwable.addSuppressedMirai(e: Throwable) {
    if (e === this) {
        return
    }
    kotlin.runCatching {
        this.addSuppressed(e)
    }
}


/**
 * 两个字符串的近似值
 * 要求前面完全一致
 * 如
 *  XXXXXYYYYY.fuzzyCompare(XXXXXYYY)   = 0.8
 *  XXXXXYYYYY.fuzzyCompare(XXXXXYYYZZ) = 0.8
 */

internal fun String.fuzzyCompare(target: String): Double {
    var step = 0
    if (this == target) {
        return 1.0
    }
    if (target.length > this.length) {
        return 0.0
    }
    for (i in this.indices) {
        if (target.length == i) {
            step--
        }else {
            if (this[i] != target[i]) {
                break
            }
            step++
        }
    }

    if(step == this.length-1){
        return 1.0
    }
    return step.toDouble()/this.length
}

/**
 * 模糊搜索一个List中index最接近target的东西
 */
internal inline fun <T : Any> Collection<T>.fuzzySearch(
    target: String,
    index: (T) -> String
): T? {
    if (this.isEmpty()) {
        return null
    }
    var potential: T? = null
    var rate = 0.0
    this.forEach {
        val thisIndex = index(it)
        if(thisIndex == target){
            return it
        }
        with(thisIndex.fuzzyCompare(target)) {
            if (this > rate) {
                rate = this
                potential = it
            }
        }
    }
    return potential
}

/**
 * 模糊搜索一个List中index最接近target的东西
 * 并且确保target是唯一的
 * 如搜索index为XXXXYY list中同时存在XXXXYYY XXXXYYYY 将返回null
 */
internal inline fun <T : Any> Collection<T>.fuzzySearchOnly(
    target: String,
    index: (T) -> String
): T? {
    if (this.isEmpty()) {
        return null
    }
    var potential: T? = null
    var rate = 0.0
    var collide = 0
    this.forEach {
        with(index(it).fuzzyCompare(target)) {
            println(index(it) + "->" + this)
            if (this > rate) {
                rate = this
                potential = it
            }
            if(this == 1.0){
                collide++
            }
            if(collide > 1){
                return null//collide
            }
        }
    }
    return potential
}


internal fun Group.fuzzySearchMember(nameCardTarget: String): Member? {
    return this.members.fuzzySearchOnly(nameCardTarget) {
        it.nameCard
    }
}