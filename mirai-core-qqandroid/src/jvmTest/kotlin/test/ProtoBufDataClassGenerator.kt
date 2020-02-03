package test

import net.mamoe.mirai.utils.cryptor.ProtoType
import net.mamoe.mirai.utils.cryptor.protoFieldNumber
import java.io.File

fun main() {
    println(
        File("""/Users/jiahua.liu/Desktop/QQAndroid-F/app/src/main/java/tencent/im/group/group_label/""")
            .generateUnarrangedClasses().toMutableList().arrangeClasses().joinToString("\n\n")
    )
}

class ArrangedClass(
    val name: String,
    val source: String,
    val innerClasses: MutableList<ArrangedClass>
) {
    fun toKotlinProtoBufClass(): String {
        return if (innerClasses.isNotEmpty()) {
            """
                    $source {
                        ${innerClasses.joinToString("\n\n") { "    ${it.toKotlinProtoBufClass()}" }}
                    }
                """.trimIndent()
        } else source
    }

    override fun toString(): String = toKotlinProtoBufClass()
}

fun MutableList<GeneratedClass>.arrangeClasses(): List<ArrangedClass> {
    val tree: MutableMap<String, ArrangedClass> = mutableMapOf()

    // 先处理所有没有父类的
    this.removeAll(this.filter { it.superclasses.isEmpty() }.onEach {
        tree[it.className] = it.toArrangedClass()
    })

    // 一层继承的处理
    tree.forEach { (name, clazz) ->
        this.removeAll(this.filter { it.superclasses.size == 1 && it.superclasses[0] == name }.onEach {
            clazz.innerClasses.add(it.toArrangedClass())
        })
    }

    // 两层继承的处理
    tree.filter { it.value.innerClasses.isNotEmpty() }.forEach { (_, clazz) ->
        clazz.innerClasses.forEach { innerClass ->
            this.removeAll(this.filter { it.superclasses[1] == innerClass.name }.onEach {
                innerClass.innerClasses.add(it.toArrangedClass())
            })
        }
    }

    return tree.values.toList()

//    // 循环处理每个 class 的第一个父类
//    while (this.any { it.superclasses.isNotEmpty() }) {
//        this.forEach { generatedClass: GeneratedClass ->
//            generatedClass.superclasses.lastOrNull()?.let { superClassName ->
//                if (!tree.containsKey(superClassName)) {
//                    tree[superClassName] = this@arrangeClasses.firstOrNull {
//                        it.className == superClassName
//                    } ?: inline {
//                        println("${generatedClass.className} 继承了 $superClassName, 但找不到生成的这个 class, 将使用一个空的 class 替代")
//                        GeneratedClass(mutableListOf(), superClassName, "class $superClassName")
//                    }
//                }
//
//                generatedClass.superclasses.remove(superClassName)
//            }
//        }
//    }
}

fun File.generateUnarrangedClasses(): List<GeneratedClass> {
    return this.listFiles()?.filter { it.isFile }?.map {
        it.readText().generateProtoBufDataClass()
    } ?: error("Not a folder")
}

fun String.substringBetween(left: String, right: String): String {
    return this.substringAfter(left, "").substringBefore(right, "")
}

data class GeneratedClass(
    /**
     * 带先后顺序
     */
    val superclasses: MutableList<String>,
    val className: String,
    val source: String
) {
    fun toArrangedClass(): ArrangedClass {
        return ArrangedClass(className, source, mutableListOf())
    }
}

sealed class InferredType(val adjustKotlinAnnotationDeclaration: (String) -> String = { it }, val kotlinType: String) {
    object FIXED64 : InferredType({ "@ProtoType(ProtoNumberType.FIXED) $it" }, "Long")
    object FIXED32 : InferredType({ "@ProtoType(ProtoNumberType.FIXED) $it" }, "Int")
    object FIXED16 : InferredType({ "@ProtoType(ProtoNumberType.FIXED) $it" }, "Short")
    object FIXED8 : InferredType({ "@ProtoType(ProtoNumberType.FIXED) $it" }, "Byte")

    object SIGNED64 : InferredType({ "@ProtoType(ProtoNumberType.SIGNED) $it" }, "Long")
    object SIGNED32 : InferredType({ "@ProtoType(ProtoNumberType.SIGNED) $it" }, "Int")
    object SIGNED16 : InferredType({ "@ProtoType(ProtoNumberType.SIGNED) $it" }, "Short")
    object SIGNED8 : InferredType({ "@ProtoType(ProtoNumberType.SIGNED) $it" }, "Byte")

    object UNSIGNED64 : InferredType(kotlinType = "Long")
    object UNSIGNED32 : InferredType(kotlinType = "Int")
    object UNSIGNED16 : InferredType(kotlinType = "Short")
    object UNSIGNED8 : InferredType(kotlinType = "Byte")

    object BYTES : InferredType(kotlinType = "ByteArray")
    object STRING : InferredType(kotlinType = "String")

