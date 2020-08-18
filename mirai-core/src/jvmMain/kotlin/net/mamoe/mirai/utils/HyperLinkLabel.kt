package net.mamoe.mirai.utils

import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JLabel

/**
 * 构造方法中url指代用户需要点击的链接, text为显示的提示内容
 */
internal class HyperLinkLabel constructor(url: String, text: String) : JLabel() {
    init {
        super.setText("<html><a href='$url'>$text</a></html>");
        addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent) {
                try {
                    Desktop.getDesktop().browse(URI(url))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
    }
}
