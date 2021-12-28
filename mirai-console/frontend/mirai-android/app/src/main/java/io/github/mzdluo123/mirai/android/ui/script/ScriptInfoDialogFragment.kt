package io.github.mzdluo123.mirai.android.ui.script

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHostFactory
import io.github.mzdluo123.mirai.android.utils.formatFileLength

class ScriptInfoDialogFragment(
    var scriptIndex: Int,
    var scriptViewModel: ScriptViewModel,
    var listener: ScriptInfoDialogFragmentListener
) : DialogFragment() {
    interface ScriptInfoDialogFragmentListener {
        fun onDeleteScript(index: Int)
        fun onSaveScript(index: Int)
        fun onReloadScript(index: Int)
        fun onOpenScript(index: Int)
        fun onEnableScript(index: Int)
        fun onDisableScript(index: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            scriptViewModel.hosts.value?.get(scriptIndex)?.let { info ->
                val builder = AlertDialog.Builder(it)
                val root =
                    requireActivity().layoutInflater.inflate(R.layout.dialog_script_info, null)
                builder.setTitle(info.name).setView(root)
                root.findViewById<TextView>(R.id.tv_script_info).text = buildString {
                    append("脚本类型：")
                    append(ScriptHostFactory.NAMES[info.scriptType])
                    append("\n大小：")
                    append(formatFileLength(info.fileLength))
                    append("\n作者：")
                    append(info.author)
                    append("\n版本：")
                    append(info.version)
                    append("\n说明：")
                    append(info.description)
                }
                val deleteBotton = root.findViewById<ImageButton>(R.id.btn_delete)
                val openBotton = root.findViewById<ImageButton>(R.id.btn_edit)
                val reloadBotton = root.findViewById<ImageButton>(R.id.btn_reload)
                val saveBotton = root.findViewById<ImageButton>(R.id.btn_save)
                val dialog = builder.create()
                deleteBotton.setOnClickListener {
                    listener.onDeleteScript(scriptIndex)
                    dialog.dismiss()
                }
                openBotton.setOnClickListener {
                    listener.onOpenScript(scriptIndex)
                    dialog.dismiss()
                }
                reloadBotton.setOnClickListener {
                    listener.onReloadScript(scriptIndex)
                    dialog.dismiss()
                }
                saveBotton.setOnClickListener {
                    listener.onSaveScript(scriptIndex)
                    dialog.dismiss()
                }
                dialog
            }

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}