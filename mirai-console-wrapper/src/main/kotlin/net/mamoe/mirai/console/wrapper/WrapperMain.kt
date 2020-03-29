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
import java.awt.Frame
import java.awt.Panel
import java.awt.TextArea
import java.awt.Toolkit
import java.io.File
import java.lang.StringBuilder
import java.net.URLClassLoader
import java.util.*


val contentPath by lazy {
    File(System.getProperty("user.dir") + "/content/").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

object WrapperMain {
    internal var uiBarOutput = StringBuilder()
    private val uilog = StringBuilder()
    internal fun uiLog(any: Any?){
        if(any!=null) {
            uilog.append(any)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        gc()
        if(args.contains("native") || args.contains("-native")){

            val f = Frame("Mirai-Console Version Check")
            f.isResizable = false
            val srcSize= Toolkit.getDefaultToolkit().screenSize

            val width = 300
            val height = 200

            val p = Panel()
            val textArea = TextArea()
            textArea.isEditable = false
            p.add(textArea)
            p.isVisible = true

            f.setLocation((srcSize.width-width)/2, (srcSize.height-height)/2)
            f.setSize(width, height)
            f.add(p)
            f.isVisible = true

            uiLog("正在进行版本检查\n")

            var uiOpen = true
            GlobalScope.launch {
                while (isActive && uiOpen){
                    delay(16)//60 fps
                    withContext(Dispatchers.Main){
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
            uiLog("版本检查完成\n")
            runBlocking {
                MiraiDownloader.downloadIfNeed(true)
            }
            start(CONSOLE_GRAPHICAL)

        }else{
            preStartInNonNative()
        }
    }


    private fun preStartInNonNative(){
        println("You are running Mirai-Console-Wrapper under " + System.getProperty("user.dir"))
        var type = WrapperProperties.determineConsoleType(WrapperProperties.content)
        if(type!=null){
            println("Starting Mirai Console $type, reset by clear /content/")
        }else{
            println("Please select Console Type")
            println("请选择 Console 版本")
            println("=> Pure       : pure console")
            println("=> Graphical  : [Not Supported Yet] graphical UI except unix")
            println("=> Terminal   : [Not Supported Yet] console in unix")
            val scanner = Scanner(System.`in`)
            while (type == null){
                var input =  scanner.next()
                input  = input.toUpperCase()[0] + input.toLowerCase().substring(1)
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

    private fun start(type: String){
        val loader = MiraiClassLoader(
            CoreUpdater.getProtocolLib()!!,
            ConsoleUpdater.getFile()!!,
            null
        )

        loader.loadClass("net.mamoe.mirai.BotFactoryJvm")

        when (type) {
            CONSOLE_PURE -> {
                loader.loadClass(
                        "net.mamoe.mirai.console.pure.MiraiConsolePureLoader"
                    ).getMethod("load", String::class.java, String::class.java)
                    .invoke(null, CoreUpdater.getCurrentVersion(), ConsoleUpdater.getCurrentVersion())
            }
        }
    }
}



private class MiraiClassLoader(
    protocol: File,
    console: File,
    parent: ClassLoader?
): URLClassLoader(arrayOf(
    protocol.toURI().toURL(),
    console.toURI().toURL()
), parent)


private object WrapperProperties{
    val contentFile by lazy{
        File(contentPath.absolutePath + "/.wrapper.txt").also {
            if(!it.exists())it.createNewFile()
        }
    }

    var content
        get() = contentFile.readText()
        set(value) = contentFile.writeText(value)


    fun determineConsoleType(
        type: String
    ):String?{
        if(type == CONSOLE_PURE || type == CONSOLE_GRAPHICAL || type == CONSOLE_TERMINAL){
            return type
        }
        return null
    }
}

private fun gc(){
    GlobalScope.launch{
        while (true) {
            delay(1000*60*5)
            System.gc()
        }
    }
}