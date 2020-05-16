package net.mamoe.mirai.console.center

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.*
import net.mamoe.mirai.console.utils.retryCatching
import java.io.File

internal object CuiPluginCenter : PluginCenter {

    var plugins: JsonArray? = null

    /**
     * 一页10个吧,pageMinNum=1
     */
    override suspend fun fetchPlugin(page: Int): Map<String, PluginCenter.PluginInsight> {
        check(page > 0)
        val startIndex = (page - 1) * 10
        val endIndex = startIndex + 9
        val map = mutableMapOf<String, PluginCenter.PluginInsight>()
        (startIndex until endIndex).forEach {
            if (plugins == null) {
                refresh()
            }
            if (it >= plugins!!.size) {
                return@forEach
            }
            val info = plugins!![it]
            with(info.jsonObject) {
                map[this["name"]!!.toString()] = PluginCenter.PluginInsight(
                    this["name"]?.primitive?.content ?: "",
                    this["version"]?.primitive?.content ?: "",
                    this["core"]?.primitive?.content ?: "",
                    this["console"]?.primitive?.content ?: "",
                    this["author"]?.primitive?.content ?: "",
                    this["description"]?.primitive?.content ?: "",
                    this["tags"]?.jsonArray?.map { it.primitive.content } ?: arrayListOf(),
                    this["commands"]?.jsonArray?.map { it.primitive.content } ?: arrayListOf()
                )
            }
        }
        return map
    }

    @OptIn(KtorExperimentalAPI::class)
    private val Http = HttpClient(CIO)

    override suspend fun findPlugin(name: String): PluginCenter.PluginInfo? {
        val result = retryCatching(3) {
            Http.get<String>("https://miraiapi.jasonczc.cn/getPluginDetailedInfo?name=$name")
        }.recover {
            return null
        }.getOrNull() ?: return null

        if (result == "err:not found") {
            return null
        }

        return result.asJson().run {
            PluginCenter.PluginInfo(
                this["name"]?.primitive?.content ?: "",
                this["version"]?.primitive?.content ?: "",
                this["core"]?.primitive?.content ?: "",
                this["console"]?.primitive?.content ?: "",
                this["tags"]?.jsonArray?.map { it.primitive.content } ?: arrayListOf(),
                this["author"]?.primitive?.content ?: "",
                this["contact"]?.primitive?.content ?: "",
                this["description"]?.primitive?.content ?: "",
                this["usage"]?.primitive?.content ?: "",
                this["vsc"]?.primitive?.content ?: "",
                this["commands"]?.jsonArray?.map { it.primitive.content } ?: arrayListOf(),
                this["changeLog"]?.jsonArray?.map { it.primitive.content } ?: arrayListOf()
            )
        }

    }

    override suspend fun refresh() {
        val results = Http.get<String>("https://miraiapi.jasonczc.cn/getPluginList").asJson()

        if (!(results.containsKey("success") && results["success"]?.boolean == true)) {
            error("Failed to fetch plugin list from Cui Cloud")
        }
        plugins = results["result"]?.jsonArray//先不解析
    }

    override suspend fun <T : Any> T.downloadPlugin(name: String, progressListener: T.(Float) -> Unit): File {
        TODO()
        /*
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
         */
    }

    override val name: String
        get() = "崔云"


    private val json = Json(JsonConfiguration.Stable)
    
    private fun String.asJson(): JsonObject {
        return json.parseJson(this).jsonObject
    }

}

