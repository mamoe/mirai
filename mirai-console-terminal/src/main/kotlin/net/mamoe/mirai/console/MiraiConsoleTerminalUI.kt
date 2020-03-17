@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.swing.SwingTerminal
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame
import kotlinx.coroutines.*
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.io.jvm.nio.copyTo
import kotlinx.coroutines.io.reader
import kotlinx.io.core.use
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleTerminalUI.LoggerDrawer.cleanPage
import net.mamoe.mirai.console.MiraiConsoleTerminalUI.LoggerDrawer.drawLog
import net.mamoe.mirai.console.MiraiConsoleTerminalUI.LoggerDrawer.redrawLogs
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

/**
 * 此文件不推荐任何人看
 * 可能导致
 *  1：心肌梗死
 *  2：呼吸困难
 *  3：想要重写但是发现改任何一个看似不合理的地方都会崩
 *
 * @author NaturalHG
 *
 */

val String.actualLength: Int get() = this.sumBy { if (it.isChineseChar) 2 else 1 }


fun String.getSubStringIndexByActualLength(widthMax: Int): Int {
    return this.sumBy { if (it.isChineseChar) 2 else 1 }.coerceAtMost(widthMax).coerceAtLeast(2)
}

val Char.isChineseChar: Boolean
    get() {
        return this.toString().isChineseChar
    }

val String.isChineseChar: Boolean
    get() {
        return this.matches(Regex("[\u4e00-\u9fa5]"))
    }


