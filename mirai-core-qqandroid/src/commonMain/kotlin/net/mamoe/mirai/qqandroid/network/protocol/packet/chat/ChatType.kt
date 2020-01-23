package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

/**
 * TROOP仍然不知道是什么
 */
enum class ChatType(val internalID: Int) {

    FRIEND(2),//可以为任何数字

    CONTACT(1006),

    //未知，推测为"组"
    TROOP(1),
    TROOP_HCTOPIC(1026),

    //坦白说
    CONFESS_A(1033),
    CONFESS_B(1034),

    CM_GAME_TEMP(1036),

    DISCUSSION(3000),

    DEVICE_MSG(9501),
}