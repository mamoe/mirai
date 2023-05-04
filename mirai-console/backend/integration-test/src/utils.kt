/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import net.mamoe.mirai.console.internal.plugin.ConsoleJvmPluginTestFailedError
import net.mamoe.mirai.utils.MiraiInternalApi
import org.junit.jupiter.api.fail
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.*

internal fun readStringListFromEnv(key: String): MutableList<String> {
    val size = System.getenv(key)?.toInt() ?: 0
    val rsp = mutableListOf<String>()
    for (i in 0 until size) {
        rsp.add(System.getenv("${key}_$i")!!)
    }
    return rsp
}

internal fun saveStringListToEnv(key: String, value: Collection<String>, env: MutableMap<String, String>) {
    env[key] = value.size.toString()
    value.forEachIndexed { index, v ->
        env["${key}_$index"] = v
    }
}

// region assertion kits
public fun File.assertNotExists() {
    if (exists()) {
        fail { "Except ${this.absolutePath} not exists but this file exists in disk" }
    }
}

public fun assertClassSame(expected: Class<*>?, actually: Class<*>?) {
    fun vt(c: Class<*>?): String {
        if (c == null) return "<null>"
        return "$c from ${c.classLoader}"
    }
    if (expected === actually) return
    fail {
        "Class not same:\n" +
                "Class excepted: ${vt(expected)}\n" +
                "Class actually: ${vt(actually)}"
    }
}

@OptIn(MiraiInternalApi::class)
public fun forceFail(
    msg: String? = null,
    cause: Throwable? = null,
): Nothing {
    throw ConsoleJvmPluginTestFailedError(msg, cause)
}
// endregion

// region JVM Utils
public val vmClassfileVersion: Int = runCatching {
    val obj = ClassReader("java.lang.Object")
    val classobj = ClassNode().also { obj.accept(it, ClassReader.SKIP_CODE) }
    classobj.version
}.recoverCatching {
    val ccl = object : ClassLoader(null) {
        fun canLoad(ver: Int): Boolean {
            val klass = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val cname =
                "net/mamoe/console/integrationtest/vtest/C${ver}_${System.currentTimeMillis()}_${UUID.randomUUID()}"
                    .replace('-', '_')

            klass.visit(
                ver,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
                cname,
                null, "java/lang/Object", null
            )
            klass.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null)!!.also { cinit ->
                cinit.visitVarInsn(Opcodes.ALOAD, 0)
                cinit.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
                cinit.visitInsn(Opcodes.RETURN)
                cinit.visitMaxs(0, 0)
            }
            val code = klass.toByteArray()
            return kotlin.runCatching {
                val k = defineClass(null, code, 0, code.size)
                Class.forName(k.name, true, this)
            }.isSuccess
        }
    }
    if (ccl.canLoad(Opcodes.V17)) return@recoverCatching Opcodes.V17
    if (ccl.canLoad(Opcodes.V16)) return@recoverCatching Opcodes.V16
    if (ccl.canLoad(Opcodes.V15)) return@recoverCatching Opcodes.V15
    if (ccl.canLoad(Opcodes.V14)) return@recoverCatching Opcodes.V14
    if (ccl.canLoad(Opcodes.V13)) return@recoverCatching Opcodes.V13
    if (ccl.canLoad(Opcodes.V12)) return@recoverCatching Opcodes.V12
    if (ccl.canLoad(Opcodes.V11)) return@recoverCatching Opcodes.V11
    if (ccl.canLoad(Opcodes.V10)) return@recoverCatching Opcodes.V10
    if (ccl.canLoad(Opcodes.V9)) return@recoverCatching Opcodes.V9
    Opcodes.V1_8
}.getOrElse { Opcodes.V1_8 } // Fallback

public fun canVmLoad(opversion: Int): Boolean = opversion <= vmClassfileVersion

// endregion
