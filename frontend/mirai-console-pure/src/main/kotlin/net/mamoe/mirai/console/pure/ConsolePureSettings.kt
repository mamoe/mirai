/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/mirai-console.mirai-console-pure.main/ConsolePureSettings.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

package net.mamoe.mirai.console.pure

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
@MustBeDocumented
annotation class ConsolePureExperimentalAPI

@ConsolePureExperimentalAPI
public object ConsolePureSettings {
    @JvmField
    var setupAnsi: Boolean = System.getProperty("os.name")
        .toLowerCase()
        .contains("windows")// Just for Windows

    @JvmField
    var noConsole: Boolean = false

    @JvmField
    var dropAnsi = false
    @JvmField
    var noConsoleSafeReading=false
}