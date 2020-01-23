package net.mamoe.mirai.qqandroid.network.io

abstract class JceStruct {
    abstract fun writeTo(builder: JceOutput)

    interface Factory<out T : JceStruct> {
        fun newInstanceFrom(input: JceInput): T
    }
}