/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.Either.Companion.fold
import kotlin.jvm.JvmName
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

@Serializable
public abstract class CheckableResponseB : CheckableResponse {
    public abstract val result: Int

    @Suppress("SpellCheckingInspection")
    public abstract val errmsg: String

    final override val _errorCode: Int get() = result
    final override val _errorMessage: String get() = errmsg
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

/*
 * `check`: throws exception, or returns succeed value.
 * `checked`: do `check` and wrap result into an `Either`.
 */

public fun <T : CheckableResponse> T.check(): T {
    check(_errorCode == 0) { "Error code: $_errorCode, Error message: $_errorMessage" }
    return this
}

public open class FailureResponse(
    public val errorCode: Int,
    public val errorMessage: String,
) {
    public fun createException(): Exception {
        return IllegalStateException("Error code: $errorCode, Error message: $errorMessage")
    }
}

public inline fun <reified T : CheckableResponse> T.checked(): Either<FailureResponse, T> {
    if (_errorCode == 0) return Either<FailureResponse, T>(this)
    return Either(FailureResponse(_errorCode, _errorMessage.toString()))
}

public fun DeserializationFailure.check(): Nothing = throw this.createException()
public fun FailureResponse.check(): Nothing = throw this.createException()

public inline fun <reified T : CheckableResponse> Either<DeserializationFailure, T>.check(): T {
    return this.fold(onLeft = { it.check() }, onRight = { it.check() })
}

public inline fun <reified T> Either<DeserializationFailure, T>.check(): T {
    return this.fold(onLeft = { it.check() }, onRight = { it })
}

@JvmName("checkedFailureResponseT")
public inline fun <reified T> Either<FailureResponse, T>.check(): T {
    return this.fold(onLeft = { it.check() }, onRight = { it })
}