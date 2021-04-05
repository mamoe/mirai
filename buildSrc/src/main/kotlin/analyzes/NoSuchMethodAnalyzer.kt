/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package analyzes

import analyzes.AsmUtil.hasField
import analyzes.AsmUtil.hasMethod
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.util.zip.ZipFile

@Suppress("UNCHECKED_CAST")
object NoSuchMethodAnalyzer {
    private fun analyzeMethod(
        analyzer: AndroidApiLevelCheck.Analyzer,
        method: MethodNode,
        asmClasses: AsmClassesM
    ) {
        analyzer.withContext("Analyze ${method.name}${method.desc}") {
            method.instructions?.forEach { insn ->
                when (insn) {
                    is MethodInsnNode -> {
                        if (insn.owner.startsWith("net/mamoe/mirai/")) {
                            if (!asmClasses.hasMethod(insn.owner, insn.name, insn.desc, insn.opcode)) {
                                report(
                                    "No such method",
                                    "${insn.owner}.${insn.name}${insn.desc}, opcode=${insn.opcode}"
                                )
                            }
                        }
                    }
                    is FieldInsnNode -> {
                        if (insn.owner.startsWith("net/mamoe/mirai/")) {
                            if (!asmClasses.hasField(insn.owner, insn.name, insn.desc, insn.opcode)) {
                                report(
                                    "No such field",
                                    "${insn.owner}.${insn.name}: ${insn.desc}, opcode=${insn.opcode}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun check(classes: Sequence<File>, libs: Sequence<File>) = AsmUtil.run {
        val analyzer = AndroidApiLevelCheck.Analyzer(emptyMap())
        val asmClasses: AsmClassesM = AsmClassesM(mutableListOf(), HashMap())
        libs.forEach { lib ->
            asmClasses += lib.readLib()
        }
        classes.map { it.walk() }.flatten().filter { it.isFile }
            .filter { it.extension == "class" }
            .map { it.readClass() to it }
            .onEach { (c, _) ->
                asmClasses[c.name] = lazyOf(c)
            }.toList().forEach { (classNode, file) ->
                analyzer.file = file
                classNode.methods?.forEach { method ->
                    analyzeMethod(analyzer, method, asmClasses)
                }
            }
        asmClasses.close()
        if (analyzer.reported) {
            error("Verify failed")
        }
    }
}