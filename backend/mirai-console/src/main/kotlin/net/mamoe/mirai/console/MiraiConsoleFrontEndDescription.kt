/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import com.vdurmont.semver4j.Semver

/**
 * 有关前端实现的信息
 */
public interface MiraiConsoleFrontEndDescription {
    /**
     * 此前端实现的名称
     */
    public val name: String

    /**
     * 此前端实现的提供者
     */
    public val vendor: String

    /**
     * 此前端实现的名称
     */
    public val version: Semver

    /**
     * 返回显示在 [MiraiConsole] 启动时的信息
     */
    public fun render(): String = "Frontend ${name}: version ${version}, provided by $vendor"
}