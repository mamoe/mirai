/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

public interface JsonStruct

@PublishedApi
internal val defaultJson: Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

public fun <T : JsonStruct> String.loadAs(deserializer: DeserializationStrategy<T>, json: Json = defaultJson): T {
    return json.decodeFromString(deserializer, this)
}

@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified T> String.loadSafelyAs(
    deserializer: DeserializationStrategy<T>,
    json: Json = defaultJson
): Either<DeserializationFailure, T> where T : JsonStruct {
    return try {
        Either<DeserializationFailure, T>(json.decodeFromString(deserializer, this))
    } catch (e: Throwable) {
        // typeOf is used in ktor and coroutines so Kotlin will absolutely provide ABI guarantee for it.
        Either(DeserializationFailure(typeOf<T>(), this, e))
    }
}

public fun <T : JsonStruct> T.toJsonString(serializer: SerializationStrategy<T>, json: Json = defaultJson): String =
    json.encodeToString(serializer, this)