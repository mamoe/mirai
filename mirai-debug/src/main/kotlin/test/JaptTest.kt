package test

import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult

@Suppress("RedundantSuspendModifier")
suspend fun suspendPrintln(arg: String) = println(arg)

suspend fun main() {
    suspendPrintln("Hello")
    suspendPrintln(" World!")
}

fun getLoginResult(): LoginResult = LoginResult.SUCCESS