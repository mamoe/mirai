/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.channels

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.utils.AtomicBoolean
import net.mamoe.mirai.utils.testFramework.assertCoroutineSuspends
import net.mamoe.mirai.utils.testFramework.assertNoCoroutineSuspension
import kotlin.test.*


class OnDemandChannelTest {
    ///////////////////////////////////////////////////////////////////////////
    // CoroutineScope lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun attachScopeJob() {
        val job = SupervisorJob()
        val channel = OnDemandChannel<Int, Int>(job) {
            fail()
        }
        assertEquals(1, job.children.toList().size)
        channel.close()
    }

    @Test
    fun finishAfterInstantiation() {
        val supervisor = SupervisorJob()
        val channel = OnDemandChannel<Int, Int>(supervisor) {
            fail("ran")
        }
        assertEquals(1, supervisor.children.toList().size)
        val job = supervisor.children.single()
        assertEquals(true, job.isActive)

        channel.close()

        assertEquals(0, supervisor.children.toList().size)
        assertEquals(false, job.isActive)
    }

    @Test
    fun `cancel producer job on finish`() = runTest {
        // Actually, this case won't happen, because producer coroutine will be cancelled on [finish]

        lateinit var job: Job
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            job = currentCoroutineContext()[Job]!!
            emit(1)
            emit(1)
            emit(1)
            emit(1)
            fail()
        }

        channel.expectMore(1)
        channel.receiveOrNull()
        assertTrue { job.isActive }
        channel.close()
        assertFalse { job.isActive }
        yield()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Producer Coroutine — Tickets
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `producer receives initial ticket`() = runTest {
        val channel = OnDemandChannel(currentCoroutineContext()) { initialTicket ->
            assertEquals(1, initialTicket)
            emit(2)
        }

        channel.expectMore(1)
        channel.receiveOrNull()

        channel.close()
    }

    @Test
    fun `producer receives second ticket`() = runTest {
        val channel = OnDemandChannel(currentCoroutineContext()) { initialTicket ->
            assertEquals(1, initialTicket)
            assertEquals(2, emit(3))
        }

        channel.expectMore(1)
        channel.receiveOrNull()
        channel.expectMore(2)

        channel.close()
    }

    @Test
    fun `producer receives third ticket`() = runTest {
        val channel = OnDemandChannel(currentCoroutineContext()) { initialTicket ->
            assertEquals(1, initialTicket)
            assertEquals(2, emit(4))
            assertEquals(3, emit(5))
        }

        channel.expectMore(1)
        channel.receiveOrNull()
        channel.expectMore(2)
        channel.receiveOrNull()
        channel.expectMore(3)

        channel.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Consumer — Receive Correct Values
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `receives correct first value`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            emit(3)
        }

        channel.expectMore(1)
        assertEquals(3, channel.receiveOrNull())
        channel.close()
    }

    @Test
    fun `receives correct second value`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            emit(3)
            emit(4)
        }

        channel.expectMore(1)
        assertEquals(3, channel.receiveOrNull())
        channel.expectMore(2)
        assertEquals(4, channel.receiveOrNull())
        channel.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // expectMore/emit/receiveOrNull
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `producer coroutine won't start until expectMore`() {
        val channel = OnDemandChannel<Int, Int> {
            fail()
        }
        channel.close()
    }

    @Test
    fun `producer coroutine starts iff expectMore`() = runTest {
        var started = false
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            // (1)
            assertEquals(false, started)
            started = true
            yield() // goto (2)
            fail()
        }
        assertFalse { started }
        assertTrue { channel.expectMore(1) } // launches the job, but it won't execute due to single parallelism
        yield() // goto (1)
        // (2)
        assertTrue { started }
        channel.close()
    }

    @Test
    fun `receiveOrNull does not suspend if value is ready`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            emit(1)
        }

        assertTrue { channel.expectMore(1) }
        yield() // run `emit`
        // now value is ready
        assertNoCoroutineSuspension { channel.receiveOrNull() }
        channel.close()
    }

    @Test
    fun `receiveOrNull does suspend if value is not ready`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            yield()
            emit(1)
        }

        assertTrue { channel.expectMore(1) }
        assertCoroutineSuspends { channel.receiveOrNull() }
        channel.close()
    }

    @Test
    fun `emit won't resume unless another expectMore`() = runTest {
        val canResume = AtomicBoolean(false)
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            emit(1)

            if (!canResume.value) fail("Emit should not resume")

            canResume.value = false
        }

        channel.expectMore(1)
        channel.receiveOrNull()
        canResume.value = true
        channel.expectMore(2)
        yield() // run producer
        assertEquals(false, canResume.value)
        channel.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Operation while already finished
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `expectMore and receiveOrNull while already finished just after instantiation`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            fail("Producer should not run")
        }
        channel.close()

        assertFalse { channel.expectMore(1) }
        assertNull(channel.receiveOrNull())
        assertFalse { channel.expectMore(1) }
        assertFalse { channel.expectMore(1) }
        assertNull(channel.receiveOrNull())
        assertNull(channel.receiveOrNull())
    }

    @Test
    fun `expectMore and receiveOrNull while already finished`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            emit(1)
        }

        assertTrue { channel.expectMore(1) }
        assertNotNull(channel.receiveOrNull())
        assertFalse { channel.isClosed }

        assertTrue { channel.expectMore(1) } // `expectMore` don't know if more values are available
        yield() // go to producer
        // now we must know producer has no more value
        assertTrue { channel.isClosed }
        assertNull(channel.receiveOrNull())
        assertFalse { channel.expectMore(1) }
        assertNull(channel.receiveOrNull())
        assertFalse { (channel as CoroutineOnDemandReceiveChannel).getScope().isActive }
    }

    @Test
    fun `emit while already finished`() {
        // Actually, this case won't happen, because producer coroutine will be cancelled on [finish]

        `cancel producer job on finish`()
    }


    @Test
    fun `producer exception closes channel then receiveOrNull throws`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            throw NoSuchElementException("Oops")
        }

        assertTrue { channel.expectMore(1) }
        assertFalse { channel.isClosed }
        assertIs<ChannelState.Producing<*, *>>(channel.state)
        assertFailsWith<ProducerFailureException> {
            println(channel.receiveOrNull())
        }.also {
            assertIs<NoSuchElementException>(it.cause)
        }
        assertTrue { channel.isClosed }

        // The exception looks like this, though I don't know why there are two causes.

        //net.mamoe.mirai.utils.channels.ProducerFailureException: Producer failed to produce a value, see cause
        //	at net.mamoe.mirai.utils.channels.CoroutineOnDemandReceiveChannel.receiveOrNull(OnDemandChannelImpl.kt:164)
        //	at net.mamoe.mirai.utils.channels.CoroutineOnDemandReceiveChannel$receiveOrNull$1.invokeSuspend(OnDemandChannelImpl.kt)
        //	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        //	at kotlinx.coroutines.test.TestBuildersKt.runTest$default(Unknown Source)
        //	at net.mamoe.mirai.utils.channels.OnDemandChannelTest.producer exception(OnDemandChannelTest.kt:273)
        //	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
        //Caused by: java.util.NoSuchElementException: Oops
        //	at net.mamoe.mirai.utils.channels.OnDemandChannelTest$producer exception$1$channel$1.invokeSuspend(OnDemandChannelTest.kt:275)
        //	at net.mamoe.mirai.utils.channels.OnDemandChannelTest$producer exception$1$channel$1.invoke(OnDemandChannelTest.kt)
        //	at net.mamoe.mirai.utils.channels.OnDemandChannelTest$producer exception$1$channel$1.invoke(OnDemandChannelTest.kt)
        //	at net.mamoe.mirai.utils.channels.CoroutineOnDemandReceiveChannel$Producer$1.invokeSuspend(OnDemandChannelImpl.kt:46)
        //	(Coroutine boundary)
        //	at net.mamoe.mirai.utils.channels.CoroutineOnDemandReceiveChannel.receiveOrNull(OnDemandChannelImpl.kt:162)
        //	at net.mamoe.mirai.utils.channels.OnDemandChannelTest$producer exception$1.invokeSuspend(OnDemandChannelTest.kt:280)
        //	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTestCoroutine$2.invokeSuspend(TestBuilders.kt:212)
        //Caused by: java.util.NoSuchElementException: Oops
        //  ...
    }


    @Test
    fun `producer exception closes channel then receiveOrNull throws in Producing state`() = runTest {
        val channel = OnDemandChannel<Int, Int>(currentCoroutineContext()) {
            throw NoSuchElementException("Oops")
        }

        assertTrue { channel.expectMore(1) }
        yield() // fail the channel first
        assertIs<ChannelState.Consuming<*, *>>(channel.state)
        assertFalse { channel.isClosed } // channel won't close until receiveOrNull

        assertFailsWith<ProducerFailureException> {
            println(channel.receiveOrNull())
        }.also {
            assertIs<NoSuchElementException>(it.cause)
        }
        assertTrue { channel.isClosed }
    }
}


private val <T, V> OnDemandReceiveChannel<T, V>.state
    get() = (this as CoroutineOnDemandReceiveChannel<T, V>).getState() 