package net.mamoe.mirai.qqandroid.network

class Ticket(
    val id: Int,
    val data: ByteArray,
    val key: ByteArray?,
    val creationTime: Long,
    val expireTime: Long
) {
    companion object {
        const val USER_A5 = 0x2
        const val AQ_SIG = 0x200000
        const val USER_SIG_64 = 0x2000
        const val SUPER_KEY = 0x100000
        const val OPEN_KEY = 0x4000
        const val ACCESS_TOKEN = 0x8000
        const val USER_ST_SIG = 0x80
        const val USER_A8 = 0x10
        const val LS_KEY = 0x200
        const val S_KEY = 0x1000
        const val V_KEY = 0x20000
        const val TGT = 0x40
        const val D2 = 0x40000
        const val SID = 0x80000
        const val USER_ST_WEB_SIG = 0x20
        const val PAY_TOKEN = 0x800000
        const val PF = 0x1000000
        const val DA2 = 0x2000000
    }
}