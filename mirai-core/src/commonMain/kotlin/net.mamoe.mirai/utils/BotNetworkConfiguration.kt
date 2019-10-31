package net.mamoe.mirai.utils

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import net.mamoe.mirai.network.protocol.tim.packet.login.TouchResponsePacket
import kotlin.jvm.JvmField

/**
 * 网络和连接配置
 */
class BotNetworkConfiguration {
    /**
     * 等待 [TouchResponsePacket] 的时间
     */
    var touchTimeout: TimeSpan = 2.seconds
    /**
     * 是否使用随机的设备名.
     * 使用随机可以降低被封禁的风险, 但可能导致每次登录都需要输入验证码
     * 当一台设备只登录少量账号时, 将此项设置为 `true` 可能更好.
     */
    var randomDeviceName: Boolean = false
    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    var heartbeatPeriod: TimeSpan = 60.seconds
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    var heartbeatTimeout: TimeSpan = 2.seconds

    companion object {
        /**
         * 默认的配置实例
         */
        @JvmField
        val Default = BotNetworkConfiguration()
    }
}