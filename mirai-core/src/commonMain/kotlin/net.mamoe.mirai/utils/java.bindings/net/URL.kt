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

public expect class URL : Serializable {
    public constructor(protocol: String, host: String, port: Int, file: String)
    public constructor(protocol: String, host: String, file: String)
    public constructor(spec: String)
    public constructor(context: URL, spec: String)

    //public constructor(context: URL, spec: String, handler: URLStreamHandler)
    public open fun getQuery(): String
    public open fun getPath(): String
    public open fun getUserInfo(): String
    public open fun getAuthority(): String
    public open fun getPort(): Int
    public open fun getDefaultPort(): Int
    public open fun getProtocol(): String
    public open fun getHost(): String
    public open fun getFile(): String
    public open fun getRef(): String
    public open fun equals(obj: Any?): Boolean
    public open fun hashCode(): Int
    public open fun sameFile(other: URL): Boolean
    public open fun toString(): String
    public open fun toExternalForm(): String
    public open fun toURI(): URI
    public open fun openConnection(): URLConnection
    public open fun openConnection(proxy: Proxy): URLConnection
    public fun openStream(): java.io.InputStream
    public fun getContent(): Any?

    //public final fun getContent(classes: Array<Class<?>>): Any?
    //public open fun createURLStreamHandler(protocol: String): URLStreamHandler
    public open fun hasNext(): Boolean

    //public open fun next(): java.net.spi.URLStreamHandlerProvider
    //public open fun run(): URLStreamHandler
    //public open fun getHandler(u: URL): URLStreamHandler
    public companion object {
        //@JvmStatic public open fun setURLStreamHandlerFactory(fac: URLStreamHandlerFactory)
    }
}  