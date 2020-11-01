/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "RedundantModalityModifier",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE",
    "NON_FINAL_MEMBER_IN_OBJECT",
    "ConvertSecondaryConstructorToPrimary"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.net

import kotlin.jvm.JvmStatic

public expect open class InetAddress : java.io.Serializable {
    public constructor(hostsFileName: String)

    public open fun run(): Nothing?
    public open fun getOriginalHostName(ia: InetAddress): String
    public open fun isMulticastAddress(): Boolean
    public open fun isAnyLocalAddress(): Boolean
    public open fun isLoopbackAddress(): Boolean
    public open fun isLinkLocalAddress(): Boolean
    public open fun isSiteLocalAddress(): Boolean
    public open fun isMCGlobal(): Boolean
    public open fun isMCNodeLocal(): Boolean
    public open fun isMCLinkLocal(): Boolean
    public open fun isMCSiteLocal(): Boolean
    public open fun isMCOrgLocal(): Boolean
    public open fun isReachable(timeout: Int): Boolean
    public open fun getHostName(): String
    public open fun getCanonicalHostName(): String
    public open fun getAddress(): ByteArray
    public open fun getHostAddress(): String
    public open fun hashCode(): Int
    public open fun equals(obj: Any?): Boolean
    public open fun toString(): String
    public open fun get(): Array<InetAddress>

    //public open fun compareTo(other: CachedAddresses): Int
    public open fun getHostByAddr(addr: ByteArray): String
    public open fun lookupAllHostAddr(host: String): Array<InetAddress>

    public companion object {
        @JvmStatic
        public open fun getByAddress(host: String, addr: ByteArray): InetAddress

        @JvmStatic
        public open fun getByName(host: String): InetAddress

        @JvmStatic
        public open fun getAllByName(host: String): Array<InetAddress>

        @JvmStatic
        public open fun getLoopbackAddress(): InetAddress

        @JvmStatic
        public open fun getByAddress(addr: ByteArray): InetAddress

        @JvmStatic
        public open fun getLocalHost(): InetAddress
    }
}  