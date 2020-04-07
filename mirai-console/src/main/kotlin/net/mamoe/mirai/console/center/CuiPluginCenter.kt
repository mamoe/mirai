package net.mamoe.mirai.console.center

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.tryNTimes
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

internal object CuiPluginCenter: PluginCenter{

    var plugins:JsonArray? = null

    /**
     * 一页10个吧,pageMinNum=1
     */
    override suspend fun fetchPlugin(page: Int): Map<String, PluginCenter.PluginInsight> {
        check(page > 0)
        val startIndex = (page-1)*10
        val endIndex = startIndex+9
        val map = mutableMapOf<String, PluginCenter.PluginInsight>()
        (startIndex until endIndex).forEach {
            if(plugins == null){
                refresh()
            }
            if(it >= plugins!!.size()){
                return@forEach
            }
            val info = plugins!![it]
            with(info.asJsonObject){
                map[this.get("name").asString] = PluginCenter.PluginInsight(
                    this.get("name")?.asString ?: "",
                    this.get("version")?.asString ?: "",
                    this.get("core")?.asString ?: "",
                    this.get("console")?.asString ?: "",
                    this.get("author")?.asString ?: "",
                    this.get("description")?.asString ?: "",
                    this.get("tags")?.asJsonArray?.map { it.asString } ?: arrayListOf(),
                    this.get("commands")?.asJsonArray?.map { it.asString } ?: arrayListOf()
                )
            }
        }
        return map
    }

    override suspend fun findPlugin(name: String): PluginCenter.PluginInfo? {
        val result = withContext(Dispatchers.IO){
            tryNTimes {
                Jsoup
                    .connect("https://miraiapi.jasonczc.cn/getPluginDetailedInfo?name=$name")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute()
            }
        }.body()

        if(result == "err:not found"){
            return null
        }

        return result.asJson().run{
            PluginCenter.PluginInfo(
                this.get("name")?.asString ?: "",
                this.get("version")?.asString ?: "",
                this.get("core")?.asString ?: "",
                this.get("console")?.asString ?: "",
                this.get("tags")?.asJsonArray?.map { it.asString } ?: arrayListOf(),
                this.get("author")?.asString ?: "",
                this.get("contact")?.asString ?: "",
                this.get("description")?.asString ?: "",
                this.get("usage")?.asString ?: "",
                this.get("vsc")?.asString ?: "",
                this.get("commands")?.asJsonArray?.map { it.asString } ?: arrayListOf(),
                this.get("changeLog")?.asJsonArray?.map { it.asString } ?: arrayListOf()
            )
        }

    }

    override suspend fun refresh() {
        val results =
                withContext(Dispatchers.IO) {
                    tryNTimes {
                        Jsoup
                            .connect("https://miraiapi.jasonczc.cn/getPluginList")
                            .ignoreContentType(true)
                            .execute()
                    }
                }.body().asJson()

        if(!(results.has("success") && results["success"].asBoolean)){
            error("Failed to fetch plugin list from Cui Cloud")
        }
        plugins = results.get("result").asJsonArray//先不解析
    }

    override suspend fun <T : Any> T.downloadPlugin(name: String, progressListener: T.(Float) -> Unit):File{
        val info = findPlugin(name) ?: error("Plugin Not Found")
        val targetFile = File(PluginManager.pluginsPath, "$name-" + info.version + ".jar")
        withContext(Dispatchers.IO) {
            tryNTimes {
                val con =
                    URL("https://pan.jasonczc.cn/?/mirai/plugins/$name/$name-" + info.version + ".mp4").openConnection() as HttpURLConnection
                val input = con.inputStream
                val size = con.contentLength
                var totalDownload = 0F
                val outputStream = FileOutputStream(targetFile)
                var len: Int
                val buff = ByteArray(1024)
                while (input.read(buff).also { len = it } != -1) {
                    totalDownload += len
                    outputStream.write(buff, 0, len)
                    progressListener.invoke(this@downloadPlugin, totalDownload / size)
                }
            }
        }
        return targetFile
    }

    override val name: String
        get() = "崔云"


    fun String.asJson(): JsonObject {
        return JsonParser.parseString(this).asJsonObject
    }

}

