/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.console.wrapper

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import kotlinx.coroutines.*
import java.awt.TextArea
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import javax.swing.JFrame
import javax.swing.JPanel


val contentPath by lazy {
    File(System.getProperty("user.dir"), "content").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

val extendedLibraries by lazy {
    val file =
        System.getProperty("mirai.libraries")?.let { File(it) } ?: File(System.getProperty("user.dir"), "libraries")
    file.also { if (!it.exists()) it.mkdirs() }
}

object WrapperCli : CliktCommand(name = "mirai-warpper") {
    private val native by option(
        help = """
        Start in GRAPHICAL mode without command line outputs
        ------------------------------------------
        以图形界面模式启动
    """.trimIndent(),
        envvar = "mirai.wrapper.native"
    ).flag("-n", default = false)

    private val update: VersionUpdateStrategy by option(
        help = """
        Strategy to automatic updates. 
        "KEEP" to stay on the current version;
        "STABLE" to update to the latest stable versions;
        "EA" to update to use the newest features but might not be stable.
        ------------------------------------------
        版本升级策略. "KEEP" 为停留在当前版本; "STABLE" 为更新到最新稳定版; "EA" 为更新到最新预览版.
    """.trimIndent(),
        envvar = "mirai.wrapper.update"
    ).enum<VersionUpdateStrategy>().default(VersionUpdateStrategy.STABLE)

    private val console: ConsoleType by option(
        help = """
        The type of the console to be started. 
        "GRAPHICAL" to use JavaFX graphical UI;
        "TERMINAL" to use terminal UI for Unix;
        "PURE" to use pure CLI.
         ------------------------------------------
         UI 类型. "GRAPHICAL" 为 JavaFX 图形界面; "TERMINAL" 为 Unix 终端界面; "PURE" 为纯命令行.
   """.trimIndent(),
        envvar = "mirai.wrapper.console"
    ).enum<ConsoleType>().default(ConsoleType.Pure)

    override fun run() {

        if (native) {
            val f = JFrame("Mirai-Console Version Check")
            f.setSize(500, 200)
            f.setLocationRelativeTo(null)
            f.isResizable = false

            val p = JPanel()
            f.add(p)
            val textArea = TextArea()
            p.add(textArea)
            textArea.isEditable = false

            f.isVisible = true

            WrapperMain.uiLog("正在进行版本检查\n")
            val dic = System.getProperty("user.dir")
            WrapperMain.uiLog("工作目录: ${dic}\n")
            WrapperMain.uiLog("扩展库目录: ${extendedLibraries}\n")
            WrapperMain.uiLog("若无法启动, 请尝试清除工作目录下/content/文件夹\n")
            var uiOpen = true
            GlobalScope.launch {
                while (isActive && uiOpen) {
                    delay(16)//60 fps
                    withContext(Dispatchers.Main) {
                        textArea.text = WrapperMain.uiLog.toString() + "\n" + WrapperMain.uiBarOutput.toString()
                    }
                }
            }
            runBlocking {
                launch {
                    CoreUpdater.versionCheck(update)
                }
                launch {
                    ConsoleUpdater.versionCheck(ConsoleType.Graphical, update)
                }
            }
            WrapperMain.uiLog("版本检查完成, 启动中\n")

            runBlocking {
                MiraiDownloader.downloadIfNeed(true)
            }
            GlobalScope.launch {
                delay(3000)
                uiOpen = false
                f.isVisible = false
            }

            WrapperMain.start(ConsoleType.Graphical)
        } else {
            WrapperMain.preStartInNonNative(console, update)
        }
    }
}

enum class ConsoleType {
    Graphical,
    Terminal,
    Pure
}

enum class VersionUpdateStrategy {
    KEEP,
    STABLE,
    EA
}

object WrapperMain {
    internal var uiBarOutput = StringBuilder()
    internal val uiLog = StringBuilder()

    internal fun uiLog(any: Any?) {
        if (any != null) {
            uiLog.append(any)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        gc()
        WrapperCli.main(args)
    }


    internal fun preStartInNonNative(defaultType: ConsoleType, strategy: VersionUpdateStrategy) {
        println("You are running Mirai-Console-Wrapper under " + System.getProperty("user.dir"))
        println("All additional libraries are located at $extendedLibraries")

        var type = ConsoleType.values().firstOrNull { it.name.equals(WrapperProperties.content, ignoreCase = true) }
        if (type != null) {
            println("Starting Mirai Console $type, reset by clear /content/")
        } else {
            WrapperProperties.content = defaultType.toString()
            type = defaultType
        }

        println("Starting version check...")
        runBlocking {
            launch {
                CoreUpdater.versionCheck(strategy)
            }
            launch {
                ConsoleUpdater.versionCheck(type, strategy)
            }
        }

        runBlocking {
            MiraiDownloader.downloadIfNeed(false)
        }

        println("Version check complete, starting Mirai")
        println("shadow-Protocol:" + CoreUpdater.getProtocolLib()!!)
        println("Console        :" + ConsoleUpdater.getFile()!!)
        println("Root           :" + System.getProperty("user.dir") + "/")

        start(type)
    }

    internal fun start(type: ConsoleType) {
        val loader = MiraiClassLoader(
            CoreUpdater.getProtocolLib()!!,
            ConsoleUpdater.getFile()!!,
            WrapperMain::class.java.classLoader
        )

        loader.loadClass("net.mamoe.mirai.BotFactoryJvm")
        loader.loadClass(
                when (type) {
                    ConsoleType.Pure -> "net.mamoe.mirai.console.pure.MiraiConsolePureLoader"
                    ConsoleType.Graphical -> "net.mamoe.mirai.console.graphical.MiraiConsoleGraphicalLoader"
                    else -> return
                }
            ).getMethod("load", String::class.java, String::class.java)
            .invoke(null, CoreUpdater.getCurrentVersion(), ConsoleUpdater.getCurrentVersion())

    }
}


private class MiraiClassLoader(
    protocol: File,
    console: File,
    parent: ClassLoader?
) : URLClassLoader(
    arrayOf(
        protocol.toURI().toURL(),
        console.toURI().toURL()
    ), null
) {
    init {
        extendedLibraries.listFiles { file ->
            file.isFile && file.extension == "jar"
        }?.forEach {
            kotlin.runCatching {
                /*
                Confirm that the current jar is valid
                确认当前jar是否有效
                */
                JarFile(it).close()
                addURL(it.toURI().toURL())
            }
        }
    }

    private val parent0: ClassLoader? = parent
    override fun findClass(name: String?): Class<*> {
        return try {
            super.findClass(name)
        } catch (exception: ClassNotFoundException) {
            if (parent0 == null) throw exception
            parent0.loadClass(name)
        }
    }
}


private object WrapperProperties {
    val contentFile by lazy {
        File(contentPath, ".wrapper.txt").also {
            if (!it.exists()) it.createNewFile()
        }
    }

    var content
        get() = contentFile.readText()
        set(value) = contentFile.writeText(value)
}

private fun gc() {
    GlobalScope.launch {
        while (isActive) {
            delay(1000 * 60)
            System.gc()
        }
    }
}
