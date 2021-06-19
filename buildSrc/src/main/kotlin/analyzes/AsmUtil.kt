/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package analyzes

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.InputStream

typealias AsmClasses = Map<String, ClassNode>
typealias AsmClassesM = MutableMap<String, ClassNode>

object AsmUtil {
    fun ClassNode.getMethod(name: String, desc: String, isStatic: Boolean): MethodNode? {
        return methods?.firstOrNull {
            it.name == name && it.desc == desc && ((it.access and Opcodes.ACC_STATIC) != 0) == isStatic
        }
    }

    fun ClassNode.getField(name: String, desc: String, isStatic: Boolean): FieldNode? {
        return fields?.firstOrNull {
            it.name == name && it.desc == desc && ((it.access and Opcodes.ACC_STATIC) != 0) == isStatic
        }
    }

    fun File.readClass(): ClassNode = inputStream().use { it.readClass() }

    fun InputStream.readClass(): ClassNode {
        val cnode = ClassNode()
        ClassReader(this).accept(cnode, 0)
        return cnode
    }

    private fun AsmClassesM.patchJvmClass(owner: String) {
        if (owner.startsWith("java/") || owner.startsWith("javax/")) {
            if (!this.containsKey(owner)) {
                ClassLoader.getSystemClassLoader().getResourceAsStream("$owner.class")?.use {
                    val c = it.readClass()
                    this[c.name] = c
                }
            }
        }
    }

    fun AsmClassesM.hasField(
        owner: String,
        name: String,
        desc: String,
        opcode: Int
    ): Boolean {
        patchJvmClass(owner)
        val c = this[owner] ?: return false
        val isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC
        if (c.getField(name, desc, isStatic) != null) {
            return true
        }
        if (isStatic) return false
        return hasField(c.superName ?: "", name, desc, opcode)
    }

    fun AsmClassesM.hasMethod(
        owner: String,
        name: String,
        desc: String,
        opcode: Int
    ): Boolean {
        patchJvmClass(owner)
        when (opcode) {
            Opcodes.INVOKESTATIC -> {
                val c = this[owner] ?: return false
                return c.getMethod(name, desc, true) != null
            }
            Opcodes.INVOKEINTERFACE,
            Opcodes.INVOKESPECIAL,
            Opcodes.INVOKEVIRTUAL -> {
                fun loopFind(current: String): Boolean {
                    patchJvmClass(current)
                    val c = this[current] ?: return false
                    if (c.getMethod(name, desc, false) != null) return true
                    c.superName?.let {
                        if (loopFind(it)) {
                            return true
                        }
                    }
                    c.interfaces?.forEach {
                        if (loopFind(it)) return true
                    }
                    return false
                }
                return loopFind(owner)
            }
        }
        return false
    }
}