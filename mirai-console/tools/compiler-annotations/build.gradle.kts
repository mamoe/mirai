/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

version = Versions.console
description = "Mirai Console compiler annotations"

kotlin {
    explicitApi()

    jvm("android") {
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.androidJvm)
    }

    jvm("common") {
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.common)
    }

    jvm("jvm")
}

configureMppPublishing()