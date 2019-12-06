package japttest;

import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotAccount;
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult;
import net.mamoe.mirai.network.protocol.tim.packet.login.SubmitPasswordPacket;
import test.JaptTestKt;

@SuppressWarnings("unused")
public class SuspendTest {

    public static void main(String[] args) throws InterruptedException {
        // TODO: 2019/12/6 Kotlin or IDE bug here
        boolean bool = JaptTestKt.getLoginResult() == LoginResult.YOU_CAN_WRITE_ANY_THING;

        BuildersKt.runBlocking(GlobalScope.INSTANCE.getCoroutineContext(), (scope, continuation) -> {
            Bot bot = new Bot(new BotAccount(1994701021L, ""), scope.getCoroutineContext());
            if (bot.network.login(continuation) instanceof SubmitPasswordPacket.LoginResponse.Success) {
                System.out.println("login successful");
            }
            return null;
        });

        System.out.println("Hello world finished");

    }
}
