/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.Either.Companion.flatMapNull
import net.mamoe.mirai.utils.Either.Companion.fold
import net.mamoe.mirai.utils.Either.Companion.ifLeft
import net.mamoe.mirai.utils.Either.Companion.ifRight
import net.mamoe.mirai.utils.Either.Companion.left
import net.mamoe.mirai.utils.Either.Companion.leftOrNull
import net.mamoe.mirai.utils.Either.Companion.mapLeft
import net.mamoe.mirai.utils.Either.Companion.mapRight
import net.mamoe.mirai.utils.Either.Companion.onLeft
import net.mamoe.mirai.utils.Either.Companion.onRight
import net.mamoe.mirai.utils.Either.Companion.right
import net.mamoe.mirai.utils.Either.Companion.rightOrNull
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class EitherTest {

    @Test
    fun `type check`() {
        Either<CharSequence, Int>("")
        Either<CharSequence, Int>(1)

        assertFailsWith<IllegalArgumentException> { Either.invoke<CharSequence, String>("") }
        assertFailsWith<IllegalArgumentException> { Either.invoke<String, CharSequence>("") }
    }

    @Test
    fun `type variance`() {
        val p: Either<CharSequence, Int> = Either("") // type is <String, Int>
        assertIs<String>(p.left)
    }

    @Test
    fun `get left`() {
        assertEquals("", Either<CharSequence, Int>("").leftOrNull)
        assertEquals(null, Either<CharSequence, Int>(1).leftOrNull)

        assertFailsWith<ClassCastException> { Either<CharSequence, Int>(1).left }
    }

    @Test
    fun `get right`() {
        assertEquals(null, Either<CharSequence, Int>("").rightOrNull)
        assertEquals(1, Either<CharSequence, Int>(1).rightOrNull)

        assertFailsWith<ClassCastException> { Either<CharSequence, Int>("").right }
    }

    @Test
    fun `can fold`() {
        assertEquals(
            true,
            Either<CharSequence, Int>("").fold(
                onLeft = { true },
                onRight = { false }
            )
        )
        assertEquals(
            false,
            Either<CharSequence, Int>(1).fold(
                onLeft = { true },
                onRight = { false }
            )
        )
    }

    @Test
    fun `can map left`() {
        assertTypeIs(typeOf<Either<Boolean, Int>>(), Either<CharSequence, Int>("").mapLeft { true })

        // left is not null, so block will be called
        assertEquals(true, Either<CharSequence, Int>("").mapLeft { true }.leftOrNull)
        assertEquals(null, Either<CharSequence, Int>("").mapLeft { true }.rightOrNull)

        // right is null, so map will also be null
        assertEquals(
            null,
            Either<CharSequence, Int>(1)
                .mapLeft<CharSequence, Int, Boolean> {
                    throw AssertionError("should not be called")
                }.leftOrNull
        )
        assertEquals(
            1,
            Either<CharSequence, Int>(1)
                .mapLeft<CharSequence, Int, Boolean> {
                    throw AssertionError("should not be called")
                }.rightOrNull
        )
    }

    @Test
    fun `can map right`() {
        assertTypeIs(typeOf<Either<CharSequence, Boolean>>(), Either<CharSequence, Int>(1).mapRight { true })

        // right is not null, so block will be called
        assertEquals(null, Either<CharSequence, Int>(1).mapRight { true }.leftOrNull)
        assertEquals(true, Either<CharSequence, Int>(1).mapRight { true }.rightOrNull)

        // right is null, so map will also be null
        assertEquals(
            null,
            Either<CharSequence, Int>("")
                .mapRight<CharSequence, Int, Boolean> {
                    throw AssertionError("should not be called")
                }.rightOrNull
        )
        assertEquals(
            "",
            Either<CharSequence, Int>("")
                .mapRight<CharSequence, Int, Boolean> {
                    throw AssertionError("should not be called")
                }.leftOrNull
        )
    }

    @Test
    fun `can flatMapNull`() {
        assertTypeIs(typeOf<Either<CharSequence, Int>>(), Either<CharSequence, Int>(1))

        // not null types
        Either<CharSequence, Int>("left").run {
            val result = flatMapNull { throw AssertionError("Fail") }
            assertEquals(this, result) // don't assertSame: arguments boxed separately
        }

        Either<CharSequence, Int>(1).run {
            val result = flatMapNull { throw AssertionError("Fail") }
            assertEquals(this, result) // don't assertSame: arguments boxed separately
        }

        // nullable types
        Either<CharSequence, Int?>("left").run {
            val result = flatMapNull { throw AssertionError("Fail") }
            assertEquals(this, result) // don't assertSame: arguments boxed separately
        }

        Either<CharSequence, Int?>(1).run {
            val result = flatMapNull { throw AssertionError("Fail") }
            assertEquals(this, result) // don't assertSame: arguments boxed separately
        }

        // normal case
        Either<CharSequence, Int?>(null).run {
            val result = flatMapNull { right(true) } // can interlace type
            assertEquals(true, result.right)
        }
    }

    @Test
    fun `can call onRight`() {
        var called = false
        Either<CharSequence, Int>(1).onRight { called = true }
        assertEquals(true, called)

        Either<CharSequence, Int>("").onRight {
            throw AssertionError("should not be called")
        }
    }

    @Test
    fun `can call onLeft`() {
        var called = false
        Either<CharSequence, Int>("").onLeft { called = true }
        assertEquals(true, called)

        Either<CharSequence, Int>(1).onLeft {
            throw AssertionError("should not be called")
        }
    }

    @Test
    fun `can call ifRight`() {
        assertEquals(true, Either<CharSequence, Int>(1).ifRight { true })
        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
        Either<CharSequence, Int>("").ifRight { throw AssertionError("should not be called") }
    }

    @Test
    fun `can call ifLeft`() {
        assertEquals(true, Either<CharSequence, Int>("").ifLeft { true })
        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
        Either<CharSequence, Int>(1).ifLeft { throw AssertionError("should not be called") }
    }

    private inline fun <reified V> assertTypeIs(expected: KType, @Suppress("UNUSED_PARAMETER") value: V) {
        assertEquals(expected, typeOf<V>())
    }
}