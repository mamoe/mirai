package net.mamoe.mirai.qqandroid.network.protocol.packet.login


import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.utils.GuidSource
import net.mamoe.mirai.qqandroid.utils.MacOrAndroidIdChangeFlag
import net.mamoe.mirai.qqandroid.utils.guidFlag
import net.mamoe.mirai.utils.cryptor.DecrypterByteArray
import net.mamoe.mirai.utils.cryptor.DecrypterType
import net.mamoe.mirai.utils.io.toByteArray

class LoginPacketDecrypter(override val value: ByteArray) : DecrypterByteArray {
    companion object : DecrypterType<LoginPacketDecrypter>
}

@UseExperimental(ExperimentalUnsignedTypes::class)
internal object LoginPacket : PacketFactory<LoginPacket.LoginPacketResponse, LoginPacketDecrypter>(LoginPacketDecrypter) {

    operator fun invoke(
        client: QQAndroidClient
    ): OutgoingPacket = buildOutgoingPacket(client, EncryptMethod.ByECDH135) {
        writeECDHEncryptedPacket(client.ecdh) {
            writeShort(9) // subCommand
            writeShort(LoginType.PASSWORD.value.toShort())

            client.run {
                client.device.run {
                    val appId = 16L
                    val subAppId = 2L

                    t18(appId, appClientVersion, account.id)
                    t1(account.id, ipAddress)
                    t106(
                        appId,
                        subAppId /* maybe 1*/,
                        appClientVersion,
                        account.id,
                        ipAddress,
                        1,
                        account.passwordMd5,
                        0,
                        account.id.toByteArray(),
                        tgtgtKey,
                        true,
                        guid,
                        LoginType.PASSWORD
                    )

                    /* // from GetStWithPasswd
                    int mMiscBitmap = this.mMiscBitmap;
                    if (t.uinDeviceToken) {
                        mMiscBitmap = (this.mMiscBitmap | 0x2000000);
                    }


                    // defaults true
                    if (ConfigManager.get_loginWithPicSt()) appIdList = longArrayOf(1600000226L)
                    */
                    t116(miscBitMap, subSigMap, longArrayOf(1600000226L))
                    t100(appId, subAppId, appClientVersion, mainSigMap or 0xC0)
                    t107(0)
                    t108(byteArrayOf())
                    // ignored: t104()
                    t142(apkId)

                    // if login with non-number uin
                    // t112()
                    t144(
                        androidId = androidId,
                        androidDevInfo = generateDeviceInfoData(),
                        osType = osType,
                        osVersion = version.release,
                        ipv6NetType = ipv6NetType,
                        simInfo = simInfo,
                        unknown = byteArrayOf(), apn = apn,
                        isGuidFromFileNull = false,
                        isGuidAvailable = true,
                        isGuidChanged = false,
                        guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag.NoChange),
                        buildModel = model,
                        guid = guid,
                        buildBrand = brand,
                        tgtgtKey = tgtgtKey
                    )

                    t145(guid)
                    t147(appId, apkVersionName, apkSignatureMd5)

                    if (miscBitMap and 0x80 != 0) {
                        t166(1)
                    }

                    // ignored t16a because array5 is null

                    t154(ssoSequenceId)
                    t141(simInfo, ipv6NetType, apn)
                    t8(2052)

                    // ignored t511 because domain is null
                    // ignored t172 because rollbackSig is null
                    // ignored t185 because loginType is not SMS
                    // ignored t400 because of first login

                    t187(macAddress)
                    t188(androidId)
                    if (imsiMd5.isNotEmpty()) {
                        t194(imsiMd5)
                    }
                    t191()
                    t201(N = byteArrayOf())

                    val bssid = wifiBSSID
                    val ssid = wifiSSID
                    if (bssid != null && ssid != null) {
                        t202(bssid, ssid)
                    }

                    t177()
                    t516()
                    t521()

                    t525(buildPacket {
                        t536(buildPacket {
                            //com.tencent.loginsecsdk.ProtocolDet#packExtraData
                            writeByte(1) // const
                            writeByte(0) // data count
                        }.readBytes())
                    })

                    // ignored t318 because not logging in by QR
                }
            }
        }

    }


    class LoginPacketResponse : Packet

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): LoginPacketResponse {

        TODO()
    }
}

interface PacketId {
    val commandId: Int // ushort actually
    val subCommandId: Int // ushort actually
}

object NullPacketId : PacketId {
    override val commandId: Int
        get() = error("uninitialized")
    override val subCommandId: Int
        get() = error("uninitialized")
}
