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
import java.io.File
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
    @JvmStatic
    fun main(args: Array<String>) {
        GlobalScope.launch{
            while (true) {
                delay(1000*60*5)
                System.gc()
            }
        }
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
                CoreUpdator.versionCheck()
            }
            launch {
                ConsoleUpdater.versionCheck(type)
            }
        }
        println("Version check complete, starting Mirai")
        println("Core    :" + CoreUpdator.getCore()!!)
        println("Protocol:" + CoreUpdator.getProtocolLib()!!)
        println("Console :" + ConsoleUpdater.getFile()!! )
        println("Root    :" + System.getProperty("user.dir") + "/")

        val loader = MiraiClassLoader(
            CoreUpdator.getCore()!!,
            CoreUpdator.getProtocolLib()!!,
            ConsoleUpdater.getFile()!!,
            this.javaClass.classLoader
        )
        when(type) {
            CONSOLE_PURE -> {
                loader.loadClass("net.mamoe.mirai.BotFactoryJvm")
                loader.loadClass(
                    "net.mamoe.mirai.console.pure.MiraiConsolePureLoader"
                ).getMethod("load", String::class.java,String::class.java)
                    .invoke(null,CoreUpdator.getCurrentVersion(),ConsoleUpdater.getCurrentVersion())
            }
        }
    }
}

class MiraiClassLoader(
    core:File,
    protocol: File,
    console: File,
    parent: ClassLoader
): URLClassLoader(arrayOf(
    core.toURI().toURL(),
    protocol.toURI().toURL(),
    console.toURI().toURL()
), parent)


object WrapperProperties{
    val contentFile by lazy{
        File(contentPath.absolutePath + "/.wrapper.txt").also {
            if(!it.exists())it.createNewFile()
        }
    }

    var content
        get() = contentFile.readText()
        set(value) = contentFile.writeText(value)


    fun determineConsoleType(
        type:String
    ):String?{
        if(type == CONSOLE_PURE || type == CONSOLE_GRAPHICAL || type == CONSOLE_TERMINAL){
            return type
        }
        return null
    }

}