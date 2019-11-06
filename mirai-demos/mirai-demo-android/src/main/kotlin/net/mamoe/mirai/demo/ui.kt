@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.demo

import android.app.Application
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.login
import net.mamoe.mirai.network.protocol.tim.packet.login.requireSuccess
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.SimpleLogger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.properties.Delegates

@Suppress("unused")
private val Throwable.stacktraceString: String
    get() = ByteArrayOutputStream().also { printStackTrace(PrintStream(it)) }.toString()

class MyApplication : Application()

class MainActivity : AppCompatActivity() {
    private var rootLayout: LinearLayout by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootLayout = findViewById(R.id.main_view)


    }

    private suspend fun initializeBot(qq: UInt, password: String) {
        DefaultLogger = {
            PlatformLogger(it) + SimpleLogger { message, e ->
                // TODO: 2019/11/6
            }
        }

        val bot = Bot(qq, password).apply { login().requireSuccess() }

        bot.subscribeFriendMessages {
            "Hello" reply "Hello Mirai!"
        }
    }
}