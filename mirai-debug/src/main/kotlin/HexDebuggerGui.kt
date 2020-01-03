@file:Suppress("EXPERIMENTAL_API_USAGE")

import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import kotlinx.coroutines.*
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import kotlinx.io.core.readUShort
import net.mamoe.mirai.utils.io.encodeToString
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.readUVarInt
import tornadofx.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor


/**
 * How to run:
 *
 * `gradle :mirai-debug:run`
 */
class Application : App(HexDebuggerGui::class, Styles::class)

class Styles : Stylesheet() {
    companion object {
        // Define css classes
        val heading by cssclass()

        // Define colors
        val mainColor = c("#bdbd22")
    }

    init {
        heading {
            textFill = mainColor
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }

        button {
            padding = box(vertical = 5.px, horizontal = 15.px)
            fontWeight = FontWeight.BOLD
            fontSize = 16.px
        }

        label {
            padding = box(vertical = 5.px, horizontal = 15.px)
            fontSize = 16.px
        }

        textField {
            padding = box(vertical = 5.px, horizontal = 15.px)
        }

        textArea {
            fontSize = 18.px
        }

        val flat = mixin {
            backgroundInsets += box(0.px)
            borderColor += box(Color.DARKGRAY)
        }

        s(button, textInput) {
            +flat
        }
    }
}

class HexDebuggerGui : View("Mirai Hex Debugger") {
    private lateinit var input: TextArea
    private lateinit var outSize: TextField
    private lateinit var outUVarInt: TextField
    private lateinit var outShort: TextField
    private lateinit var outUInt: TextField
    private lateinit var outString: TextArea
    private lateinit var outBits: TextField


    private val clip = Toolkit.getDefaultToolkit().systemClipboard
    private val clipboardContent: String?
        get() {
            val trans = clip.getContents(null)
            if (trans?.isDataFlavorSupported(DataFlavor.stringFlavor) == true) {
                return try {
                    trans.getTransferData(DataFlavor.stringFlavor) as String
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            return null
        }

    init {
        GlobalScope.launch {
            var last = clipboardContent
            while (true) {
                delay(100)

                val current = try {
                    clipboardContent
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                try {
                    if (current != last) {
                        withContext(Dispatchers.Main) {
                            input.text = current
                            updateOutputs(
                                current?.lineSequence()
                                    ?.map { it.substringBefore("//") }
                                    ?.joinToString(" ")
                                    .toString()
                                    .replace("UVarInt", "", ignoreCase = true)
                                    .replace("[", "")
                                    .replace("]", "")
                                    .replace("(", "")
                                    .replace(")", "")
                                    .replace("  ", " ")
                                    .replace("  ", " ")
                                    .replace("  ", " ")
                                    .replace("_", "")
                                    .replace(",", "")
                                    .replace("`", "")
                                    .replace("\"", "")
                                    .replace("'", "")
                                    .replace("*", "")
                                    .replace("\\", "")
                                    .replace("/", "")
                                    .replace("-", "")
                            )
                        }
                    }
                } finally {
                    last = current
                }
            }

        }
    }

    private fun updateOutputs(value: String) {
        outUVarInt.text = runOrNull {
            value.hexToBytes().read {
                readUVarInt().toString()
            }
        }

        outShort.text = runOrNull {
            value.hexToBytes().read {
                readShort().toString()
            }
        }

        outUInt.text = runOrNull {
            value.hexToBytes().read {
                readUInt().toString()
            }
        }

        outSize.text = runOrNull {
            value.hexToBytes().size.toString()
        }

        outBits.text = runOrNull {
            value.hexToBytes().read {
                when (remaining.toInt()) {
                    0 -> null
                    1 -> readUByte().toString(2)
                    2 -> readUShort().toString(2)
                    4 -> readUInt().toString(2)
                    else -> ""
                }
            }
        }

        outString.text = runOrNull {
            value.hexToBytes().encodeToString()
        }
    }

    override val root = hbox {
        //prefWidth = 735.0
        minHeight = 300.0
        prefHeight = minHeight

        input = textarea {
            promptText = "Input"
            paddingVertical = 10

            prefWidth = 150.0
            minWidth = 100.0
            //maxWidth = 150.0
            fitToHeight(this@hbox)
        }

        vbox(10) {
            label(" => ") {
                alignment = Pos.CENTER
                spacing = 10.0
            }

            button("_") {
                paddingAll = 0.0
                setOnMouseClicked {
                    if (primaryStage.isAlwaysOnTop) {
                        primaryStage.isAlwaysOnTop = false
                        text = "_"
                    } else {
                        primaryStage.isAlwaysOnTop = true
                        text = "↑"
                    }
                }
            }

            alignment = Pos.CENTER
            fitToHeight(this@hbox)
        }

        vbox(10) {
            paddingTop = 10
            alignment = Pos.TOP_RIGHT
            label("size")
            label("UVarInt")
            label("short")
            label("uint")
            label("bits")
            label("string")
            children.filterIsInstance<Region>().forEach {
                it.fitToParentWidth()
            }
        }

        vbox(20) {
            alignment = Pos.CENTER_RIGHT

            outSize = textfield {
                promptText = "Size"
                isEditable = false
            }

            outUVarInt = textfield {
                promptText = "UVarInt"
                isEditable = false
            }

            outShort = textfield {
                promptText = "Short"
                isEditable = false
            }

            outUInt = textfield {
                promptText = "UInt"
                isEditable = false
            }

            outBits = textfield {
                promptText = "Bits"
                isEditable = false
            }

            outString = textarea {
                promptText = "String"
                isEditable = false
                maxWidth = 100.0
                minHeight = 30.0
            }

            children.filterIsInstance<Region>().forEach {
                it.fitToParentWidth()
            }
        }

        input.textProperty().onChange {
            if (it == null) {
                return@onChange
            }
            updateOutputs(it)
        }
    }
}


private inline fun <T> runOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (e: Exception) {
        null
    }
}