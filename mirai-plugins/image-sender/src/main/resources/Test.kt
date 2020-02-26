import com.alibaba.fastjson.JSONObject
import com.google.gson.JsonObject
import net.mamoe.mirai.console.plugins.*
import net.mamoe.mirai.utils.cryptor.contentToString
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import kotlin.concurrent.thread

object Data {
    val section = (File(System.getProperty("user.dir") + "/setu.yml")).loadAsConfig()


    val abstract = section.getStringList("abstract").toMutableList()
    val R18 = section.getConfigSectionList("R18").toMutableList()
    val normal = section.getConfigSectionList("normal").toMutableList()

    fun init() {
        section.setIfAbsent("abstract", mutableListOf<String>())
        section.setIfAbsent("R18", mutableListOf<ConfigSection>())
        section.setIfAbsent("Normal", mutableListOf<ConfigSection>())
    }

    fun save() {
        section["abstract"] = abstract
        section["R18"] = R18
        section["normal"] = normal
        section.save()
    }
}

fun main() {

    val abstract_file = (File(System.getProperty("user.dir") + "/abstractSetu.yml")).loadAsConfig()

    abstract_file.setIfAbsent("R18", mutableListOf<ConfigSection>())
    abstract_file.setIfAbsent("normal", mutableListOf<ConfigSection>())

    val r18 = abstract_file.getConfigSectionList("R18").toMutableList()
    val normal = abstract_file.getConfigSectionList("normal").toMutableList()

    Data.R18.forEach {
        val forbid = with(it.getString("tags")) {
            this.contains("初音ミク") || this.contains("VOCALOID") || this.contains("Miku")
                    ||
                    this.contains("东方") || this.contains("東方")
        }
        if (forbid) {
            println("过滤掉了一张图")
        } else {
            r18.add(
                ConfigSectionImpl().apply {
                    this["pid"] = it["pid"]!!
                    this["author"] = it["author"]!!
                    this["uid"] = it["uid"]!!
                    this["tags"] = it["tags"]!!
                    this["url"] = it["url"]!!
                }
            )
        }
    }

    Data.normal.forEach {
        val forbid = with(it.getString("tags")) {
            this.contains("初音ミク") || this.contains("VOCALOID") || this.contains("Miku")
                    ||
                    this.contains("东方") || this.contains("東方")
        }
        if (forbid) {
            println("过滤掉了一张图")
        } else {
            normal.add(
                ConfigSectionImpl().apply {
                    this["pid"] = it["pid"]!!
                    this["author"] = it["author"]!!
                    this["uid"] = it["uid"]!!
                    this["tags"] = it["tags"]!!
                    this["url"] = it["url"]!!
                }
            )
        }
    }

    abstract_file.set("R18", r18)
    abstract_file.set("normal", normal)
    abstract_file.save()

    /**
    Data.init()
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
    Data.save()
    })

    while (true){
    try {
    val val0 = JSONObject.parseObject(Jsoup
    .connect("https://api.lolicon.app/setu/")
    .ignoreContentType(true)
    .method(Connection.Method.GET)
    .data("r18","1")
    .data("num","10")
    .execute().body())

    val val1 = val0.getJSONArray("data")
    for(index in 0 until val1.size - 1){
    val content = val1.getJSONObject(index)

    val pid = content.getString("pid")
    if(Data.abstract.contains(pid)){
    println("获取到了一张重复图$pid")
    continue
    }
    val configSection = ConfigSectionImpl()
    val isR18 = content.getBoolean("r18")
    configSection["author"] = content.getString("author")
    configSection["pid"] = pid
    configSection["uid"] = content.getInteger("uid")
    configSection["width"] = content.getInteger("width")
    configSection["height"] = content.getInteger("height")
    configSection["tags"] = content.getJSONArray("tags").map {
    it.toString()
    }.joinToString(",")
    configSection["url"] = content.getString("url")
    if(isR18){
    Data.R18.add(configSection)
    print("获取到了一张R18")
    }else{
    Data.normal.add(configSection)
    print("获取到了一张Normal")
    }
    Data.abstract.add(pid)
    println(configSection.contentToString())
    }
    }catch (e:Exception){
    println(e.message)
    }
    Data.save()
    println("SAVED")
    Thread.sleep(1000)
    }

     */

}