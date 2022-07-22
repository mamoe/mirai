/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.utils.decodeBase64
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

internal val publicKeyForVerify by lazy {
    KeyFactory.getInstance("RSA")
        .generatePublic(X509EncodedKeySpec("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJTW4abQJXeVdAODw1CamZH4QJZChyT08ribet1Gp0wpSabIgyKFZAOxeArcCbknKyBrRY3FFI9HgY1AyItH8DOUe6ajDEb6c+vrgjgeCiOiCVyum4lI5Fmp38iHKH14xap6xGaXcBccdOZNzGT82sPDM2Oc6QYSZpfs8EO7TYT7KSB2gaHz99RQ4A/Lel1Vw0krk+DescN6TgRCaXjSGn268jD7lOO23x5JS1mavsUJtOZpXkK9GqCGSTCTbCwZhI33CpwdQ2EHLhiP5RaXZCio6lksu+d8sKTWU1eEiEb3cQ7nuZXLYH7leeYFoPtbFV4RicIWp0/YG+RP7rLPCwIDAQAB".decodeBase64()))
}

internal actual fun QQEcdhInitialPublicKey.verify(sign: String): Boolean {
    val arrayForVerify = "305$version$keyStr".toByteArray()
    val signInstance = Signature.getInstance("SHA256WithRSA").apply {
        initVerify(publicKeyForVerify)
        update(arrayForVerify)
    }
    return signInstance.verify(sign.decodeBase64())
}
