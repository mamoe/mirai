package io.github.mzdluo123.mirai.android

import androidx.test.espresso.IdlingRegistry
import org.junit.After
import org.junit.Before

open class TestWithIdleResources {

    /**
     * 所有测试的基类
     * 请将所有测试继承这个类防止在未完成操作时进行下一步点击
     *
     * 测试时请使用英文系统
     *
     * */

    @Before
    fun before() {
        IdlingRegistry.getInstance().register(IdleResources.loadingData)
        IdlingRegistry.getInstance().register(IdleResources.botServiceLoading)
    }

    @After
    fun after() {
        IdlingRegistry.getInstance().unregister(IdleResources.loadingData)
        IdlingRegistry.getInstance().register(IdleResources.botServiceLoading)
    }
}