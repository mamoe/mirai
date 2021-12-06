package io.github.mzdluo123.mirai.android.utils

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import io.github.mzdluo123.mirai.android.R
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.io.IOException

@Throws(IOException::class)
fun Context.copyToFileDir(uri: Uri, name: String, path: String): File {
    val plugin = File(path, name)
    plugin.createNewFile()
    val output = plugin.outputStream()
    this.contentResolver?.openInputStream(uri)?.use {
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (it.read(buf).also { bytesRead = it } > 0) {
            output.write(buf, 0, bytesRead)
        }
    }
    output.close()
    return plugin
}

fun formatFileLength(length: Long): String? {
    var length = length
    val suffixs = arrayOf("B", "K", "M", "G", "T", "P")
    var suffixIndex = 0
    var remain = 0f
    while (length >= 1024) {
        remain = (remain + length % 1024) / 1024
        length /= 1024
        suffixIndex++
    }
    return String.format("%.2f%s", remain + length, suffixs[suffixIndex])
}

suspend fun Context.askFileName(): String? {
        val name = CompletableDeferred<String?>()
        val view = View.inflate(this@askFileName, R.layout.dialog_ask_filename,null)
        val editText = view.findViewById<EditText>(R.id.filename_input)
        val dialog = AlertDialog.Builder(this@askFileName)
            .setView(view)
            .setPositiveButton("确定", DialogInterface.OnClickListener { _, _ ->
                name.complete(editText.text.toString())
            })
            .setNegativeButton(
                "取消", DialogInterface.OnClickListener { _, _ ->
                    name.complete(null)
                })
            .setCancelable(false)
            .setTitle("请输入文件名称")
            .create()
        dialog.show()
        return name.await()?.trim()
}