package io.github.mzdluo123.mirai.android.ui.script

import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ooooonly.giteeman.GiteeFile
import io.github.mzdluo123.mirai.android.R

@ExperimentalStdlibApi
class ScriptCenterListAdapter(var listener: (GiteeFile) -> Unit) :
    BaseQuickAdapter<GiteeFile, BaseViewHolder>(R.layout.item_script_center_list) {
    override fun convert(holder: BaseViewHolder, item: GiteeFile) {
        with(holder) {
            setVisible(R.id.iv_file, item.isFile)
            setVisible(R.id.iv_folder, item.isDictionary)
            setText(R.id.tv_name, item.fileName)
            holder.getView<CardView>(R.id.cv_item).setOnClickListener {
                listener(item)
            }
        }
    }
}