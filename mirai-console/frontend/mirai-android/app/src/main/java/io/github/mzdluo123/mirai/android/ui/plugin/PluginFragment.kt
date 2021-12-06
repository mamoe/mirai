package io.github.mzdluo123.mirai.android.ui.plugin

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*

import android.widget.Toast
import androidx.appcompat.widget.PopupMenu

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_plugin.*
import java.io.File


class PluginFragment : Fragment() {

    private lateinit var pluginViewModel: PluginViewModel
    private lateinit var adapter: PluginsAdapter

    companion object {
        const val SELECT_RESULT_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pluginViewModel =
            ViewModelProvider(this).get(PluginViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_plugin, container, false)
        setHasOptionsMenu(true)
        adapter = PluginsAdapter()

        adapter.setOnItemClickListener { _, view, position ->
            val menu = PopupMenu(requireContext(), view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                menu.gravity = Gravity.END
            }
            menu.menuInflater.inflate(R.menu.plugin_manage, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        pluginViewModel.deletePlugin(position)
                        Toast.makeText(activity, "删除成功，重启后生效", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    else -> return@setOnMenuItemClickListener true
                }
            }
            menu.show()
        }

        pluginViewModel.pluginList.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        plugin_recycler.adapter = adapter
        plugin_recycler.layoutManager = LinearLayoutManager(activity)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plugin_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return false
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "application/java-archive"
        }

        startActivityForResult(intent, SELECT_RESULT_CODE)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == SELECT_RESULT_CODE && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().


            resultData?.data?.also { uri ->

                startActivity(
//                    Intent(activity, PluginImportActivity::class.java).putExtra(
//                        "uri",
//                        uri.toString()
//                    )
                    Intent(Intent.ACTION_VIEW, uri)
                )

            }
        }
    }

    override fun onResume() {
        super.onResume()
        pluginViewModel.refreshPluginList()
    }


}


class PluginsAdapter() :
    BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_plugin) {
    override fun convert(holder: BaseViewHolder, item: File) {
        holder.setText(R.id.pluginName_text, item.name)
        holder.setText(R.id.pluginSize_text, "${item.length() / 1024}kb")
    }
}