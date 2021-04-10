/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils

import android.util.Log

/**
 * [Log] 日志实现
 *
 * @see MiraiLogger.create
 * @see SingleFileLogger 使用单一文件记录日志
 * @see DirectoryLogger 在一个目录中按日期存放文件记录日志, 自动清理过期日志
 */
@MiraiInternalApi
public actual open class PlatformLogger actual constructor(
    public override val identity: String?,
) : MiraiLoggerPlatformBase() {

    public override fun verbose0(message: String?) {
        Log.v(identity, message)
    }

    public override fun verbose0(message: String?, e: Throwable?) {
        Log.v(identity, message, e)
    }


    public override fun info0(message: String?) {
        Log.i(identity, message)
    }

    public override fun info0(message: String?, e: Throwable?) {
        Log.i(identity, message, e)
    }


    public override fun warning0(message: String?) {
        Log.w(identity, message)
    }

    public override fun warning0(message: String?, e: Throwable?) {
        Log.w(identity, message, e)
    }


    public override fun error0(message: String?) {
        Log.e(identity, message)
    }

    public override fun error0(message: String?, e: Throwable?) {
        Log.e(identity, message, e)
    }


    public override fun debug0(message: String?) {
        Log.d(identity, message)
    }

    public override fun debug0(message: String?, e: Throwable?) {
        Log.d(identity, message, e)
    }

}