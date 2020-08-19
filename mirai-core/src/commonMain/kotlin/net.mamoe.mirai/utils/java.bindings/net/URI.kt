/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.net

import java.io.Serializable
import kotlin.jvm.JvmStatic

public expect class URI : Comparable<URI>, Serializable {
    public constructor(str: String)
    public constructor(scheme: String, host: String, path: String, fragment: String)
    public constructor(scheme: String, ssp: String, fragment: String)

    public open fun parseServerAuthority(): URI
    public open fun normalize(): URI
    public open fun resolve(uri: URI): URI
    public open fun resolve(str: String): URI
    public open fun relativize(uri: URI): URI
    public open fun toURL(): URL
    public open fun getScheme(): String
    public open fun isAbsolute(): Boolean
    public open fun isOpaque(): Boolean
    public open fun getRawSchemeSpecificPart(): String
    public open fun getSchemeSpecificPart(): String
    public open fun getRawAuthority(): String
    public open fun getAuthority(): String
    public open fun getRawUserInfo(): String
    public open fun getUserInfo(): String
    public open fun getHost(): String
    public open fun getPort(): Int
    public open fun getRawPath(): String
    public open fun getPath(): String
    public open fun getRawQuery(): String
    public open fun getQuery(): String
    public open fun getRawFragment(): String
    public open fun getFragment(): String
    public open fun equals(ob: Any?): Boolean
    public open fun hashCode(): Int
    public open fun compareTo(that: URI): Int
    public open fun toString(): String
    public open fun toASCIIString(): String
    public open fun create(scheme: String, path: String): URI

    public companion object {
        @JvmStatic
        public fun create(str: String): URI
    }
}  