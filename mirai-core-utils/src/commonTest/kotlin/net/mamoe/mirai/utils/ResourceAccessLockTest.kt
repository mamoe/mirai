/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.*

internal class ResourceAccessLockTest {
    @Test
    fun testInitializedLockCannotReInit() {
        val lock = ResourceAccessLock()
        lock.setInitialized()
        assertFalse { lock.tryInitialize() }
    }

    @Test
    fun testUseFailedIfLockUninitializedOrLocked() {
        val lock = ResourceAccessLock()
        lock.setUninitialized()
        assertFalse { lock.tryUse() }
        lock.setLocked()
        assertFalse { lock.tryUse() }
    }

    @Test
    fun testLockFailedIfUninitialized() {
        val lock = ResourceAccessLock()
        lock.setUninitialized()
        assertFalse { lock.lockIfNotUsing() }
    }

    @Test
    fun testLockFailedIfUsing() {
        val lock = ResourceAccessLock()
        lock.setInitialized()
        assertTrue { lock.tryUse() }
        assertFalse { lock.lockIfNotUsing() }
    }

    @Test
    fun testLockUsedIfInitialized() {
        val lock = ResourceAccessLock()
        lock.setInitialized()
        assertTrue { lock.tryUse() }
    }

    @Test
    fun testRelease() {
        val lock = ResourceAccessLock()
        lock.setInitialized()
        assertFails { lock.release() }
        assertEquals(ResourceAccessLock.INITIALIZED, lock.currentStatus())
        assertTrue { lock.tryUse() }
        lock.release()
    }
}
