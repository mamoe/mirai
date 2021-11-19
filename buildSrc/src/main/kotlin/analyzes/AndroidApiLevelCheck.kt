/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package analyzes

import groovy.util.Node
import org.gradle.api.Project
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import java.io.File

object AndroidApiLevelCheck {
    data class ClassInfo(
        val name: String,
        val since: Int,
        val superTypes: List<SuperInfo>,
        val fieldInfos: Map<String, MemberInfo>,
        val methodInfos: Map<String, MemberInfo>
    ) {
        data class SuperInfo(
            val name: String,
            val since: Int?,
            val removed: Int?
        )

        data class MemberInfo(
            val name: String,
            val since: Int?
        )
    }

    class Analyzer(
        val classesInfos: Map<String, ClassInfo>
    ) {
        var path: String? = null
        var context: String? = null
        var file: File? = null
        var apilevel = 0
        var reported = false
        inline fun withPath(path: String, block: Analyzer.() -> Unit) {
            this.path = path
            block(this)
            this.path = null
        }

        inline fun withContext(context: String, block: Analyzer.() -> Unit) {
            this.context = context
            block(this)
            this.context = null
        }

        fun report(prefix: String, message: String) {
            reported = true
            file?.let { file ->
                println("> $file")
                this.file = null
            }
            context?.let { context ->
                println("    > $context")
                this.context = null
            }
            path?.let { path ->
                println("      > $path")
                this.path = null
            }
            if (prefix.isBlank()) {
                message
            } else {
                "$prefix: $message"
            }.split('\n').forEach { println("          $it") }
        }

        fun needCheck(type: String): Boolean {
            if (type.startsWith("android/")) return true
            if (type.startsWith("androidx/")) return true
            if (type.startsWith("java/")) return true
            if (type.startsWith("javax/")) return true
            return classesInfos.containsKey(type)
        }

        fun checkClass(prefix: String, name: String) {
            if (!needCheck(name)) return
            val info = classesInfos[name]
            if (info == null) {
                report(prefix, "$name not found in api-version.xml")
                return
            }
            if (info.since > apilevel) {
                report(prefix, "$name since api level ${info.since}")
            }
        }

        fun checkFieldAccess(prefix: String, owner: String, name: String) {
            if (!needCheck(owner)) return

            val info = classesInfos[owner] ?: return
            val field = info.fieldInfos[name]
            if (field == null) {
                report(prefix, "No field $owner.$name")
                return
            }
            if ((field.since ?: 0) > apilevel) {
                report(prefix, "$owner.$name since api level ${field.since}")
            }
        }

        fun checkMethodAccess(prefix: String, owner: String, name: String) {
            if (!needCheck(owner)) return

            fun findMethod(type: String): ClassInfo.MemberInfo? {
                val cinfo = classesInfos[type] ?: return null
                return cinfo.methodInfos[name] ?: kotlin.run {
                    cinfo.superTypes.forEach { stype ->
                        if (stype.removed != null) {
                            if (apilevel >= stype.removed) return@forEach
                        }
                        if (stype.since != null) {
                            if (apilevel < stype.since) return@forEach
                        }
                        findMethod(stype.name)?.let { return it }
                    }
                    null
                }
            }

            val method = findMethod(owner)
            if (method == null) {
                report(prefix, "No method $owner.$name")
                return
            }
            if ((method.since ?: 0) > apilevel) {
                report(prefix, "$owner.$name since api level ${method.since}")
            }
        }

        private val Type.top: Type
            get() = when (sort) {
                Type.ARRAY -> elementType
                else -> this
            }


        fun analyze(classNode: ClassNode, file: File) {
            this.file = file
            withContext("Check class") {
                withPath("class checking") {
                    checkClass("Couldn't extend ${classNode.superName}", classNode.superName)
                    classNode.interfaces?.forEach { checkClass("Couldn't implements $it", it) }
                }
            }
            classNode.fields?.forEach { field ->
                withContext("Field ${field.name}: ${field.desc}") {
                    val type = Type.getType(field.desc).top.internalName
                    checkClass("Couldn't access $type", type)
                }
            }
            classNode.methods?.forEach { method ->
                withContext("Method ${method.name}${method.desc}") {
                    withPath("Checking method desc") {
                        val returnType = Type.getReturnType(method.desc).top.internalName
                        checkClass("Couldn't access $returnType", returnType)
                        Type.getArgumentTypes(method.desc).map { it.top.internalName }.forEach {
                            checkClass("Couldn't access $it", it)
                        }
                    }
                    method.instructions?.forEach { insn ->
                        when (insn) {
                            is FieldInsnNode -> {
                                withPath("Access field ${insn.owner}.${insn.name}: ${insn.desc}") {
                                    val type = Type.getType(insn.desc)
                                    val prefix = "Couldn't access ${insn.owner}.${insn.name}: ${insn.desc}"
                                    checkClass(prefix, type.internalName)
                                    checkFieldAccess(prefix, insn.owner, insn.name)
                                }
                            }
                            is MethodInsnNode -> {
                                withPath("Invoke method ${insn.owner}.${insn.name}${insn.desc}") {
                                    checkClass("Couldn't access ${insn.owner}", insn.owner)
                                    val returnType = Type.getReturnType(insn.desc).top.internalName
                                    checkClass("Couldn't access $returnType", returnType)
                                    Type.getArgumentTypes(insn.desc).map { it.top.internalName }.forEach {
                                        checkClass("Couldn't access $it", it)
                                    }
                                    checkMethodAccess(
                                        "Couldn't access ${insn.owner}.${insn.name}${insn.desc}",
                                        insn.owner,
                                        insn.name + insn.desc
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun check(classes: File, level: Int, project: Project) {
        val apiVersionsFile =
            project.rootProject.projectDir.resolve("buildSrc/src/main/resources/androidutil/api-versions.xml")
        val classesInfos = mutableMapOf<String, ClassInfo>()
        @Suppress("DEPRECATION")
        groovy.util.XmlParser().parse(apiVersionsFile).children().forEach { classNode ->
            classNode as Node
            if (classNode.name() == "class") {
                val fieldInfos = mutableMapOf<String, ClassInfo.MemberInfo>()
                val methodInfos = mutableMapOf<String, ClassInfo.MemberInfo>()
                val cinfo = ClassInfo(
                    classNode.attribute("name").toString(),
                    classNode.attribute("since").toString().toInt(),
                    (classNode.children() as List<Node>).filter {
                        it.name() == "implements" || it.name() == "extends"
                    }.map {
                        ClassInfo.SuperInfo(
                            it.attribute("name").toString(),
                            it.attribute("since")?.toString()?.toInt(),
                            it.attribute("removed")?.toString()?.toInt()
                        )
                    },
                    fieldInfos, methodInfos
                )
                classesInfos[cinfo.name] = cinfo
                classNode.children().forEach { memberNode ->
                    memberNode as Node
                    when (memberNode.name()) {
                        "method" -> {
                            val method = ClassInfo.MemberInfo(
                                memberNode.attribute("name").toString(),
                                memberNode.attribute("since")?.toString()?.toInt()
                            )
                            methodInfos[method.name] = method
                        }
                        "field" -> {
                            val field = ClassInfo.MemberInfo(
                                memberNode.attribute("name").toString(),
                                memberNode.attribute("since")?.toString()?.toInt()
                            )
                            fieldInfos[field.name] = field
                        }
                    }
                }
            }
        }
        val analyzer = Analyzer(classesInfos)
        analyzer.apilevel = level

        classes.walk()
            .filter { it.isFile && it.extension == "class" }
            .map { file ->
                kotlin.runCatching {
                    AsmUtil.run { file.readClass() }
                }.getOrNull() to file
            }
            .filter { it.first != null }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as Pair<ClassNode, File>
            }
            .forEach { (classNode, file) ->
                analyzer.analyze(classNode, file)
            }

        if (analyzer.reported) {
            error("Verify failed")
        }
    }
}