object MiraiConsoleTerminalUI : MiraiConsoleUI {
    const val cacheLogSize = 50
    var mainTitle = "Mirai Console v0.01 Core v0.15"

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        mainTitle = "Mirai Console(Terminal) $consoleVersion $consoleBuild Core $coreVersion"
    }

    override fun pushLog(identity: Long, message: String) {
        log[identity]!!.push(message)
        if (identity == screens[currentScreenId]) {
            drawLog(message)
        }
    }

    // 修改interface之后用来暂时占位
    override fun pushLog(priority: LogPriority, identityStr: String, identity: Long, message: String) {
        this.pushLog(identity, message)
    }

    override fun prePushBot(identity: Long) {
        log[identity] = LimitLinkedQueue(cacheLogSize)
    }

    override fun pushBot(bot: Bot) {
        botAdminCount[bot.uin] = 0
        screens.add(bot.uin)
        drawFrame(this.getScreenName(currentScreenId))
        if (terminal is SwingTerminalFrame) {
            terminal.flush()
        }
    }

    @Volatile
    var requesting = false
    private var requestResult: String? = null
    override suspend fun requestInput(): String {
        requesting = true
        while (requesting) {
            delay(100)//不然会卡死 迷惑吧
        }
        return requestResult!!
    }


    private suspend fun provideInput(input: String) {
        if (requesting) {
            requestResult = input
            requesting = false
        } else {
            MiraiConsole.CommandProcessor.runConsoleCommand(commandBuilder.toString())
        }
    }


    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {
        botAdminCount[identity] = admins.size
    }

    override fun createLoginSolver(): LoginSolver {
        return object : LoginSolver() {
            override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
                val tempFile: File = createTempFile(suffix = ".png").apply { deleteOnExit() }
                withContext(Dispatchers.IO) {
                    tempFile.createNewFile()
                    pushLog(0, "[Login Solver]需要图片验证码登录, 验证码为 4 字母")
                    try {
                        tempFile.writeChannel().apply {
                            writeFully(ByteBuffer.wrap(data))
                            close()
                        }
                        pushLog(0, "请查看文件 ${tempFile.absolutePath}")
                    } catch (e: Exception) {
                        error("[Login Solver]验证码无法保存[Error0001]")
                    }
                }

                lateinit var toLog: String
                tempFile.inputStream().use {
                    val img = ImageIO.read(it)
                    toLog += img?.createCharImg((terminal.terminalSize.columns / 1.5).toInt()) ?: "无法创建字符图片. 请查看文件\n"
                }
                pushLog(0, "$toLog[Login Solver]请输验证码. ${tempFile.absolutePath}")
                return requestInput()
                    .takeUnless { it.isEmpty() || it.length != 4 }
                    .also {
                        pushLog(0, "[Login Solver]正在提交[$it]中...")
                    }
            }

            override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
                pushLog(0, "[Login Solver]需要滑动验证码")
                pushLog(0, "[Login Solver]请在任意浏览器中打开以下链接并完成验证码. ")
                pushLog(0, "[Login Solver]完成后请输入任意字符 ")
                pushLog(0, url)
                return requestInput().also {
                    pushLog(0, "[Login Solver]正在提交中")
                }
            }

            override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
                pushLog(0, "[Login Solver]需要进行账户安全认证")
                pushLog(0, "[Login Solver]该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题")
                pushLog(0, "[Login Solver]完成以下账号认证即可成功登录|理论本认证在mirai每个账户中最多出现1次")
                pushLog(0, "[Login Solver]请将该链接在QQ浏览器中打开并完成认证, 成功后输入任意字符")
                pushLog(0, "[Login Solver]这步操作将在后续的版本中优化")
                pushLog(0, url)
                return requestInput().also {
                    pushLog(0, "[Login Solver]正在提交中...")
                }
            }

        }
    }

    private val log = ConcurrentHashMap<Long, LimitLinkedQueue<String>>().also {
        it[0L] = LimitLinkedQueue(cacheLogSize)
    }

    private val botAdminCount = ConcurrentHashMap<Long, Int>()

    private val screens = mutableListOf(0L)
    private var currentScreenId = 0


    lateinit var terminal: Terminal
    lateinit var textGraphics: TextGraphics

    private var hasStart = false
    private lateinit var internalPrinter: PrintStream
    fun start() {
        if (hasStart) {
            return
        }

        internalPrinter = System.out


        hasStart = true
        val defaultTerminalFactory = DefaultTerminalFactory(internalPrinter, System.`in`, Charset.defaultCharset())
        try {
            terminal = defaultTerminalFactory.createTerminal()
            terminal.enterPrivateMode()
            terminal.clearScreen()
            terminal.setCursorVisible(false)
        } catch (e: Exception) {
            try {
                terminal = SwingTerminalFrame("Mirai Console")
                terminal.enterPrivateMode()
                terminal.clearScreen()
                terminal.setCursorVisible(false)
            } catch (e: Exception) {
                error("can not create terminal")
            }
        }
        textGraphics = terminal.newTextGraphics()

        /*
        var lastRedrawTime = 0L
        var lastNewWidth = 0
        var lastNewHeight = 0

        terminal.addResizeListener(TerminalResizeListener { terminal1: Terminal, newSize: TerminalSize ->
            try {
                if (lastNewHeight == newSize.rows
                    &&
                    lastNewWidth == newSize.columns
                ) {
                    return@TerminalResizeListener
                }
                lastNewHeight = newSize.rows
                lastNewWidth = newSize.columns
                terminal.clearScreen()
                if(terminal !is SwingTerminalFrame) {
                    Thread.sleep(300)
                }
                update()
                redrawCommand()
                redrawLogs(log[screens[currentScreenId]]!!)
            }catch (ignored:Exception){

            }
        })

       */
        var lastJob: Job? = null
        terminal.addResizeListener { _: Terminal, _: TerminalSize ->
            lastJob = GlobalScope.launch {
                try {
                    delay(300)
                    if (lastJob == coroutineContext[Job]) {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        terminal.clearScreen()
                        //inited = false
                        update()
                        redrawCommand()
                        redrawLogs(log[screens[currentScreenId]]!!)
                    }
                } catch (e: Exception) {
                    pushLog(0, "[UI ERROR] ${e.message}")
                }
            }
        }

        if (terminal !is SwingTerminalFrame) {
            System.setOut(PrintStream(object : OutputStream() {
                var builder = java.lang.StringBuilder()
                override fun write(b: Int) {
                    with(b.toChar()) {
                        if (this == '\n') {
                            pushLog(0, builder.toString())
                            builder = java.lang.StringBuilder()
                        } else {
                            builder.append(this)
                        }
                    }
                }
            }))
        }

        System.setErr(System.out)

        try {
            update()
        } catch (e: Exception) {
            pushLog(0, "[UI ERROR] ${e.message}")
        }

        val charList = listOf(',', '.', '/', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '=', '+', '!', ' ')
        thread {
            while (true) {
                try {
                    val keyStroke: KeyStroke = terminal.readInput()

                    when (keyStroke.keyType) {
                        KeyType.ArrowLeft -> {
                            currentScreenId =
                                getLeftScreenId()
                            clearRows(2)
                            cleanPage()
                            update()
                        }
                        KeyType.ArrowRight -> {
                            currentScreenId =
                                getRightScreenId()
                            clearRows(2)
                            cleanPage()
                            update()
                        }
                        KeyType.Enter -> {
                            runBlocking {
                                provideInput(commandBuilder.toString())
                            }
                            emptyCommand()
                        }
                        KeyType.Escape -> {
                            exit()
                        }
                        else -> {
                            if (keyStroke.character != null) {
                                if (keyStroke.character.toInt() == 8) {
                                    deleteCommandChar()
                                }
                                if (keyStroke.character.isLetterOrDigit() || charList.contains(keyStroke.character)) {
                                    addCommandChar(keyStroke.character)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    pushLog(0, "[UI ERROR] ${e.message}")
                }
            }
        }
    }

    private fun getLeftScreenId(): Int {
        var newId = currentScreenId - 1
        if (newId < 0) {
            newId = screens.size - 1
        }
        return newId
    }

    private fun getRightScreenId(): Int {
        var newId = 1 + currentScreenId
        if (newId >= screens.size) {
            newId = 0
        }
        return newId
    }

    private fun getScreenName(id: Int): String {
        return when (screens[id]) {
            0L -> {
                "Console Screen"
            }
            else -> {
                "Bot: ${screens[id]}"
            }
        }
    }


    fun clearRows(row: Int) {
        textGraphics.putString(
            0, row, " ".repeat(
                terminal.terminalSize.columns
            )
        )
    }

    fun drawFrame(
        title: String
    ) {
        val width = terminal.terminalSize.columns
        val height = terminal.terminalSize.rows
        terminal.setBackgroundColor(TextColor.ANSI.DEFAULT)

        textGraphics.foregroundColor = TextColor.ANSI.WHITE
        textGraphics.backgroundColor = TextColor.ANSI.GREEN
        textGraphics.putString((width - mainTitle.actualLength) / 2, 1, mainTitle, SGR.BOLD)
        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
        textGraphics.putString(2, 3, "-".repeat(width - 4))
        textGraphics.putString(2, 5, "-".repeat(width - 4))
        textGraphics.putString(2, height - 4, "-".repeat(width - 4))
        textGraphics.putString(2, height - 3, "|>>>")
        textGraphics.putString(width - 3, height - 3, "|")
        textGraphics.putString(2, height - 2, "-".repeat(width - 4))

        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
        val leftName =
            getScreenName(getLeftScreenId())
        // clearRows(2)
        textGraphics.putString((width - title.actualLength) / 2 - "$leftName << ".length, 2, "$leftName << ")
        textGraphics.foregroundColor = TextColor.ANSI.WHITE
        textGraphics.backgroundColor = TextColor.ANSI.YELLOW
        textGraphics.putString((width - title.actualLength) / 2, 2, title, SGR.BOLD)
        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
        val rightName =
            getScreenName(getRightScreenId())
        textGraphics.putString((width + title.actualLength) / 2 + 1, 2, ">> $rightName")
    }

    fun drawMainFrame(
        onlineBotCount: Number
    ) {
        drawFrame("Console Screen")
        val width = terminal.terminalSize.columns
        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
        clearRows(4)
        textGraphics.putString(2, 4, "|Online Bots: $onlineBotCount")
        textGraphics.putString(
            width - 2 - "Powered By Mamoe Technologies|".actualLength,
            4,
            "Powered By Mamoe Technologies|"
        )
    }

    fun drawBotFrame(
        qq: Long,
        adminCount: Number
    ) {
        drawFrame("Bot: $qq")
        val width = terminal.terminalSize.columns
        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
        clearRows(4)
        textGraphics.putString(2, 4, "|Admins: $adminCount")
        textGraphics.putString(width - 2 - "Add admins via commands|".actualLength, 4, "Add admins via commands|")
    }


    object LoggerDrawer {
        var currentHeight = 6

        fun drawLog(string: String, flush: Boolean = true) {
            val maxHeight = terminal.terminalSize.rows - 4
            val heightNeed = (string.actualLength / (terminal.terminalSize.columns - 6)) + 1
            if (heightNeed - 1 > maxHeight) {
                pushLog(0, "[UI ERROR]: 您的屏幕太小, 有一条超长LOG无法显示")
                return//拒绝打印
            }
            if (currentHeight + heightNeed > maxHeight) {
                cleanPage()//翻页
            }
            if (string.contains("\n")) {
                string.split("\n").forEach { _ ->
                    drawLog(string, false)
                }
            } else {
                val width = terminal.terminalSize.columns - 6
                var x = string
                while (true) {
                    if (x == "") {
                        break
                    }
                    val toWrite = if (x.actualLength > width) {
                        val index = x.getSubStringIndexByActualLength(width)
                        x.substring(0, index).also {
                            x = if (index < x.length) {
                                x.substring(index)
                            } else {
                                ""
                            }
                        }
                    } else {
                        x.also {
                            x = ""
                        }
                    }
                    try {
                        textGraphics.foregroundColor = TextColor.ANSI.GREEN
                        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
                        textGraphics.putString(
                            3,
                            currentHeight, toWrite, SGR.ITALIC
                        )
                    } catch (ignored: Exception) {
                        //
                    }
                    ++currentHeight
                }
            }
            if (flush && terminal is SwingTerminalFrame) {
                terminal.flush()
            }
        }


        fun cleanPage() {
            for (index in 6 until terminal.terminalSize.rows - 4) {
                clearRows(index)
            }
            currentHeight = 6
        }


        fun redrawLogs(toDraw: Queue<String>) {
            //this.cleanPage()
            currentHeight = 6
            var logsToDraw = 0
            var vara = 0
            val toPrint = mutableListOf<String>()
            toDraw.forEach {
                val heightNeed = (it.actualLength / (terminal.terminalSize.columns - 6)) + 1
                vara += heightNeed
                if (currentHeight + vara < terminal.terminalSize.rows - 4) {
                    logsToDraw++
                    toPrint.add(it)
                } else {
                    return@forEach
                }
            }
            toPrint.reversed().forEach {
                drawLog(it, false)
            }
            if (terminal is SwingTerminalFrame) {
                terminal.flush()
            }
        }
    }


    private var commandBuilder = StringBuilder()
    private fun redrawCommand() {
        val height = terminal.terminalSize.rows
        val width = terminal.terminalSize.columns
        clearRows(height - 3)
        textGraphics.foregroundColor = TextColor.ANSI.DEFAULT
        textGraphics.putString(2, height - 3, "|>>>")
        textGraphics.putString(width - 3, height - 3, "|")
        textGraphics.foregroundColor = TextColor.ANSI.WHITE
        textGraphics.backgroundColor = TextColor.ANSI.BLACK
        textGraphics.putString(7, height - 3, commandBuilder.toString())
        if (terminal is SwingTerminalFrame) {
            terminal.flush()
        }
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
    }

    private fun addCommandChar(
        c: Char
    ) {
        if (!requesting && commandBuilder.isEmpty() && c != '/') {
            addCommandChar('/')
        }
        textGraphics.foregroundColor = TextColor.ANSI.WHITE
        textGraphics.backgroundColor = TextColor.ANSI.BLACK
        val height = terminal.terminalSize.rows
        commandBuilder.append(c)
        if (terminal is SwingTerminalFrame) {
            redrawCommand()
        } else {
            textGraphics.putString(6 + commandBuilder.length, height - 3, c.toString())
        }
        textGraphics.backgroundColor = TextColor.ANSI.DEFAULT
    }

    private fun deleteCommandChar() {
        if (commandBuilder.isNotEmpty()) {
            commandBuilder = StringBuilder(commandBuilder.toString().substring(0, commandBuilder.length - 1))
        }
        val height = terminal.terminalSize.rows
        if (terminal is SwingTerminalFrame) {
            redrawCommand()
        } else {
            textGraphics.putString(7 + commandBuilder.length, height - 3, " ")
        }
    }


    private var lastEmpty: Job? = null
    private fun emptyCommand() {
        commandBuilder = StringBuilder()
        if (terminal is SwingTerminal) {
            redrawCommand()
            terminal.flush()
        } else {
            lastEmpty = GlobalScope.launch {
                try {
                    delay(100)
                    if (lastEmpty == coroutineContext[Job]) {
                        withContext(Dispatchers.IO) {
                            terminal.clearScreen()
                        }
                        //inited = false
                        update()
                        redrawCommand()
                        redrawLogs(log[screens[currentScreenId]]!!)
                    }
                } catch (e: Exception) {
                    pushLog(0, "[UI ERROR] ${e.message}")
                }
            }
        }
    }

    private fun update() {
        when (screens[currentScreenId]) {
            0L -> {
                drawMainFrame(screens.size - 1)
            }
            else -> {
                drawBotFrame(
                    screens[currentScreenId],
                    0
                )
            }
        }
        redrawLogs(log[screens[currentScreenId]]!!)
    }

    fun exit() {
        try {
            terminal.exitPrivateMode()
            terminal.close()
            exitProcess(0)
        } catch (ignored: Exception) {

        }
    }
}


class LimitLinkedQueue<T>(
    private val limit: Int = 50
) : ConcurrentLinkedDeque<T>() {
    override fun push(e: T) {
        if (size >= limit) {
            this.pollLast()
        }
        return super.push(e)
    }
}

/**
 * @author NaturalHG
 */
private fun BufferedImage.createCharImg(outputWidth: Int = 100, ignoreRate: Double = 0.95): String {
    val newHeight = (this.height * (outputWidth.toDouble() / this.width)).toInt()
    val tmp = this.getScaledInstance(outputWidth, newHeight, Image.SCALE_SMOOTH)
    val image = BufferedImage(outputWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    fun gray(rgb: Int): Int {
        val r = rgb and 0xff0000 shr 16
        val g = rgb and 0x00ff00 shr 8
        val b = rgb and 0x0000ff
        return (r * 30 + g * 59 + b * 11 + 50) / 100
    }

    fun grayCompare(g1: Int, g2: Int): Boolean =
        kotlin.math.min(g1, g2).toDouble() / kotlin.math.max(g1, g2) >= ignoreRate

    val background = gray(image.getRGB(0, 0))

    return buildString(capacity = height) {

        val lines = mutableListOf<StringBuilder>()

        var minXPos = outputWidth
        var maxXPos = 0

        for (y in 0 until image.height) {
            val builderLine = StringBuilder()
            for (x in 0 until image.width) {
                val gray = gray(image.getRGB(x, y))
                if (grayCompare(gray, background)) {
                    builderLine.append(" ")
                } else {
                    builderLine.append("#")
                    if (x < minXPos) {
                        minXPos = x
                    }
                    if (x > maxXPos) {
                        maxXPos = x
                    }
                }
            }
            if (builderLine.toString().isBlank()) {
                continue
            }
            lines.add(builderLine)
        }
        for (line in lines) {
            append(line.substring(minXPos, maxXPos)).append("\n")
        }
    }
}

// Copied from Ktor CIO
private fun File.writeChannel(
    coroutineContext: CoroutineContext = Dispatchers.IO
): ByteWriteChannel = GlobalScope.reader(CoroutineName("file-writer") + coroutineContext, autoFlush = true) {
    @Suppress("BlockingMethodInNonBlockingContext")
    RandomAccessFile(this@writeChannel, "rw").use { file ->
        val copied = channel.copyTo(file.channel)
        file.setLength(copied) // truncate tail that could remain from the previously written data
    }
}.channel
