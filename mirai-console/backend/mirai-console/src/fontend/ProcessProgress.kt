/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.fontend

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.Closeable

/**
 * 一个下载进度
 *
 * @see MiraiConsole.newProcessProgress
 */
// @ConsoleFrontEndImplementation
public interface ProcessProgress : Closeable {
    /**
     * 更新当前下载进度的文本
     */
    public fun updateText(txt: String)

    /**
     * 更新当前下载进度的文本
     */
    public fun updateText(txt: CharSequence) {
        updateText(txt.toString())
    }

    /**
     * 设置此进度的最终大小
     */
    public fun setTotalSize(totalSize: Long)

    /**
     * 更新下载进度的进度
     *
     * 在更新进度后需要[刷新显示][rerender]
     */
    public fun update(processed: Long)

    /**
     * 更新下载进度的进度
     *
     * 在更新进度后需要[刷新显示][rerender]
     */
    public fun update(processed: Long, totalSize: Long)

    /**
     * 将该进度标记为 已失败 / 出错
     *
     * 注: 即使此进度被标记为失败, 也需要[手动释放][close]
     */
    public fun markFailed()

    /**
     * 立即重新渲染此进度
     */
    public fun rerender()

    /**
     * 释放此进度, 相关资源和 UI 将会更新
     */
    override fun close()
}
