/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.internal.message.data.OnlineAudioImpl.Companion.DOWNLOAD_URL
import net.mamoe.mirai.internal.message.data.OnlineAudioImpl.Companion.refineUrl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.data.AudioCodec
import net.mamoe.mirai.message.data.OfflineAudio
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class AudioTest : AbstractTest() {

    @Test
    fun `test factory`() {
        assertEquals(
            OfflineAudio("name", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf()),
            OfflineAudio("name", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf())
        )
    }

    @Test
    fun `invalid extraData is refreshed`() {
        assertContentEquals(
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, null).extraData,
            OfflineAudio("test", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf(1, 2, 3)).extraData,
        )
    }

    @Test
    fun `test equality`() {
        assertEquals(
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null),
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null)
        )
        assertEquals(
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null),
            OnlineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, "url", 2, null)
        )
        assertEquals(
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, originalPtt = null),
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, originalPtt = null)
        )
        assertEquals(
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf()),
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, byteArrayOf())
        )
        assertEquals(
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, ImMsgBody.Ptt(srcUin = 2)),
            OfflineAudioImpl("name", byteArrayOf(), 1, AudioCodec.SILK, ImMsgBody.Ptt(srcUin = 2))
        )
    }

    @Test
    fun `test refineUrl`() {
        assertFalse { DOWNLOAD_URL.endsWith("/") }

        assertEquals("", refineUrl(""))
        assertEquals("$DOWNLOAD_URL/test", refineUrl("/test"))
        assertEquals("$DOWNLOAD_URL/test", refineUrl("test"))
        assertEquals("https://custom.com", refineUrl("https://custom.com"))
        assertEquals("http://localhost", refineUrl("http://localhost"))
    }
}