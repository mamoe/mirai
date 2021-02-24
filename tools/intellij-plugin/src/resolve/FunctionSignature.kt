/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.resolve

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import net.mamoe.mirai.console.intellij.diagnostics.resolveReferencedType
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.getReturnTypeReference
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.idea.search.getKotlinFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

inline fun FunctionSignature(builderAction: FunctionSignatureBuilder.() -> Unit): FunctionSignature {
    return FunctionSignatureBuilder().apply(builderAction).build()
}

data class FunctionSignature(
    val name: String? = null,
    val dispatchReceiver: FqName? = null,
    val extensionReceiver: FqName? = null,
    val parameters: List<FqName>? = null,
    val returnType: FqName? = null,
)

class FunctionSignatureBuilder {
    private var name: String? = null
    private var dispatchReceiver: FqName? = null
    private var extensionReceiver: FqName? = null
    private var parameters: List<FqName>? = null
    private var returnType: FqName? = null

    fun name(name: String) {
        this.name = name
    }

    fun dispatchReceiver(dispatchReceiver: String) {
        this.dispatchReceiver = FqName(dispatchReceiver)
    }

    fun extensionReceiver(extensionReceiver: String) {
        this.extensionReceiver = FqName(extensionReceiver)
    }

    fun parameters(vararg parameters: String) {
        this.parameters = parameters.map { FqName(it) }
    }

    fun returnType(returnType: String) {
        this.returnType = FqName(returnType)
    }

    fun build(): FunctionSignature = FunctionSignature(name, dispatchReceiver, extensionReceiver, parameters, returnType)
}

fun FunctionSignatureBuilder.dispatchReceiver(dispatchReceiver: FqName) {
    dispatchReceiver(dispatchReceiver.toString())
}

fun FunctionSignatureBuilder.extensionReceiver(extensionReceiver: FqName) {
    extensionReceiver(extensionReceiver.toString())
}


fun KtFunction.hasSignature(functionSignature: FunctionSignature): Boolean {
    if (functionSignature.name != null) {
        if (this.name != functionSignature.name) return false
    }
    if (functionSignature.dispatchReceiver != null) {
        if (this.containingClassOrObject?.fqName != functionSignature.dispatchReceiver) return false
    }
    if (functionSignature.extensionReceiver != null) {
        if (this.receiverTypeReference?.resolveReferencedType()?.getKotlinFqName() != functionSignature.extensionReceiver) return false
    }
    if (functionSignature.parameters != null) {
        if (this.valueParameters.zip(functionSignature.parameters).any { it.first.type()?.fqName != it.second }) return false
    }
    if (functionSignature.returnType != null) {
        if (this.getReturnTypeReference()?.resolveReferencedType()?.getKotlinFqName() != functionSignature.returnType) return false
    }
    return true
}


fun PsiMethod.hasSignature(functionSignature: FunctionSignature): Boolean {
    if (functionSignature.name != null) {
        if (this.name != functionSignature.name) return false
    }
    val parameters = parameterList.parameters.toMutableList()
    if (functionSignature.dispatchReceiver != null) {
        val containingClass = this.containingClass ?: return false

        val kotlinContainingClassFqn = if (this is KtLightMethod) {
            if (this.modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
                this.containingClass.kotlinOrigin?.companionObjects?.firstOrNull()?.fqName
            } else containingClass.getKotlinFqName()
        } else containingClass.getKotlinFqName()

        if (kotlinContainingClassFqn != functionSignature.dispatchReceiver) return false
    }
    if (functionSignature.extensionReceiver != null) {
        val receiver = parameters.removeFirstOrNull() ?: return false
        if (receiver.type.canonicalText != functionSignature.extensionReceiver.toString()) return false
    }
    if (functionSignature.parameters != null) {
        if (parameters.zip(functionSignature.parameters).any { it.first.type.canonicalText != it.second.toString() }) return false
    }
    if (functionSignature.returnType != null) {
        if (returnType?.canonicalText != functionSignature.returnType.toString()) return false
    }
    return true
}


fun KtExpression.isCalling(functionSignature: FunctionSignature): Boolean {
    val descriptor = resolveToCall(BodyResolveMode.PARTIAL)?.resultingDescriptor ?: return false
    return descriptor.hasSignature(functionSignature)
}

fun CallableDescriptor.hasSignature(functionSignature: FunctionSignature): Boolean {
    if (functionSignature.name != null) {
        if (this.name.toString() != functionSignature.name) return false
    }
    if (functionSignature.extensionReceiver != null) {
        if (this.extensionReceiverParameter?.fqNameUnsafe != functionSignature.extensionReceiver.toUnsafe()) return false
    }
    if (functionSignature.dispatchReceiver != null) {
        if (this.containingDeclaration.fqNameUnsafe != functionSignature.dispatchReceiver.toUnsafe()) return false
    }
    if (functionSignature.parameters != null) {
        if (this.valueParameters.zip(functionSignature.parameters).any { it.first.type.fqName != it.second }) return false
    }
    if (functionSignature.returnType != null) {
        if (this.returnType?.fqName != functionSignature.returnType) return false
    }
    return true
}