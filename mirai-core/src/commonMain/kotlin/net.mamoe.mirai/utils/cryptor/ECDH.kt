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

    val shareKey: ByteArray
}

expect class ECDH(keyPair: ECDHKeyPair) {
    val keyPair: ECDHKeyPair

    fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray

    companion object {
        fun constructPublicKey(key: ByteArray): ECDHPublicKey
        fun generateKeyPair(): ECDHKeyPair
        fun calculateShareKey(privateKey: ECDHPrivateKey, publicKey: ECDHPublicKey): ByteArray
    }
}

@Suppress("FunctionName")
expect fun ECDH(): ECDH

internal val initialPublicKey =
    ECDH.constructPublicKey("3046301006072A8648CE3D020106052B8104001F03320004928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8".chunkedHexToBytes())
private val commonHeadFor02 = "302E301006072A8648CE3D020106052B8104001F031A00".chunkedHexToBytes()
private val commonHeadForNot02 = "3046301006072A8648CE3D020106052B8104001F033200".chunkedHexToBytes()
private const val constantHead = "3046301006072A8648CE3D020106052B8104001F03320004"
private val byteArray_04 = byteArrayOf(0x04)

fun ByteArray.adjustToPublicKey(): ECDHPublicKey {
    val key = if (this[0].toInt() == 0x02) { // from server
        commonHeadFor02 + this
    } else if (!this.toUHexString("").startsWith(constantHead)) {
        commonHeadForNot02 +
                if (this[0].toInt() == 0x04) this
                else (byteArray_04 + this)
    } else this

    return ECDH.constructPublicKey(key)
}