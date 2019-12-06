package japttest;

import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult;
import test.JaptTestKt;

@SuppressWarnings("unused")
public class SuspendTest {

    public static void main(String[] args) {
        // TODO: 2019/12/6 Kotlin or IDE bug here
        boolean bool = JaptTestKt.getLoginResult() == LoginResult.YOU_CAN_WRITE_ANY_THING;

        System.out.println("Hello world finished");

    }
}
