/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator.steps

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import net.mamoe.mirai.console.compiler.common.cast
import org.intellij.lang.annotations.Language
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.JComponent
import javax.swing.text.JTextComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


class Validation {

    annotation class WithValidator(val clazz: KClass<out Validator<WithValidator>>) {
        companion object {
            init {
                registerValidator<WithValidator> { annotation, component ->
                    val instance = annotation.clazz.objectInstance ?: annotation.clazz.createInstance()
                    instance.validate(annotation, component)
                }
            }
        }
    }

    annotation class NotBlank(val tipName: String) {
        companion object {
            init {
                registerValidator<NotBlank> { annotation, component ->
                    if (component.text.isNullOrBlank()) {
                        report("请填写 ${annotation.tipName}")
                    }
                }
            }
        }
    }

    annotation class Pattern(val tipName: String, @Language("RegExp") val value: String) {
        companion object {
            init {
                registerValidator<Pattern> { annotation, component ->
                    if (component.text?.matches(Regex(annotation.value)) != true) {
                        report("请正确填写 ${annotation.tipName}")
                    }
                }
            }
        }
    }

    fun interface Validator<in A : Annotation> {
        @Throws(ValidationException::class)
        fun ValidationContext.validate(annotation: A, component: JTextComponent)

        @Throws(ValidationException::class)
        fun validate(annotation: A, component: JTextComponent) {
            ValidationContext.run { validate(annotation, component) }
        }

        object ValidationContext {
            fun report(message: String): Nothing = throw ValidationException(message)
        }
    }

    class ValidationException(message: String) : Exception(message)

    companion object {
        private data class RegisteredValidator<A : Annotation>(val type: KClass<A>, val validator: Validator<A>)

        private val validators: MutableCollection<RegisteredValidator<*>> = ConcurrentLinkedQueue()

        private inline fun <reified A : Annotation> registerValidator(validator: Validator<A>) {
            validators.add(RegisteredValidator(A::class, validator))
        }

        fun popup(message: String, component: JComponent) {
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(message, MessageType.ERROR, null)
                .setFadeoutTime(2000)
                .createBalloon()
                .show(RelativePoint.getSouthWestOf(component), Balloon.Position.below)
        }

        /**
         * @return `true` if no error
         */
        fun doValidation(step: ModuleWizardStep): Boolean {
            fun validateProperty(field: Field): Boolean {
                field.isAccessible = true
                val annotationsToValidate =
                    validators.associateBy { (type: KClass<out Annotation>) ->
                        field.annotations.find { it::class == type }
                    }

                for ((annotation, validator) in annotationsToValidate) {
                    if (annotation == null) continue
                    val component = field.get(step) as JTextComponent
                    try {
                        validator.validator.cast<Validator<Annotation>>().validate(annotation, component)
                    } catch (e: ValidationException) {
                        popup(e.message ?: e.toString(), component)
                        return false // report one error only
                    }
                }
                return true
            }

            var result = true
            for (prop in step::class.java.declaredFields) {
                if (!validateProperty(prop)) result = false
            }
            return result
        }
    }
}
