package net.mamoe.mirai.qqandroid.io

abstract class JceStruct {
    abstract fun writeTo(builder: JceOutput)

    interface Factory<out T : JceStruct> {
        fun newInstanceFrom(input: JceInput): T

    }
}