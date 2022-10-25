package net.mamoe.mirai.clikt.core

import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.test.Test


class JaroWinkerSimilarityTest {
    @Test
    fun jaroWinklerSimilarity() = forAll(
        row("", "", 1.0),
        row("", "a", 0.0),
        row("a", "", 0.0),
        row("a", "a", 1.0),
        row("aa", "aa", 1.0),
        row("aaapppp", "", 0.0),
        row("fly", "ant", 0.0),
        row("cheeseburger", "cheese fries", 0.91),
        row("frog", "fog", 0.93),
        row("elephant", "hippo", 0.44),
        row("hippo", "elephant", 0.44),
        row("hippo", "zzzzzzzz", 0.0),
        row("hello", "hallo", 0.88)
    ) { s1, s2, expected ->
        jaroWinklerSimilarity(s1, s2) shouldBe (expected plusOrMinus 0.01)
    }
}