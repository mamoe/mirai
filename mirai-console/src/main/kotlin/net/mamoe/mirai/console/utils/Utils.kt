package net.mamoe.mirai.console.utils
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
