/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import java.lang.reflect.Method
import kotlin.reflect.KClass


fun Any.reflectMethod(name: String, vararg params: KClass<out Any>): Pair<Any, Method> {
    return this to this::class.java.getMethod(name, *params.map { it.java }.toTypedArray())
}

operator fun Pair<Any, Method>.invoke(vararg args: Any?): Any? {
    return second.invoke(first, *args)
}

@Suppress("NOTHING_TO_INLINE") // or error
fun Project.setJavaCompileTarget() {
    tasks.filter { it.name in arrayOf("compileKotlin", "compileTestKotlin") }.forEach { task ->
        task
            .reflectMethod("getKotlinOptions")()!!
            .reflectMethod("setJvmTarget", String::class)("1.8")
    }


    kotlin.runCatching { // apply only when java plugin is available
        (extensions.getByName("java") as JavaPluginExtension).run {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType(JavaCompile::class.java) {
            options.encoding = "UTF8"
        }
    }
}