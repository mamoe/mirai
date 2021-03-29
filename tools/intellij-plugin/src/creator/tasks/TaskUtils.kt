/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator.tasks

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.writeChild
import net.mamoe.mirai.console.intellij.creator.steps.NamedFile
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val Path.vfOrNull: VirtualFile?
    get() = LocalFileSystem.getInstance().refreshAndFindFileByPath(this.toAbsolutePath().toString())

val Path.vf: VirtualFile
    get() = vfOrNull ?: error("Failed to resolve VirtualFile ${this.toAbsolutePath()}")

fun VirtualFile.readText(): String? = if (this.exists() && !this.isDirectory) String(inputStream.use { it.readBytes() }) else null
fun VirtualFile.readChildText(relative: String): String? = this.resolve(relative)?.readText()

fun VirtualFile.resolve(relative: String): VirtualFile? = VfsUtil.findRelativeFile(
    this,
    *relative.replace('\\', '/').split('/').toTypedArray()
)

fun <T> invokeAndWait(modalityState: ModalityState? = null, runnable: () -> T): T {
    val app = ApplicationManager.getApplication()
    if (app.isDispatchThread) return runnable()
    return computeDelegated {
        app.invokeAndWait({ it(runnable()) }, modalityState ?: ModalityState.defaultModalityState())
    }
}

fun <T> runWriteActionAndWait(modalityState: ModalityState? = null, runnable: () -> T) {
    invokeAndWait(modalityState) {
        runWriteAction(runnable)
    }
}

@PublishedApi
internal inline fun <T> computeDelegated(executor: (setter: (T) -> Unit) -> Unit): T {
    var resultRef: T? = null
    executor { resultRef = it }
    @Suppress("UNCHECKED_CAST")
    return resultRef as T
}

fun Project.getTemplate(
    templateName: String,
    properties: Map<String, String?>? = null
): String {
    val manager = FileTemplateManager.getInstance(this)
    val template = manager.getJ2eeTemplate(templateName)

    val allProperties = manager.defaultProperties
    properties?.let { prop -> allProperties.putAll(prop.mapValues { it.value.orEmpty() }) }

    return template.getText(allProperties)
}

fun Project.getTemplate(
    templateName: String,
    vararg properties: Pair<String, String?>
): String = getTemplate(templateName, properties.toMap())


fun VirtualFile.writeChild(namedFile: NamedFile): VirtualFile = this.writeChild(namedFile.path, namedFile.content)

@Language("RegExp")
const val CLASS_NAME_PATTERN = "[a-zA-Z]+[0-9a-zA-Z_]*" // self written

@Language("RegExp")
const val PACKAGE_PATTERN = """[a-zA-Z]+[0-9a-zA-Z_]*(\.[a-zA-Z]+[0-9a-zA-Z_]*)*"""

@Language("RegExp")
const val QUALIFIED_CLASS_NAME_PATTERN = """($PACKAGE_PATTERN\.)?$CLASS_NAME_PATTERN""" // self written

fun String.isValidQualifiedClassName(): Boolean = this matches Regex(QUALIFIED_CLASS_NAME_PATTERN)
fun String.isValidPackageName(): Boolean = this matches Regex(PACKAGE_PATTERN)
fun String.isValidSimpleClassName(): Boolean = this matches Regex(CLASS_NAME_PATTERN)
fun String.adjustToClassName(): String? {
    val result = buildString {
        var doCapitalization = true

        fun Char.isAllowed() = isLetterOrDigit() || this in "_-"

        for (char in this@adjustToClassName) {
            if (!char.isAllowed()) continue

            if (doCapitalization) {
                when {
                    char.isDigit() -> {
                        if (this.isEmpty()) append('_')
                        append(char)
                    }
                    char.isLetter() -> append(char.toUpperCase())
                    char == '-' -> append("_")
                    else -> append(char)
                }
                doCapitalization = false
            } else {
                if (char in "_-") {
                    doCapitalization = true
                } else {
                    append(char)
                }
            }
        }
    }

    if (result.isValidSimpleClassName()) return result

    return null
}

@Suppress("RedundantNullableReturnType")
private val UNINITIALIZED: Any? = Any()

@Suppress("UNCHECKED_CAST")
fun <T, R> lateinitReadWriteProperty(initializer: () -> R) = object : ReadWriteProperty<T, R> {
    private var field = AtomicReference(UNINITIALIZED)
    override fun setValue(thisRef: T, property: KProperty<*>, value: R) {
        field.set(value)
    }

    override tailrec fun getValue(thisRef: T, property: KProperty<*>): R {
        val v = field.get()
        if (v !== UNINITIALIZED) return v as R
        field.compareAndSet(UNINITIALIZED, initializer())
        return getValue(thisRef, property)
    }
}