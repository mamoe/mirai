package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File

@CacheableTask
public open class BuildMiraiPluginTask : ShadowJar() {
    @Internal
    public var targetField: KotlinTarget? = null

    @get:Internal
    public val target: KotlinTarget
        get() = targetField!!

    /**
     * ShadowJar 打包结果
     */
    @get:Internal
    public val output: File
        get() = outputs.files.singleFile
}