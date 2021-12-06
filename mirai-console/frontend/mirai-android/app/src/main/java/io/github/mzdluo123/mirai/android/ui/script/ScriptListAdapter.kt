package io.github.mzdluo123.mirai.android.ui.script

import android.util.Log
import android.widget.Switch
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHost

class ScriptListAdapter(var fragment: ScriptFragment) :
    BaseQuickAdapter<ScriptHost.ScriptInfo, BaseViewHolder>(R.layout.item_script) {
    override fun convert(holder: BaseViewHolder, item: ScriptHost.ScriptInfo) {
        with(holder) {
            setText(R.id.tv_script_alias, item.name)
            setText(R.id.tv_script_author, item.author)
            setText(R.id.tv_script_version, item.version)
            holder.getView<CardView>(R.id.cv_item).setOnClickListener {
                fragment.showScriptInfo(holder.layoutPosition)
            }
            holder.getView<Switch>(R.id.swt_enable).apply {
                Log.i("item.enable", item.enable.toString())
                setOnCheckedChangeListener(null)
                setChecked(item.enable)
                setOnCheckedChangeListener { _, check ->
                    Log.i("checkChange", check.toString())
                    if (check) fragment.onEnableScript(holder.layoutPosition)
                    else fragment.onDisableScript(holder.layoutPosition)
                }
            }
        }
    }
}