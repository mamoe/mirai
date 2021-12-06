package io.github.mzdluo123.mirai.android.activity

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.databinding.ActivityPluginImportBinding
import io.github.mzdluo123.mirai.android.ui.plugin.PluginViewModel
import io.github.mzdluo123.mirai.android.utils.askFileName
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import kotlinx.android.synthetic.main.activity_plugin_import.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.mamoe.mirai.console.plugins.PluginDescription
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader


@ExperimentalUnsignedTypes
class PluginImportActivity : AppCompatActivity() {

    private lateinit var uri: Uri

    private lateinit var pluginViewModel: PluginViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var activityPluginImportBinding: ActivityPluginImportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin_import)
//        uri = Uri.parse(intent.getStringExtra("uri"))

        val errorHandel = CoroutineExceptionHandler { _, _ ->
            Toast.makeText(this, "无法打开这个文件，请检查这是不是一个合法的插件jar文件", Toast.LENGTH_SHORT).show()
            finish()

        }
        uri = intent.data ?: return
        pluginViewModel = ViewModelProvider(this).get(PluginViewModel::class.java)
        activityPluginImportBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_plugin_import)
        lifecycleScope.launch(errorHandel) { loadPluginData() }
        activityPluginImportBinding.importBtn.setOnClickListener {
            startImport()
        }

    }

    private fun createDialog() {
        dialog = AlertDialog.Builder(this)
            .setTitle("正在编译")
            .setMessage("这可能需要一些时间，请不要最小化")
            .setCancelable(false)
            .create()
    }

    private fun startImport() {
        createDialog()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch(Dispatchers.Main) {
                dialog.dismiss()
                Toast.makeText(
                    this@PluginImportActivity,
                    "无法编译插件 \n${throwable}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }



        dialog.show()
        when (import_radioGroup.checkedRadioButtonId) {
            R.id.compile_radioButton -> {
                lifecycleScope.launch(exceptionHandler) {
                    val name = withContext(Dispatchers.Main) {
                        askFileName()
                    } ?: return@launch
                    withContext(Dispatchers.IO) {
                        copyToFileDir(
                            uri,
                            name,
                            this@PluginImportActivity.getExternalFilesDir(null)!!.absolutePath
                        )
                    }
                    pluginViewModel.compilePlugin(
                        File(baseContext.getExternalFilesDir(null), name),
                        desugaring_checkBox.isChecked
                    )
                    withContext(Dispatchers.IO) {
                        File(this@PluginImportActivity.getExternalFilesDir(null), name).delete()
                    }
                    dialog.dismiss()
                    Toast.makeText(this@PluginImportActivity, "安装成功,重启后即可加载", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
            R.id.copy_radioButton -> {
                lifecycleScope.launch(exceptionHandler) {
                    val name = withContext(Dispatchers.Main) {
                        askFileName()
                    } ?: return@launch
                    withContext(Dispatchers.IO) {
                        copyToFileDir(
                            uri,
                            name,
                            this@PluginImportActivity.getExternalFilesDir("plugins")!!.absolutePath
                        )
                    }
                    dialog.dismiss()
                    Toast.makeText(this@PluginImportActivity, "安装成功,重启后即可加载", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }

    }


    private suspend fun loadPluginData() = withContext(Dispatchers.IO) {
        val realFileName = "tmpfile.jar"
        baseContext.copyToFileDir(uri, realFileName, cacheDir.absolutePath)
        val cacheFile = File(cacheDir.absolutePath, realFileName)
        val zipFile = ZipFile(cacheFile)
        zipFile.extractFile("plugin.yml", cacheDir.absolutePath)

        val yml = Yaml()
        lateinit var plugInfo: PluginDescription
        FileReader(File(cacheDir.absolutePath, "plugin.yml")).use {
            with(yml.load<LinkedHashMap<String, Any>>(it)) {
                plugInfo = PluginDescription(
                    file = File(cacheDir.absolutePath, "tmpfile.jar"),
                    name = this.get("name") as String,
                    author = kotlin.runCatching {
                        this.get("author") as String
                    }.getOrElse {
                        "unknown"
                    },
                    basePath = kotlin.runCatching {
                        this.get("path") as String
                    }.getOrElse {
                        this.get("main") as String
                    },
                    version = kotlin.runCatching {
                        this.get("version") as String
                    }.getOrElse {
                        "unknown"
                    },
                    info = kotlin.runCatching {
                        this.get("info") as String
                    }.getOrElse {
                        "unknown"
                    },
                    depends = kotlin.runCatching {
                        this.get("depends") as List<String>
                    }.getOrElse {
                        listOf()
                    }
                )

            }
            withContext(Dispatchers.Main) {
                activityPluginImportBinding.pluginData = plugInfo
            }

        }


    }
}

