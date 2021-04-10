/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package postktcompile

import analyzes.*
import difflib.DiffUtils
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.File

object PostKotlinCompile {
    private fun AsmClasses.canAccessInterface(from: ClassNode, to: String): Boolean {
        fun scan(current: ClassNode): Boolean {
            if (current.name == to) return true
            if (current.superName == to) return true
            current.interfaces?.forEach { itf ->
                if (scan(get(itf)?.value ?: return@forEach)) return true
            }
            return false
        }
        return scan(from)
    }

    fun fixKtClass(dir: File, libs: Set<File>, logs: MutableList<String>) = AsmUtil.run {
        val asmClasses: AsmClassesM = AsmClassesM(mutableListOf(), HashMap())
        dir.walk().filter {
            it.isFile && it.extension == "class"
        }.forEach { f ->
            f.readClass().let { asmClasses[it.name] = lazyOf(it) }
        }
        val classes = asmClasses.values.toList()
        libs.forEach { asmClasses += it.readLib() }

        classes.forEach { klassLazy ->
            val klass = klassLazy.value
            var edited = false
            klass.methods?.forEach { method ->
                method.instructions?.forEach { insn ->
                    if (insn is MethodInsnNode) {
                        if (insn.itf && insn.opcode != Opcodes.INVOKEINTERFACE && insn.opcode != Opcodes.INVOKESTATIC) {
                            if (!asmClasses.canAccessInterface(klass, insn.owner)) {
                                // println("${klass.name} . ${method.name}${method.desc}, ${insn.owner}.${insn.name}${insn.desc} (${insn.opcode})")
                                edited = true
                                logs.add(
                                    "[${klass.name}] [${method.name}${method.desc}] - Change opcode of ${insn.owner}.${insn.name}${insn.desc} from ${insn.opcode} to ${Opcodes.INVOKEINTERFACE}"
                                )
                                insn.opcode = Opcodes.INVOKEINTERFACE
                            }
                        }
                    }
                }
            }
            if (edited) {
                dir.resolve("${klass.name}.class").writeBytes(
                    ClassWriter(0).also { klass.accept(it) }.toByteArray()
                )
            }
        }
        asmClasses.close()
    }

    fun registerForAll(rootProject: Project) {
        val checkEnabled = rootProject.hasProperty("mirai.pkc.check.enable")
        val validator = rootProject.file("binary-compatibility-validator/kt-compile-edit")
        rootProject.subprojects {
            val subp: Project = this@subprojects
            subp.tasks.withType(AbstractKotlinCompileTool::class.java) {
                val task: AbstractKotlinCompileTool<*> = this@withType
                task.doLast {
                    val logFile = validator.resolve("${subp.name.replace(":", "_")}-${task.name}.txt")
                    val logs = mutableListOf<String>()
                    println("$task: Pre classes patching......")
                    task.outputs.files.toList().forEach { f ->
                        println("$task: Patching $f")
                        fixKtClass(f, task.classpath.files, logs)
                    }
                    println("$task: Kotlin compiled classes fix completed.")
                    val oldLog = if (logFile.isFile) logFile.readText().trim().lines() else emptyList()
                    val newLog = logs.sorted()
                    if (newLog == oldLog) return@doLast
                    val patch = DiffUtils.diff(oldLog, newLog)

                    val diff = DiffUtils.generateUnifiedDiff(logFile.name, logFile.name + ".rebuild", oldLog, patch, 3)
                    logFile.parentFile.mkdirs()
                    if (checkEnabled || newLog.isNotEmpty()) { // Kotlin classes not recompiled.
                        logFile.writeText(newLog.joinToString("\n", postfix = "\n"))
                    }
                    val diffMsg = diff.joinToString("\n")
                    if (checkEnabled) {
                        error(diffMsg)
                    } else {
                        println(diffMsg)
                    }
                }
            }
        }
    }
}
