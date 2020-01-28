package test;

import net.mamoe.mirai.utils.cryptor.contentToString
import java.io.File
import java.lang.StringBuilder

fun main(){
    val var9 = toJCEInfo(File("/Users/jiahua.liu/Desktop/mirai/mirai-core-qqandroid/src/jvmTest/kotlin/test/GetFriendListReq").readText())
    println("import kotlinx.serialization.SerialId\n" +
            "import kotlinx.serialization.Serializable\n" +
            "import net.mamoe.mirai.qqandroid.io.JceStruct\n")
    println(var9.toString())
}


/**
 * 不支持叠加中
 */
class JCEInfo(
){
    lateinit var className: String
    var parents: List<String>? = null//seems useless
    lateinit var properties: List<Property>

    override fun toString(): String {
        properties = properties.sortedBy { it->it.jceID }
        /**
         *
        @Serializable
        class RequestDataVersion2(
        @SerialId(0) val map: Map<String, Map<String, ByteArray>>
        ) : JceStruct
         */
        val max = (properties.size - 1).toString().length
        val builder:StringBuilder = StringBuilder("@Serializable")
        builder.append("\n").append("internal class ").append(className).append("(")
        properties.forEach {
            builder.append(",").append("\n").append(it.toStringWithSpacing(max))
        }
        builder.append("\n").append("): JceStruct")
        return builder.toString().replace("(,","(")
    }


}

class Property(
    val name:String,
    var type:String,
    var defaultValue:String? = null
){
    var jceID:Int = -1
    //convert type/default value to kotlin format
    init {
        type = type
            .replace("byte[]", "ByteArray")
            .replace("ArrayList", "List")
            .replace("byte", "Byte")
            .replace("int", "Int")
            .replace("short", "Short")
            .replace("long", "Long")
    }
    //@SerialId(1) val iVersion: Short = 3,
    override fun toString(): String {
        if (defaultValue != null) {
            return "@SerialId(" + jceID + ") val " + name + ":" + type + " = " + defaultValue
        }
        return "@SerialId(" + jceID + ") val " + name + ":" + type+"? = null"
    }

    fun toStringWithSpacing(maxIDLength:Int): String {
        val space = " ".repeat(maxIDLength - (jceID.toString().length))
        if (defaultValue != null) {
            return "    @SerialId(" + jceID + ") " + space + "val " + name + ":" + type + " = " + defaultValue
        }
        return "    @SerialId(" + jceID + ") "+ space +"val " + name + ":" + type+"? = null"
    }

}


fun toJCEInfo(source:String):JCEInfo{
    val info = JCEInfo()
    val allProperties = mutableMapOf<String,Property>()
    var inputStreamVariableRegix:String? = null
    println(source)
    source.split("\n").forEach{
        when{
            it.contains("class") -> {
                var var0 = it.substringBetween("class","{").trim()
                if(var0.contains("extends")){
                    info.parents = var0.substringAfter("extends").split(",").map { it.trim() }.toList()
                    var0 = var0.substringBefore(" extends")
                }
                //println("class name: $var0" )
                info.className = var0
            }

            (it.contains("public") && it.contains(";")) -> {
                val var1 = it.trim().split(" ")
                if(var1.size == 5){
                    allProperties.put(var1[2],
                        Property(
                            var1[2],
                            var1[1],
                            var1[4].replace(";","")
                        )
                    )
                }else{
                    allProperties.put(
                        var1[2].replace(";",""),
                        Property(
                            var1[2].replace(";",""),
                            var1[1]
                        )
                    )
                }
            }

            (inputStreamVariableRegix==null && it.contains("public void readFrom")) -> {
                //   public void readFrom(JceInputStream var1) {
                inputStreamVariableRegix = it.trim().substringBetween("(JceInputStream ",")") + ".read"
                //println("inputStreamVariableRegix: " + inputStreamVariableRegix )
            }

            (inputStreamVariableRegix!=null && it.contains(inputStreamVariableRegix!!)) -> {
                val key = it.substringBetween("this.", " = ")
                if(!allProperties.containsKey(key)){
                    println(key + " is found in readFrom but not in properties")
                }
                val id = it
                    .substringBetween(".read(",");")
                    .split(",")[1].trim().toInt()
                allProperties.get(key)?.jceID = id;
            }
        }
    }
    info.properties = allProperties.values.toList();
    return info;
}