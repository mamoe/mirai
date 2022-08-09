/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("BotConfigurationExt_common")

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiFile
import net.mamoe.mirai.utils.resolveCreateFile
import net.mamoe.mirai.utils.resolveMkdir
import kotlin.jvm.JvmName


internal expect val BotConfiguration.workingDirPath: String

/*
Note: Required the written path,
      NOT the resolved (absolute) path.
      See: #2160
*/
internal expect val BotConfiguration.cacheDirPath: String

internal fun BotConfiguration.actualCacheDir(): MiraiFile = MiraiFile.create(workingDirPath).resolveMkdir(cacheDirPath)
internal fun BotConfiguration.contactCacheDir(): MiraiFile = actualCacheDir().resolveMkdir("contacts")
internal fun BotConfiguration.friendCacheFile(): MiraiFile = contactCacheDir().resolveCreateFile("friends.json")
internal fun BotConfiguration.groupCacheDir(): MiraiFile = contactCacheDir().resolveMkdir("groups")
internal fun BotConfiguration.groupCacheFile(groupId: Long): MiraiFile =
    groupCacheDir().resolveCreateFile("$groupId.json")

internal fun BotConfiguration.accountSecretsFile(): MiraiFile = actualCacheDir().resolve("account.secrets")