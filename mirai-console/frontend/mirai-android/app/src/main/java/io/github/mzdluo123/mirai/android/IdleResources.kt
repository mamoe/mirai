package io.github.mzdluo123.mirai.android

import androidx.test.espresso.idling.CountingIdlingResource

object IdleResources {

    // Android单元测试所需要的东西

    val loadingData by lazy { CountingIdlingResource("logUploadDialogIdleResources") }

    val botServiceLoading by lazy { CountingIdlingResource("botServiceLoading") }
}