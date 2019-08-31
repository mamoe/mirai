package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseFailedPacket(val state: State, input: DataInputStream) : ServerPacket(input) {
    enum class State {
        WRONG_PASSWORD,
        // UNKNOWN,//? 要再次发送某数据包
        RETYPE_PASSWORD,//similar to [WRONG_PASSWORD]
        BLOCKED,//你的帐号存在被盗风险，已进入保护模式
        UNKNOWN_QQ_NUMBER,//你输入的帐号不存在
        DEVICE_LOCK,//设备锁
        TAKEN_BACK,//被回收
        // VERIFICATION_CODE,//需要验证码
        // SUCCEED,
    }

    override fun decode() {
    }
}