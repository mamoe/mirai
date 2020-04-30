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

import kotlinx.coroutines.*
import java.awt.TextArea
import java.io.File
import java.net.URLClassLoader
import java.util.*
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

object WrapperMain {
    internal var uiBarOutput = StringBuilder()
    private val uilog = StringBuilder()
    internal fun uiLog(any: Any?) {
        if (any != null) {
            uilog.append(any)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        gc()
        if (args.contains("native") || args.contains("-native")) {
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

            uiLog("正在进行版本检查\n")
            val dic = System.getProperty("user.dir")
            uiLog("工作目录: ${dic}\n")
            uiLog("扩展库目录: ${extendedLibraries}\n")
            uiLog("若无法启动, 请尝试清除工作目录下/content/文件夹\n")
            var uiOpen = true
            GlobalScope.launch {
                while (isActive && uiOpen) {
                    delay(16)//60 fps
                    withContext(Dispatchers.Main) {
                        textArea.text = uilog.toString() + "\n" + uiBarOutput.toString()
                    }
                }
            }
            runBlocking {
                launch {
                    CoreUpdater.versionCheck()
                }
                launch {
                    ConsoleUpdater.versionCheck(CONSOLE_GRAPHICAL)
                }
            }
            uiLog("版本检查完成, 启动中\n")

            runBlocking {
                MiraiDownloader.downloadIfNeed(true)
            }
            GlobalScope.launch {
                delay(3000)
                uiOpen = false
                f.isVisible = false
            }

            start(CONSOLE_GRAPHICAL)
        } else {
            preStartInNonNative()
        }
    }


    private fun preStartInNonNative() {
        println("You are running Mirai-Console-Wrapper under " + System.getProperty("user.dir"))
        println("All additional libraries are located at $extendedLibraries")
        var type = WrapperProperties.determineConsoleType(WrapperProperties.content)
        if (type != null) {
            println("Starting Mirai Console $type, reset by clear /content/")
        } else {
            println("Please select Console Type")
            println("请选择 Console 版本")
            println("=> Pure       : pure console")
            println("=> Graphical  : graphical UI except unix")
            println("=> Terminal   : [Not Supported Yet] console in unix")
            val scanner = Scanner(System.`in`)
            while (type == null) {
                var input = scanner.next()
                input = input.toUpperCase()[0] + input.toLowerCase().substring(1)
                println("Selecting $input")
                type = WrapperProperties.determineConsoleType(input)
            }
            WrapperProperties.content = type
        }
        println("Starting version check...")
        runBlocking {
            launch {
                CoreUpdater.versionCheck()
            }
            launch {
                ConsoleUpdater.versionCheck(type)
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

    private fun start(type: String) {
        val loader = MiraiClassLoader(
            CoreUpdater.getProtocolLib()!!,
            ConsoleUpdater.getFile()!!,
            WrapperMain::class.java.classLoader
        )

        loader.loadClass("net.mamoe.mirai.BotFactoryJvm")
        loader.loadClass(
            when (type) {
                CONSOLE_PURE -> "net.mamoe.mirai.console.pure.MiraiConsolePureLoader"
                CONSOLE_GRAPHICAL -> "net.mamoe.mirai.console.graphical.MiraiConsoleGraphicalLoader"
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


    fun determineConsoleType(
        type: String
    ): String? {
        if (type == CONSOLE_PURE || type == CONSOLE_GRAPHICAL || type == CONSOLE_TERMINAL) {
            return type
        }
        return null
    }
}

private fun gc() {
    GlobalScope.launch {
        while (isActive) {
            delay(1000 * 60)
            System.gc()
        }
    }
}
