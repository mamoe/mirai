package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXDecorator
import tornadofx.View

class Decorator : View() {

    override val root = JFXDecorator(primaryStage, find<PrimaryView>().root)
}