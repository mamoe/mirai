package io.github.mzdluo123.mirai.android.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.databinding.FragmentAboutBinding
import kotlinx.android.synthetic.main.fragment_about.*
import splitties.toast.toast

class AboutFragment : Fragment() {
    private lateinit var aboutBinding: FragmentAboutBinding
    private var click = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val aboutBinding = DataBindingUtil.inflate<FragmentAboutBinding>(
            layoutInflater,
            R.layout.fragment_about,
            container,
            false
        )
        aboutBinding.appVersion = requireContext().packageManager.getPackageInfo(
            requireContext().packageName,
            0
        ).versionName
        aboutBinding.coreVersion = BuildConfig.COREVERSION
        return aboutBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        github_btn.setOnClickListener {
            openUrl("https://github.com/mamoe/mirai")
        }
        github2_bth.setOnClickListener {
            openUrl("https://github.com/mzdluo123/MiraiAndroid")
        }
        imageView2.setOnClickListener {
            if (click < 4) {
                click++
                return@setOnClickListener
            }
            imageView2.setImageResource(R.drawable.avatar)
        }
        btn_join_group.setOnClickListener {
            if (!joinQQGroup("df6wSbKtDBo3cMJ9ULtYAZeln5ZZuA9d")) {
                toast("拉起QQ失败，请确认你是否安装了QQ")
            }
        }
    }


    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    /****************
     *
     * 发起添加群流程。群号：MiraiAndroid(1131127734) 的 key 为： df6wSbKtDBo3cMJ9ULtYAZeln5ZZuA9d
     * 调用 joinQQGroup(df6wSbKtDBo3cMJ9ULtYAZeln5ZZuA9d) 即可发起手Q客户端申请加群 MiraiAndroid(1131127734)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    private fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

}
