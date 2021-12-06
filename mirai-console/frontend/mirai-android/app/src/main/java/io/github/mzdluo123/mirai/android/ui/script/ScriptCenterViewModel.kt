package io.github.mzdluo123.mirai.android.ui.script


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ooooonly.giteeman.GiteeFile
import io.github.mzdluo123.mirai.android.IdleResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalStdlibApi
class ScriptCenterViewModel() : ViewModel() {
    val fileList = MutableLiveData<List<GiteeFile>>()

    fun showFiles(parent: GiteeFile) {
        viewModelScope.launch {
            withContext(
                Dispatchers.IO
            ) {
                fileList.postValue(parent.listFiles())
                IdleResources.loadingData.decrement()
            }
        }
    }

}