/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.resolveCreateFile
import net.mamoe.mirai.utils.resolveMkdir
import java.io.File


internal fun BotConfiguration.actualCacheDir(): File = workingDir.resolveMkdir(cacheDir)
internal fun BotConfiguration.contactCacheDir(): File = actualCacheDir().resolveMkdir("contacts")
internal fun BotConfiguration.friendCacheFile(): File = contactCacheDir().resolveCreateFile("friends.json")
internal fun BotConfiguration.groupCacheDir(): File = contactCacheDir().resolveMkdir("groups")
internal fun BotConfiguration.groupCacheFile(groupId: Long): File = groupCacheDir().resolveCreateFile("$groupId.json")
