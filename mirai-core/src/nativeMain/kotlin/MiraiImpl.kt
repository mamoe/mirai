/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.utils.MiraiCoreServices


/**
 * 初始化 Mirai Native 平台. 必须先调用此 API, 其他 API 才能正常工作.
 */
public fun initMirai() {
    _MiraiImpl_static_init()
}


private val initialized = atomic(false)

@Suppress("FunctionName")
internal actual fun _MiraiImpl_static_init() {
    if (!initialized.compareAndSet(expect = false, update = true)) return
    MiraiCoreServices.registerAll()
    MessageProtocolFacade.INSTANCE // register serializers
}
