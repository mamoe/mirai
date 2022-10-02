/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import javax.crypto.KeyAgreement

internal open class JceEcdh : Ecdh<ECPublicKey, ECPrivateKey> {
    protected open fun newECKeyPairGenerator() = KeyPairGenerator.getInstance("EC")
    protected open fun newECKeyFactory() = KeyFactory.getInstance("EC")
    protected open fun newECAlgorithmParameters() = AlgorithmParameters.getInstance("EC")
    protected open fun newECDHKeyAgreement() = KeyAgreement.getInstance("ECDH")

    override fun generateKeyPair(): EcdhKeyPair<ECPublicKey, ECPrivateKey> {
        return newECKeyPairGenerator()
            .apply {
                // AKA. prime256v1
                // But `secp256r1` is more common
                initialize(ECGenParameterSpec("secp256r1"))
            }
            .genKeyPair()
            .let {
                EcdhKeyPair(it.public as ECPublicKey, it.private as ECPrivateKey)
            }
    }

    override fun calculateShareKey(peerKey: ECPublicKey, privateKey: ECPrivateKey): ByteArray {
        return newECDHKeyAgreement().apply {
            init(privateKey)
            doPhase(peerKey, true)
        }.generateSecret()
    }

    override fun importPublicKey(encoded: ByteArray): ECPublicKey {
        val params: ECParameterSpec = newECAlgorithmParameters().apply {
            init(ECGenParameterSpec("secp256r1"))
        }.getParameterSpec(ECParameterSpec::class.java)

        require(encoded[0] == 0x04.toByte()) { "Only uncompressed format is supported" }
        val fieldSize = params.curve.field.fieldSize
        val elementSize = (fieldSize + 7) / 8
        val affineXBytes = ByteArray(elementSize)
        val affineYBytes = ByteArray(elementSize)
        System.arraycopy(encoded, 1, affineXBytes, 0, elementSize)
        System.arraycopy(encoded, elementSize + 1, affineYBytes, 0, elementSize)
        val point = ECPoint(BigInteger(1, affineXBytes), BigInteger(1, affineYBytes))

        val keySpec = ECPublicKeySpec(point, params)
        return newECKeyFactory().generatePublic(keySpec) as ECPublicKey
    }

    override fun exportPublicKey(key: ECPublicKey): ByteArray {
        val point = key.w
        val fieldSize = key.params.curve.field.fieldSize
        val elementSize = (fieldSize + 7) / 8
        val x = point.affineX.toByteArray()
        val y = point.affineY.toByteArray()
        val startOfX = countLeadingZeros(x)
        val startOfY = countLeadingZeros(y)
        val encoded = ByteArray(elementSize * 2 + 1)
        encoded[0] = 0x04 // uncompressed
        System.arraycopy(x, startOfX, encoded, elementSize - x.size + startOfX + 1, x.size - startOfX)
        System.arraycopy(y, startOfY, encoded, encoded.size - y.size + startOfY, y.size - startOfY)
        return encoded
    }

    private fun countLeadingZeros(bytes: ByteArray): Int {
        for (i in bytes.indices) {
            if (bytes[i] != 0.toByte()) {
                return i
            }
        }
        return bytes.size
    }
}
