package net.mamoe.mirai.console.center

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object CuiPluginCenter: PluginCenter{

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
            if(it > plugins!!.size()){
                return@forEach
            }
            val info = plugins!![it]
            with(info.asJsonObject){
                map[this.get("name").asString] = PluginCenter.PluginInsight(
                    this.get("name").asString,
                    this.get("version").asString,
                    this.get("core").asString,
                    this.get("console").asString,
                    this.get("author").asString,
                    this.get("contact").asString,
                    this.get("tags").asJsonArray.map { it.asString },
                    this.get("commands").asJsonArray.map { it.asString }
                )
            }
        }
        return map
    }

    override suspend fun findPlugin(name: String): PluginCenter.PluginInfo? {
        TODO()
    }

    override suspend fun refresh() {
        val results =
            withContext(Dispatchers.IO) {
                Jsoup
                    .connect("https://miraiapi.jasonczc.cn/getPluginList")
                    .ignoreContentType(true)
                    .execute()
            }.body().asJson()
        if(!(results.has("success") && results["success"].asBoolean)){
            error("Failed to fetch plugin list from Cui Cloud")
        }
        plugins = results.getAsJsonArray("result")//先不解析
    }

    override val name: String
        get() = "崔云"


    fun String.asJson(): JsonObject {
        return JsonParser.parseString(this).asJsonObject
    }

}