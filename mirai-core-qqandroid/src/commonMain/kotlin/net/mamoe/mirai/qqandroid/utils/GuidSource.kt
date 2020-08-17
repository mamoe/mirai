/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Utils")
@file:JvmMultifileClass

package net.mamoe.mirai.qqandroid.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * GUID 来源
 *
 * 0: 初始值;
 * 1: 以前保存的文件;
 * 20: 以前没保存且现在生成失败;
 * 17: 以前没保存但现在生成成功;
 */
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
internal inline class GuidSource private constructor(val id: Long) { // uint actually
    companion object {
        /**
         * 初始值
         */
        @JvmStatic
        val STUB = GuidSource(0)
        /**
         *
         */
        @JvmStatic
        val NEWLY_GENERATED = GuidSource(17)
        /**
         * 以前没保存但现在生成成功
         */
        @JvmStatic
        val FROM_STORAGE = GuidSource(1)
        /**
         * 以前没保存且现在生成失败
         */
        @JvmStatic
        val UNAVAILABLE = GuidSource(20)
    }
}

/**
 * ```java
 * GUID_FLAG = 0;
 * GUID_FLAG |= GUID_SRC << 24 & 0xFF000000;
 * GUID_FLAG |= FLAG_MAC_ANDROIDID_GUID_CHANGE << 8 & 0xFF00;
 * ```
 *
 * FLAG_MAC_ANDROIDID_GUID_CHANGE:
 * ```java
 * if (!Arrays.equals(currentMac, get_last_mac)) {
 *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x1;
 * }
 * if (!Arrays.equals(currentAndroidId, get_last_android_id)) {
 *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x2;
 * }
 * if (!Arrays.equals(currentGuid, get_last_guid)) {
 *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x4;
 * }
 * ```
 */
internal fun guidFlag(
    guidSource: GuidSource,
    macOrAndroidIdChangeFlag: MacOrAndroidIdChangeFlag
): Long {
    var flag = 0L
    flag = flag or (guidSource.id shl 24 and 0xFF000000)
    flag = flag or (macOrAndroidIdChangeFlag.value shl 8 and 0xFF00)
    return flag
}