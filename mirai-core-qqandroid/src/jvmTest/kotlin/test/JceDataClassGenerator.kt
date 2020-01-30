package test;

import java.io.File

fun main(){
    println(
        "import kotlinx.serialization.SerialId\n" +
                "import kotlinx.serialization.Serializable\n" +
                "import net.mamoe.mirai.qqandroid.io.JceStruct\n"
    )
    File(
        """
        E:\Projects\QQAndroidFF\app\src\main\java\friendlist\
    """.trimIndent()
    ).listFiles()!!.forEach {
        try {
            println(toJCEInfo(it.readText()).toString())
        } catch (e: Exception) {
            println("when processing ${it.path}")
            throw e
        }
    }
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
    var name:String,
    var type:String,
    var defaultValue:String? = null
){
    var isRequired: Boolean = true
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

        if(name.length >1 && name.get(1).isUpperCase()){
            if(name.get(0) == 'l' || name.get(0) =='c' || name.get(0) == 'b'){
                name = name.substring(1)
            }
        }

        if(name.startsWith("str") || name.startsWith("bytes")){
            name = name.replace("str","").replace("bytes","")
        }

        if(name.contains("_")){
            val x = name.split("_")
            name = x.get(0);
            var z = 1;
            repeat(x.size-1){
                name+= "" + x.get(z).get(0).toUpperCase() + x.get(z).substring(1).toLowerCase()
                ++z;
            }
        }

        name = "" + name.get(0).toLowerCase() + "" + name.substring(1)
    }
    //@SerialId(1) val iVersion: Short = 3,
    override fun toString(): String {
        if (defaultValue != null) {
            return "@SerialId(" + jceID + ") val " + name + ":" + type + " = " + defaultValue
        }
        return "@SerialId(" + jceID + ") val " + name + ":" + type+"? = null"
    }

    fun toStringWithSpacing(maxIDLength:Int): String {
        val space = " ".repeat((maxIDLength - (jceID.toString().length)).coerceAtLeast(0))
        var base = "    @SerialId(" + jceID + ") " + space + "val " + name + ":" + type + ""
        if(!isRequired){
            if(defaultValue == null) {
                base += "? = null"
            }else{
                base += "? = $defaultValue"
            }
        }else{
            if(defaultValue != null) {
                base+=" = " + defaultValue
            }
        }
        return base
    }

}


fun toJCEInfo(source:String):JCEInfo{
    val info = JCEInfo()
    val allProperties = mutableMapOf<String,Property>()
    var inputStreamVariableRegix:String? = null
    // println(source)
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
                val src = it
                    .replace(".readString(",".read(\" \",")
                    .substringBetween("(",");")
                    .split(",")
                with(allProperties.get(key)!!){
                    this.jceID = src[1].trim().toInt()
                    this.isRequired = src[2].trim().toBoolean()
                }
            }
        }
    }
    info.properties = allProperties.values.toList();
    return info;
}