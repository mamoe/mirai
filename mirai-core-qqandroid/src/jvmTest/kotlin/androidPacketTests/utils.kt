package androidPacketTests

import net.mamoe.mirai.utils.cryptor.decryptBy
import org.bouncycastle.jce.provider.JCEECPrivateKey
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.math.ec.ECConstants
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.interfaces.ECPrivateKey

fun ByteArray.decryptBy16Zero() = this.decryptBy(ByteArray(16))

fun ByteArray.dropTCPHead(): ByteArray = this.drop(16 * 3 + 6).toByteArray()


fun loadPrivateKey(s: String): ECPrivateKey {
    fun fromHex(
        hex: String
    ): BigInteger {
        return BigInteger(1, Hex.decode(hex))
    }

    // p = 2^192 - 2^32 - 2^12 - 2^8 - 2^7 - 2^6 - 2^3 - 1
    // p = 2^192 - 2^32 - 2^12 - 2^8 - 2^7 - 2^6 - 2^3 - 1
    val p = fromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFEE37")
    val a = ECConstants.ZERO
    val b = BigInteger.valueOf(3)
    val n = fromHex("FFFFFFFFFFFFFFFFFFFFFFFE26F2FC170F69466A74DEFD8D")
    val h = BigInteger.valueOf(1)

    val curve: ECCurve = ECCurve.Fp(p, a, b)
    //ECPoint G = curve.decodePoint(Hex.decode("03"
    //+ "DB4FF10EC057E9AE26B07D0280B7F4341DA5D1B1EAE06C7D"));
    //ECPoint G = curve.decodePoint(Hex.decode("03"
//+ "DB4FF10EC057E9AE26B07D0280B7F4341DA5D1B1EAE06C7D"));
    val G = curve.decodePoint(
        Hex.decode(
            "04"
                    + "DB4FF10EC057E9AE26B07D0280B7F4341DA5D1B1EAE06C7D"
                    + "9B2F2F6D9C5628A7844163D015BE86344082AA88D95E2F9D"
        )
    )

    return JCEECPrivateKey(
        "EC",
        ECPrivateKeySpec(
            fromHex(s),
            ECParameterSpec(curve, G, n, h)
        )
    )
    //  return KeyFactory.getInstance("ECDH").generatePrivate(PKCS8EncodedKeySpec(s))
}