    object FLOAT : InferredType(kotlinType = "Float")
    object DOUBLE : InferredType(kotlinType = "Double")
    object BOOLEAN : InferredType(kotlinType = "Boolean")

    object ENUM : InferredType(kotlinType = "Int /* enum */")

    class REPEATED(kotlinType: String) : InferredType(kotlinType = "List<$kotlinType>")
    class CUSTOM(kotlinType: String) : InferredType(kotlinType = kotlinType)
}

data class PBFieldInfo(
    val protoTag: Int,
    val name: String,
    val inferredType: InferredType,
    val defaultValue: String
) {
    fun toKotlinProtoBufClassParam(): String {
        return "${inferredType.adjustKotlinAnnotationDeclaration("@SerialId($protoTag)")} val $name: ${inferredType.kotlinType}${if (defaultValue == "null") "?" else ""} = $defaultValue"
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun String.generateProtoBufDataClass(): GeneratedClass {
    if (this.indexOf("extends") == -1) {
        val javaClassname = substringBetween("class", "{")
        val superclasses = javaClassname.split("$").map { it.trim().adjustClassName() }.toMutableList().apply { removeAt(this.lastIndex) }
        val className = substringBetween("class", "{").substringAfterLast("$").trim().adjustClassName()
        return GeneratedClass(superclasses, className, "@Serializable\nclass $className : ProtoBuf")
    }

    val superclasses = substringBetween("class", "extends").split("$").map { it.trim().adjustClassName() }.toMutableList()
    superclasses.removeAt(superclasses.lastIndex)
    val className = substringBetween("class", "extends").substringAfterLast("$").trim().adjustClassName()


    val ids = substringBetween("new int[]{", "}").split(",").map { it.trim() }

    if (ids.all { it.isBlank() }) {
        return GeneratedClass(superclasses, className, "@Serializable\nclass $className : ProtoBuf")
    }

    val names = substringBetween("new String[]{", "}").split(",").map { it.trim() }
    val defaultValues = substringBetween("new Object[]{", "}").split(",").map { it.trim() }


    // name to original declaration
    val pbTypedFields = lines()
        .asSequence()
        .map { it.trim() }
        .filter { it.startsWith("public final PB") }
        .filterNot { it.startsWith("public final class") }
        .map { it.substringAfter("public final PB") }
        .associateBy { it.substringBetween(" ", " ").takeIf { it.isNotBlank() } ?: it.substringBetween(" ", ";") }
        .mapKeys { it.key.trim() }

    val customTypedFields = lines()
        .asSequence()
        .map { it.trim() }
        .filter { it.startsWith("public ") }
        .filterNot { it.startsWith("public final") }
        .filterNot { it.startsWith("public static") }
        .filterNot { it.startsWith("public ${substringBetween("class", "extends").trim()}()") }
        .map { it.substringAfter("public ") }
        .associateBy { it.substringBetween(" ", " ").takeIf { it.isNotBlank() } ?: it.substringBetween(" ", ";") }
        .mapKeys { it.key.trim() }

    val source = buildString {
        append("@Serializable").append("\n")
        append("class $className(").append("\n")

        ids.map { it.toUInt() }
            .map { protoFieldNumber(it) }
            .mapIndexed { index, tag ->
                var name = names[index].adjustName()
                var defaultValue = defaultValues[index].let {
                    if (it.startsWith("var")) "EMPTY_BYTE_ARRAY" else it
                }

                val originalName = names[index].substringBetween("\"", "\"")
                val javaDeclaration = pbTypedFields[originalName]

                val inferredType: InferredType = if (javaDeclaration == null) {
                    InferredType.CUSTOM(
                        customTypedFields[originalName]?.substringBefore(" ")?.adjustClassName()?.replace("$", ".")
                            ?: error("找不到 customTypedFields for $originalName in class $className")
                    )
                } else {
                    when (val javaFieldType = javaDeclaration.substringBefore("Field")) {
                        "Int8", "UInt8" -> InferredType.UNSIGNED8
                        "Int16", "UInt16" -> InferredType.UNSIGNED16
                        "Int32", "UInt32" -> InferredType.UNSIGNED32
                        "Int64", "UInt64" -> InferredType.UNSIGNED64

                        "SInt8" -> InferredType.SIGNED8
                        "SInt16" -> InferredType.SIGNED16
                        "SInt32" -> InferredType.SIGNED32
                        "SInt64" -> InferredType.SIGNED64

                        "Fixed8", "FInt8", "SFInt8" -> InferredType.FIXED8
                        "Fixed16", "FInt16", "SFInt16" -> InferredType.FIXED16
                        "Fixed32", "FInt32", "SFInt32" -> InferredType.FIXED32
                        "Fixed64", "FInt64", "SFInt64" -> InferredType.FIXED64

                        "Bytes" -> InferredType.BYTES
                        "String" -> InferredType.STRING
                        "Double" -> InferredType.DOUBLE
                        "Float" -> InferredType.FLOAT
                        "Bool" -> InferredType.BOOLEAN
                        "Enum" -> InferredType.ENUM

                        "Repeat", "RepeatMessage" -> InferredType.REPEATED(
                            javaDeclaration.substringBetween("<", ">").adjustClassName().replace(
                                "$",
                                "."
                            ).replace("Integer", "Int")
                        )
                        else -> error("Unsupported type: $javaFieldType for $originalName in class $className")
                    }
                }

                fun adjustPropertyName(_name: String): String {
                    var name = _name
                    when {
                        name.startsWith("str") -> {
                            name = name.substringAfter("str").takeIf { it.isNotBlank() }?.adjustName() ?: "str"
                            if (defaultValue == "EMPTY_BYTE_ARRAY")
                                defaultValue = "\"\""
                        }
                        name.startsWith("uint32") -> {
                            name = name.substringAfter("uint32").takeIf { it.isNotBlank() }?.adjustName() ?: "uint32"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace(".0", "", ignoreCase = true)
                                .replace("l", "", ignoreCase = true)
                        }
                        name.startsWith("double") -> {
                            name = name.substringAfter("double").takeIf { it.isNotBlank() }?.adjustName() ?: "double"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace("l", "", ignoreCase = true)
                        }
                        name.startsWith("float") -> {
                            name = name.substringAfter("float").takeIf { it.isNotBlank() }?.adjustName() ?: "float"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace("l", "", ignoreCase = true) + "f"
                        }
                        name.startsWith("uint16") -> {
                            name = name.substringAfter("uint16").takeIf { it.isNotBlank() }?.adjustName() ?: "uint16"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace(".0", "", ignoreCase = true)
                                .replace("l", "", ignoreCase = true)
                        }
                        name.startsWith("uint8") -> {
                            name = name.substringAfter("uint8").takeIf { it.isNotBlank() }?.adjustName() ?: "uint8"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace(".0", "", ignoreCase = true)
                                .replace("l", "", ignoreCase = true)
                        }
                        name.startsWith("uint64") -> {
                            name = name.substringAfter("uint64").takeIf { it.isNotBlank() }?.adjustName() ?: "uint64"
                            defaultValue = defaultValue.replace("D", "", ignoreCase = true)
                                .replace("f", "", ignoreCase = true)
                                .replace(".0", "", ignoreCase = true)
                        }
                        name.startsWith("bytes") -> {
                            name = name.substringAfter("bytes").takeIf { it.isNotBlank() }?.adjustName() ?: "bytes"
                        }
                        name.startsWith("rpt") -> {
                            name = name.substringAfter("rpt").takeIf { it.isNotBlank() }?.substringAfter("_")?.let { adjustPropertyName(it) } ?: "rpt"
                        }
                    }
                    return name
                }
                name = adjustPropertyName(name)

                when (inferredType) {
                    is InferredType.REPEATED -> {
                        if (defaultValue.getNumericalValue() == 0 || defaultValue == "EMPTY_BYTE_ARRAY") {
                            defaultValue = "null"
                        }
                    }
                    is InferredType.STRING -> {
                        if (defaultValue == "EMPTY_BYTE_ARRAY") {
                            defaultValue = "\"\""
                        }
                    }
                    is InferredType.BYTES -> {
                        if (defaultValue == "\"\"") {
                            defaultValue = "EMPTY_BYTE_ARRAY"
                        }
                    }
                }

                name = name.adjustName()
                if (name[0] in '0'..'9') {
                    name = "_" + name
                }

                append(PBFieldInfo(tag, name, inferredType, defaultValue).toKotlinProtoBufClassParam())

                if (ids.size - 1 != index) {
                    append(",")
                }

                append("\n")
            }

        append(") : ProtoBuf")
    }

    return GeneratedClass(superclasses, className, source)
}

fun String.getNumericalValue(): Int? {
    return this.filter { it in '0'..'9' }.toDoubleOrNull()?.toInt()
}

fun ProtoType.mapToKotlinType(): String {
    return when (this) {
        ProtoType.VAR_INT -> "Int"
        ProtoType.BIT_64 -> "Long"
        ProtoType.LENGTH_DELIMI -> "String"
        ProtoType.BIT_32 -> "Float"
        else -> "UNKNOWN"
    }
}

fun String.adjustClassName(): String {
    when (this.trim()) {
        "ByteStringMicro" -> return "ByteArray"
    }
    if(this.isEmpty()){
        return ""
    }
    return String(this.adjustName().toCharArray().apply { this[0] = this[0].toUpperCase() })
}

fun String.adjustName(): String {
    val result = this.toCharArray()
    if(result.size == 0){
        return ""
    }
    result[0] = result[0].toLowerCase()
    for (index in result.indices) {
        if (result[index] == '_') {
            if (index + 1 in result.indices) {
                result[index + 1] = result[index + 1].toUpperCase()
            }
        }
    }

    return String(result).replace("_", "").trim().removePrefix("\"").removeSuffix("\"")
}