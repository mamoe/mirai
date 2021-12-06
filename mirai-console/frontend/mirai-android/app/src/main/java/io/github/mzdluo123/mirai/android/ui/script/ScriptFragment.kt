package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHostFactory
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import io.github.mzdluo123.mirai.android.utils.askFileName
import kotlinx.android.synthetic.main.fragment_script.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.alertdialog.appcompat.*
import splitties.toast.toast

class ScriptFragment : Fragment(), ScriptInfoDialogFragment.ScriptInfoDialogFragmentListener {
    companion object {
        const val IMPORT_SCRIPT = 2
    }

    private lateinit var scriptViewModel: ScriptViewModel

    private val adapter: ScriptListAdapter by lazy {
        ScriptListAdapter(this)
    }

    private lateinit var botServiceConnection: ServiceConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        botServiceConnection = ServiceConnector(requireContext())
        lifecycle.addObserver(botServiceConnection)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_script, container, false).also {
        setHasOptionsMenu(true)
        adapter.setEmptyView(inflater.inflate(R.layout.fragment_script_empty, null).apply {
            findViewById<Button>(R.id.btn_script_center).setOnClickListener {
                findNavController().navigate(R.id.action_nav_scripts_to_nav_scripts_center)
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        script_recycler.adapter = adapter
        script_recycler.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        script_recycler.layoutManager = LinearLayoutManager(activity)
    }

    override fun onResume() {
        super.onResume()
        botServiceConnection.connectStatus.observe(this, Observer {
            if (it) {
                scriptViewModel = ScriptViewModel(botServiceConnection.botService)
                scriptViewModel.observe(viewLifecycleOwner, Observer {
                    adapter.data = it.toMutableList()
                    adapter.notifyDataSetChanged()
                })
                scriptViewModel.refreshScriptList()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_script, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> false
        R.id.action_add_script -> {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }, IMPORT_SCRIPT)
            true
        }
        R.id.action_script_center -> {
            findNavController().navigate(R.id.action_nav_scripts_to_nav_scripts_center)
            true
        }
        else -> true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == IMPORT_SCRIPT && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { importScript(it) }
        }
    }

    private fun importScript(uri: Uri) {
        uri.path ?: return
        val scriptType = ScriptHostFactory.getTypeFromSuffix(uri.path!!.split(".").last())
        if (scriptType != ScriptHostFactory.UNKNOWN) {
            importScript(uri, scriptType)
            return
        }
        context?.alertDialog {
            title = "未知脚本的后缀名，请手动选择脚本类型"
            setItems(arrayOf("Lua", "JavaScript", "Python", "KotlinScript")) { _, type ->
                importScript(uri, type + 1)
            }
        }
    }

    private fun importScript(uri: Uri, type: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = withContext(Dispatchers.Main) {
                requireActivity().askFileName()
            } ?: return@launch
            val scriptFile = ScriptManager.copyFileToScriptDir(requireContext(), uri, name)
            val result = scriptViewModel.createScriptFromFile(scriptFile, type)
            if (result) {
                context?.toast("导入成功，当前脚本数量：${scriptViewModel.hostSize}")
            } else {
                context?.toast("导入失败，请检查脚本是否有误！")
            }
        }

    }

    override fun onDeleteScript(index: Int) {
        context?.alertDialog {
            message = "删除脚本后无法恢复，是否确定？"
            okButton {
                scriptViewModel.deleteScript(index)
            }
            cancelButton { }
        }?.show()
    }

    override fun onSaveScript(index: Int) {

    }

    override fun onReloadScript(index: Int) {
        context?.alertDialog {
            message = "重新加载该脚本？"
            okButton {
                scriptViewModel.reloadScript(index)
                requireContext().toast("重载完毕")
            }
            cancelButton { }
        }?.show()
    }

    override fun onOpenScript(index: Int) {
        scriptViewModel.openScript(index)
    }

    override fun onEnableScript(index: Int) {
        scriptViewModel.enableScript(index)
        requireContext().toast("已启用")
    }

    override fun onDisableScript(index: Int) {
        scriptViewModel.disableScript(index)
        requireContext().toast("已禁用")
    }

    fun showScriptInfo(index: Int) {
        ScriptInfoDialogFragment(index, scriptViewModel, this).show(parentFragmentManager, "script")
    }

}