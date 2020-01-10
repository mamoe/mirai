package net.mamoe.mirai.qqandroid.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Inline the block
 */
@UseExperimental(ExperimentalContracts::class)
@PublishedApi
internal inline fun <R> inline(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}