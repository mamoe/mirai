/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

/**
 * IntegrationTest 测试单元
 *
 * 每个被注册的单元都会在 console 启动的各个阶段调用相关的函数, 可以在相关函数执行测试代码
 *
 * ## 注册单元
 *
 * 每个单元都需要被注册, 即被添加进 [MiraiConsoleIntegrationTestLauncher.points]
 *
 * @see MiraiConsoleIntegrationTestLauncher
 * @see AbstractTestPointAsPlugin
 */
public abstract class AbstractTestPoint {
    /**
     * 本函数会在 console 启动前调用, 可以在此处进行环境配置
     */
    protected open fun beforeConsoleStartup() {}

    /**
     * 本函数会在 console 启动成功后立即调用, 可进行环境检查, 命令执行测试, 或更多
     */
    protected open fun onConsoleStartSuccessfully() {}

    // access
    internal companion object {
        internal fun AbstractTestPoint.internalOSS() {
            onConsoleStartSuccessfully()
        }

        internal fun AbstractTestPoint.internalBCS() {
            beforeConsoleStartup()
        }
    }
}
