package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.CacheableTask
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File

@CacheableTask
public open class BuildMiraiPluginTask : ShadowJar() {
    internal var targetField: KotlinTarget? = null

    public val target: KotlinTarget get() = targetField!!

    /**
     * ShadowJar 打包结果
     */
    public val output: File get() = outputs.files.singleFile
}