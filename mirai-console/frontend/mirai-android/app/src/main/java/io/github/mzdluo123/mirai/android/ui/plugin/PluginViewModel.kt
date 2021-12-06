package io.github.mzdluo123.mirai.android.ui.plugin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.utils.DexCompiler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@ExperimentalUnsignedTypes
class PluginViewModel : ViewModel() {
    val pluginList = MutableLiveData<List<File>>()

    init {
        refreshPluginList()
    }

    private fun loadPluginList(): List<File> {
        val fileList = mutableListOf<File>()
        BotApplication.context.getExternalFilesDir("plugins")?.listFiles()?.forEach {
            if (it.isFile) {
                fileList.add(it)
            }
        }
        return fileList
    }

    fun deletePlugin(pos: Int) {
        val file = pluginList.value?.get(pos) ?: return
        file.delete()
        refreshPluginList()
    }

    fun refreshPluginList() {
        viewModelScope.launch {
            pluginList.postValue(loadPluginList())
        }
    }

    suspend fun compilePlugin(file: File, desugaring: Boolean) {
        val workDir = BotApplication.context.getExternalFilesDir(null) ?: return
        val tempDir = BotApplication.context.cacheDir
        val compiler = DexCompiler(workDir, tempDir)
        withContext(Dispatchers.IO) {
            if (tempDir.exists()) {
                deleteDir(tempDir)
            }
            tempDir.mkdir()
        }
        withContext(Dispatchers.Default) {
            val out = compiler.compile(file,desugaring)
            compiler.copyResourcesAndMove(file, out)
        }
    }

    private fun deleteDir(path: File) {
        path.listFiles()?.forEach {
            if (it.isFile) {
                it.delete()
            } else {
                if (it.isDirectory) {
                    deleteDir(it)
                }
            }
        }
        path.delete()
    }

}