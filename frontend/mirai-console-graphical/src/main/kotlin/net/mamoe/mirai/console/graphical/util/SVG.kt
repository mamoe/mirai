package net.mamoe.mirai.console.graphical.util

import com.jfoenix.svg.SVGGlyph
import javafx.scene.paint.Color

class SVG {
    companion object {
        var close = SVGGlyph(
            0,
            "CLOSE",
            "M810 274l-238 238 238 238-60 60-238-238-238 238-60-60 238-238-238-238 60-60 238 238 238-238z",
            Color.WHITE
        ).apply { setSize(8.0, 8.0) }
    }
}