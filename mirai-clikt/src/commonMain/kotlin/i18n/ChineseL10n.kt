/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.clikt.i18n

import net.mamoe.mirai.clikt.output.Localization

/**
 * zh-CN Clikt 本地化
 */
public class ChineseL10n : Localization {
    override fun usageError(message: String): String = "错误: $message"

    override fun badParameter(): String = "参数无效"

    override fun badParameterWithMessage(message: String): String = "参数无效: $message"

    override fun badParameterWithParam(paramName: String): String = "\"$paramName\" 的参数无效"

    override fun badParameterWithMessageAndParam(paramName: String, message: String): String =
        "\"$paramName\" 的参数无效: $message"

    override fun missingOption(paramName: String): String = "缺少选项 \"$paramName\""

    override fun missingArgument(paramName: String): String = "缺少参数 \"$paramName\""

    override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return "无子命令: \"$name\"" + when (possibilities.size) {
            0 -> ""
            1 -> ". 是否想输入 \"${possibilities[0]}\"?"
            else -> possibilities.joinToString(prefix = ". (可能的子命令: ", postfix = ")")
        }
    }

    override fun noSuchOption(name: String, possibilities: List<String>): String {
        return "无选项: \"$name\"" + when (possibilities.size) {
            0 -> ""
            1 -> ". 是否想输入 \"${possibilities[0]}\"?"
            else -> possibilities.joinToString(prefix = ". (可能的选项: ", postfix = ")")
        }
    }

    override fun incorrectOptionValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "选项 $name 不需要值"
            1 -> "选项 $name 需要一个参数"
            else -> "选项 $name 需要 $count 个参数"
        }
    }

    override fun incorrectArgumentValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "参数 $name 不需要值"
            1 -> "参数 $name 需要一个值"
            else -> "参数 $name 需要 $count 个值"
        }
    }

    override fun mutexGroupException(name: String, others: List<String>): String {
        return "选项 $name 不可用于 ${others.joinToString(" 或 ")}"
    }

    override fun fileNotFound(filename: String): String = "找不到文件 $filename"

    override fun invalidFileFormat(filename: String, message: String): String = "文件的格式错误 $filename: $message"

    override fun invalidFileFormat(filename: String, lineNumber: Int, message: String): String =
        "文件格式错误 $filename, 第 $lineNumber 行: $message"

    override fun unclosedQuote(): String = "引号错误"

    override fun fileEndsWithSlash(): String = "文件以 \\ 结尾"

    override fun extraArgumentOne(name: String): String = "输入了多余的参数 $name"

    override fun extraArgumentMany(name: String, count: Int): String = "输入了多余的参数 $name"

    override fun invalidFlagValueInFile(name: String): String = "Invalid flag value in file for option $name"

    override fun switchOptionEnvvar(): String = "environment variables not supported for switch options"

    override fun requiredMutexOption(options: String): String = "必须提供这些选项中的一个 $options"

    override fun invalidGroupChoice(value: String, choices: List<String>): String =
        "无效的选择: $value. (选择列表 ${choices.joinToString()})"

    override fun floatConversionError(value: String): String = "$value 不是一个有效的浮点数"

    override fun intConversionError(value: String): String = "$value 不是一个有效的整数"

    override fun boolConversionError(value: String): String = "$value 不是一个有效的布尔值"

    override fun rangeExceededMax(value: String, limit: String): String = "$value 超过了最大限制 $limit."

    override fun rangeExceededMin(value: String, limit: String): String = "$value 少于最少限制 $limit."

    override fun rangeExceededBoth(value: String, min: String, max: String): String = "$value 不在允许的范围 $min 到 $max 内."

    override fun invalidChoice(choice: String, choices: List<String>): String {
        return "无效选择: $choice. (可用列表： ${choices.joinToString()})"
    }

    override fun usageTitle(): String = "用法:"

    override fun optionsTitle(): String = "选项:"

    override fun argumentsTitle(): String = "参数:"

    override fun commandsTitle(): String = "子命令:"

    override fun optionsMetavar(): String = "[选项]"

    override fun commandMetavar(): String = "命令 [参数]..."

    override fun helpTagDefault(): String = "默认"

    override fun helpTagRequired(): String = "必须"

    override fun helpOptionMessage(): String = "显示帮助信息"
}