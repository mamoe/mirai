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
    "VIRTUAL_MEMBER_HIDDEN",
    "NON_FINAL_MEMBER_IN_OBJECT", "ConvertSecondaryConstructorToPrimary"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.net

import kotlin.jvm.JvmStatic

public expect abstract class URLConnection {
    protected constructor(url: URL)

    public open fun getContentTypeFor(fileName: String): String
    public abstract fun connect()
    public open fun setConnectTimeout(timeout: Int)
    public open fun getConnectTimeout(): Int
    public open fun setReadTimeout(timeout: Int)
    public open fun getReadTimeout(): Int
    public open fun getURL(): URL
    public open fun getContentLength(): Int
    public open fun getContentLengthLong(): Long
    public open fun getContentType(): String
    public open fun getContentEncoding(): String
    public open fun getExpiration(): Long
    public open fun getDate(): Long
    public open fun getLastModified(): Long
    public open fun getHeaderField(name: String): String
    public open fun getHeaderFields(): Map<String, List<String>>
    public open fun getHeaderFieldInt(name: String, Default: Int): Int
    public open fun getHeaderFieldLong(name: String, Default: Long): Long
    public open fun getHeaderFieldDate(name: String, Default: Long): Long
    public open fun getHeaderFieldKey(n: Int): String
    public open fun getHeaderField(n: Int): String
    public open fun getContent(): Any?

    //public open fun getContent(classes: Array<Class<?>>): Any?
    //public open fun getPermission(): java.security.Permission
    public open fun getInputStream(): java.io.InputStream
    public open fun getOutputStream(): java.io.OutputStream
    public open fun toString(): String
    public open fun setDoInput(doinput: Boolean)
    public open fun getDoInput(): Boolean
    public open fun setDoOutput(dooutput: Boolean)
    public open fun getDoOutput(): Boolean
    public open fun setAllowUserInteraction(allowuserinteraction: Boolean)
    public open fun getAllowUserInteraction(): Boolean
    public open fun setUseCaches(usecaches: Boolean)
    public open fun getUseCaches(): Boolean
    public open fun setIfModifiedSince(ifmodifiedsince: Long)
    public open fun getIfModifiedSince(): Long
    public open fun getDefaultUseCaches(): Boolean
    public open fun setDefaultUseCaches(defaultusecaches: Boolean)
    public open fun setRequestProperty(key: String, value: String)
    public open fun addRequestProperty(key: String, value: String)
    public open fun getRequestProperty(key: String): String
    public open fun getRequestProperties(): Map<String, List<String>>

    //public open fun run(): ContentHandler
    public open fun getContent(uc: URLConnection): Any?

    public companion object {
        //@JvmStatic public open fun getFileNameMap(): FileNameMap
        //@JvmStatic public open fun setFileNameMap(map: FileNameMap)
        @JvmStatic
        public open fun setDefaultAllowUserInteraction(defaultallowuserinteraction: Boolean)

        @JvmStatic
        public open fun getDefaultAllowUserInteraction(): Boolean

        @JvmStatic
        public open fun setDefaultUseCaches(protocol: String, defaultVal: Boolean)

        @JvmStatic
        public open fun getDefaultUseCaches(protocol: String): Boolean

        @JvmStatic
        public open fun setDefaultRequestProperty(key: String, value: String)

        @JvmStatic
        public open fun getDefaultRequestProperty(key: String): String

        //@JvmStatic public open fun setContentHandlerFactory(fac: ContentHandlerFactory)
        @JvmStatic
        public open fun guessContentTypeFromName(fname: String): String

        @JvmStatic
        public open fun guessContentTypeFromStream(`is`: java.io.InputStream): String
    }
}  