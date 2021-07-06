/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.Either.Companion.fold
import kotlin.reflect.KType

@Suppress("PropertyName")
public interface CheckableResponse {
    public val _errorCode: Int
    public val _errorMessage: String?
}

@Serializable
public abstract class CheckableResponseA : CheckableResponse {
    public abstract val errorCode: Int
    public abstract val errorMessage: String?

    final override val _errorCode: Int get() = errorCode
    final override val _errorMessage: String? get() = errorMessage
}

public class DeserializationFailure(
    structType: KType,
    public val json: String,
    public val exception: Throwable
) : CheckableResponseA() {
    override val errorCode: Int get() = -1
    override val errorMessage: String = "Failed to deserialize '$json' into $structType"
    public fun createException(): Exception {
        return IllegalStateException("Error code: $_errorCode, Error message: $_errorMessage", exception)
    }
}

public fun <T : CheckableResponse> T.checked(): T {
    check(_errorCode == 0) { "Error code: $_errorCode, Error message: $_errorMessage" }
    return this
}

public fun DeserializationFailure.checked(): Nothing = throw this.createException()

public inline fun <reified T : CheckableResponse> Either<DeserializationFailure, T>.checked(): T {
    return this.fold(onLeft = { it.checked() }, onRight = { it.checked() })
}

public inline fun <reified T> Either<DeserializationFailure, T>.checked(): T {
    return this.fold(onLeft = { it.checked() }, onRight = { it })
}