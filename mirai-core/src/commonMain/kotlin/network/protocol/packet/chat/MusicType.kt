package net.mamoe.mirai.internal.network.protocol.packet.chat

enum class MusicType constructor(val appID: Long, val platform: Int, val sdkVersion: String, val packageName: String, val signature: String) {
    CloudMusic(
            100495085,
            1,
            "0.0.0",
            "com.netease.cloudmusic",
            "da6b069da1e2982db3e386233f68d76d"
    ),
    QQMusic(
            100497308,
            1,
            "0.0.0",
            "com.tencent.qqmusic",
            "cbd27cd7c861227d013a25b2d10f0799"
    ),
    MiguMusic(
            1101053067,
            1,
            "0.0.0",
            "cmccwm.mobilemusic",
            "6cdc72a439cef99a3418d2a78aa28c73"
    )
}