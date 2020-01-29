package net.mamoe.mirai.utils.cryptor

import net.mamoe.mirai.utils.io.chunkedHexToBytes
import net.mamoe.mirai.utils.io.toUHexString

expect interface ECDHPrivateKey {
    fun getEncoded(): ByteArray
}

expect interface ECDHPublicKey {
    fun getEncoded(): ByteArray
}

expect class ECDHKeyPair {
    val privateKey: ECDHPrivateKey
    val publicKey: ECDHPublicKey

    /**
     * 私匙和固定公匙([initialPublicKey]) 计算得到的 shareKey
     */
    val initialShareKey: ByteArray
}

/**
 * 椭圆曲线密码, ECDH 加密
 */
expect class ECDH(keyPair: ECDHKeyPair) {
    val keyPair: ECDHKeyPair

    /**
     * 由 [keyPair] 的私匙和 [peerPublicKey] 计算 shareKey
     */
    fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray

    companion object {
        /**
         * 由完整的 publicKey ByteArray 得到 [ECDHPublicKey]
         */
        fun constructPublicKey(key: ByteArray): ECDHPublicKey

        /**
         * 生成随机密匙对
         */
        fun generateKeyPair(): ECDHKeyPair

        /**
         * 由一对密匙计算 shareKey
         */
        fun calculateShareKey(privateKey: ECDHPrivateKey, publicKey: ECDHPublicKey): ByteArray
    }

    override fun toString(): String
}

/**
 *
 */
@Suppress("FunctionName")
expect fun ECDH(): ECDH

val initialPublicKey =
    ECDH.constructPublicKey("3046301006072A8648CE3D020106052B8104001F03320004928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8".chunkedHexToBytes())
private val commonHeadFor02 = "302E301006072A8648CE3D020106052B8104001F031A00".chunkedHexToBytes()
private val commonHeadForNot02 = "3046301006072A8648CE3D020106052B8104001F033200".chunkedHexToBytes()
private const val constantHead = "3046301006072A8648CE3D020106052B8104001F03320004"
private val byteArray_04 = byteArrayOf(0x04)

fun ByteArray.adjustToPublicKey(): ECDHPublicKey {
    val head = if(this.size<30) "302E301006072A8648CE3D020106052B8104001F031A00" else "3046301006072A8648CE3D020106052B8104001F03320004"

    return ECDH.constructPublicKey((head + this.toUHexString("")).chunkedHexToBytes())
}