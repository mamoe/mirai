@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.utils

import com.sun.jna.Library
import com.sun.jna.Native
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import sun.misc.Unsafe

/**
 * @author Him188moe
 */
object ECDH : IECDH by Native.load("ecdhdll64", IECDH::class.java)

interface IECDH : Library {
    //fun encrypt(publicKey: UByteArray, shaKey: UByteArray): UByteArray
    fun encrypt(publicKey: Long, shaKey: Long): Long
}

fun main() {
    //
    // ECDH.encrypt(TIMProtocol.publicKey.hexToUBytes(), TIMProtocol.key0836.hexToUBytes())
    val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").also { it.trySetAccessible() }.get(null) as Unsafe

    val publicKeyAddress = unsafe.allocateMemory(25)
    TIMProtocol.publicKey.hexToUBytes().forEachIndexed { index, value ->
        unsafe.setMemory(publicKeyAddress + index, 1, value.toByte())
    }

    val key0836Address = unsafe.allocateMemory(16)
    TIMProtocol.key0836.hexToUBytes().forEachIndexed { index, value ->
        unsafe.setMemory(key0836Address + index, 1, value.toByte())
    }

    val encrypt = ECDH.encrypt(publicKeyAddress, key0836Address)
    //
    val bytes = mutableListOf<Byte>()
    repeat(16) {
        bytes += unsafe.getByte(encrypt + it)
    }
    println(bytes.toByteArray().toUHexString())
}
