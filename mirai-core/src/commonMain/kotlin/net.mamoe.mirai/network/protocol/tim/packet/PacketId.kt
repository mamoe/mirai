package net.mamoe.mirai.network.protocol.tim.packet


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class PacketId(
        /**
         * 用于识别的包 ID
         */
        val value: String
)
