/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.setting

import com.vdurmont.semver4j.Semver
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer

@Serializer(forClass = Semver::class)
internal object SemverAsStringSerializerLoose : KSerializer<Semver> by String.serializer().map(
    serializer = { it.toString() },
    deserializer = { Semver(it, Semver.SemverType.LOOSE) }
)

@Serializer(forClass = Semver::class)
internal object SemverAsStringSerializerIvy : KSerializer<Semver> by String.serializer().map(
    serializer = { it.toString() },
    deserializer = { Semver(it, Semver.SemverType.IVY) }
)