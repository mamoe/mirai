/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import javax.inject.Inject

@CacheableTask
public open class BuildMiraiPluginTask @Inject constructor(
    @JvmField internal val target: KotlinTarget
) : ShadowJar() {
    /**
     * ShadowJar 打包结果
     */
    @get:OutputFile
    public val output: File
        get() = outputs.files.singleFile

    public companion object {
        /**
         * Kotlin 单平台或 Java 时的默认 task 名.
         */
        public const val DEFAULT_NAME: String = "buildPlugin"
    }
}