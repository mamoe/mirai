@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "RedundantModalityModifier",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE", "NON_FINAL_MEMBER_IN_OBJECT"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.nio.file

import kotlin.jvm.JvmStatic

public expect final class Paths {

    public companion object {
        @JvmStatic
        public open fun get(first: String, vararg more: String): Path

        @JvmStatic
        public open fun get(uri: java.net.URI): Path
    }
}  