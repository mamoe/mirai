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
    init {
        this._id = PacketId(CommandId("wtlogin.login", 0x0810), 9)
    }

    operator fun invoke(
        client: QQAndroidClient
    ): OutgoingPacket = buildLoginOutgoingPacket(client.account.id.toString()) {
        val appId = 16L
        val subAppId = 537062845L

        writeLoginSsoPacket(client, subAppId, _id.commandId) { ssoSequenceId ->
            writeRequestPacket(client, EncryptMethodECDH135(client.ecdh), _id.commandId) {
                writeShort(9) // subCommand
                writeShort(LoginType.PASSWORD.value.toShort())

                t18(appId, client.appClientVersion, client.account.id)
                t1(client.account.id, client.device.ipAddress)
                t106(
                    appId,
                    subAppId /* maybe 1*/,
                    client.appClientVersion,
                    client.account.id,
                    1,
                    client.account.passwordMd5,
                    0,
                    client.account.id.toByteArray(),
                    client.tgtgtKey,
                    true,
                    client.device.guid,
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
                t116(client.miscBitMap, client.subSigMap)
                t100(appId, subAppId, client.appClientVersion, client.mainSigMap or 0xC0)
                t107(0)

                // t108(byteArrayOf())
                // ignored: t104()
                t142(client.apkId)

                // if login with non-number uin
                // t112()
                t144(
                    androidId = client.device.androidId,
                    androidDevInfo = client.device.generateDeviceInfoData(),
                    osType = client.device.osType,
                    osVersion = client.device.version.release,
                    networkType = client.networkType,
                    simInfo = client.device.simInfo,
                    unknown = byteArrayOf(),
                    apn = client.device.apn,
                    isGuidFromFileNull = false,
                    isGuidAvailable = true,
                    isGuidChanged = false,
                    guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag.NoChange),
                    buildModel = client.device.model,
                    guid = client.device.guid,
                    buildBrand = client.device.brand,
                    tgtgtKey = client.tgtgtKey
                )

                t145(client.device.guid)
                t147(appId, client.apkVersionName, client.apkSignatureMd5)

                if (client.miscBitMap and 0x80 != 0) {
                    t166(1)
                }

                // ignored t16a because array5 is null

                t154(ssoSequenceId)
                t141(client.device.simInfo, client.networkType, client.device.apn)
                t8(2052)

                t511(
                    listOf(
                        "tenpay.com",
                        "openmobile.qq.com",
                        "docs.qq.com",
                        "connect.qq.com",
                        "qzone.qq.com",
                        "vip.qq.com",
                        "qun.qq.com",
                        "game.qq.com",
                        "qqweb.qq.com",
                        "office.qq.com",
                        "ti.qq.com",
                        "mail.qq.com",
                        "qzone.com",
                        "mma.qq.com"
                    )
                )

                // ignored t172 because rollbackSig is null
                // ignored t185 because loginType is not SMS
                // ignored t400 because of first login

                t187(client.device.macAddress)
                t188(client.device.androidId)
                if (client.device.imsiMd5.isNotEmpty()) {
                    t194(client.device.imsiMd5)
                }
                t191()

                /*
                t201(N = byteArrayOf())*/

                val bssid = client.device.wifiBSSID
                val ssid = client.device.wifiSSID
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


    class LoginPacketResponse : Packet

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): LoginPacketResponse {

        TODO()
    }
}


@Suppress("FunctionName")
internal fun PacketId(commandId: CommandId, subCommandId: Int) = object : PacketId {
    override val commandId: CommandId
        get() = commandId
    override val subCommandId: Int
        get() = subCommandId
}

internal interface PacketId {
    val commandId: CommandId // ushort actually
    val subCommandId: Int // ushort actually
}

internal object NullPacketId : PacketId {
    override val commandId: CommandId
        get() = error("uninitialized")
    override val subCommandId: Int
        get() = error("uninitialized")
}
