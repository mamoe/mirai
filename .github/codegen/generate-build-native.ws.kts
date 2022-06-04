/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

val env = "\${{ env.gradleArgs }}"

val isUbunutu = "\${{ env.isUbuntu == 'true' }}"
val isWindows = "\${{ env.isWindows == 'true' }}"
val isMac = "\${{ env.isMac == 'true' }}"


val template = """
      - if: CONDITION
        name: "Compile mirai-core-api for macosArm64"
        run: ./gradlew :mirai-core-api:compileKotlinMacosArm64 :mirai-core-api:compileTestKotlinMacosArm64 $env

      - if: CONDITION
        name: "Link mirai-core-api for macosArm64"
        run: ./gradlew mirai-core-api:linkDebugTestMacosArm64 $env

      - if: CONDITION
        name: "Test mirai-core-api for macosArm64"
        run: ./gradlew :mirai-core-api:macosArm64Test $env
""".trimIndent()

val output = buildString {
    val title = "############# GENERATED FROM generate-build-native.ws.kts #############"
    appendLine("#".repeat(title.length))
    appendLine(title)
    appendLine("#".repeat(title.length))
    appendLine()



    listOf("mirai-core-utils", "mirai-core-api", "mirai-core").forEach { moduleName ->
        appendLine(
            """
           - name: "Commonize mirai-core-api"
             run: ./gradlew :mirai-core-api:commonize $env
        """.trimIndent().replace("mirai-core-api", moduleName)
        )
        appendLine()
    }

    listOf("mirai-core-utils", "mirai-core-api", "mirai-core").forEach { moduleName ->
        appendLine("# $moduleName")
        appendLine()
        appendLine(
            """
           - name: "Compile mirai-core-api for common"
             run: ./gradlew :mirai-core-api:compileCommonMainKotlinMetadata $env

           - name: "Compile mirai-core-api for native"
             run: ./gradlew :mirai-core-api:compileNativeMainKotlinMetadata $env

           - name: "Compile mirai-core-api for unix-like"
             run: ./gradlew :mirai-core-api:compileUnixMainKotlinMetadata $env
        """.trimIndent().replace("mirai-core-api", moduleName)
        )
        appendLine()

        listOf("macosX64" to isMac, "mingwX64" to isWindows, "linuxX64" to isUbunutu).forEach { (target, condition) ->
            appendLine(useTemplate(moduleName, target, condition))
            appendLine()
            appendLine()
        }
        appendLine()
    }

    this.trimEnd().let { c -> clear().appendLine(c) } // remove trailing empty lines

    appendLine()
    appendLine("#".repeat(title.length))
}
println(output.prependIndent(" ".repeat(6)))

fun useTemplate(moduleName: String, target: String, condition: String) = template
    .replace("mirai-core-api", moduleName)
    .replace("macosArm64", target)
    .replace("MacosArm64", target.replaceFirstChar { it.uppercaseChar() })
    .replace("CONDITION", condition)
    // Link release artifacts to save memory
    .replace("linkDebugTestMingwX64", "linkReleaseTestMingwX64")
