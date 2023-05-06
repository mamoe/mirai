/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import io.ktor.utils.io.streams.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.internal.utils.ExternalResourceHolder
import net.mamoe.mirai.internal.utils.ExternalResourceLeakObserver
import net.mamoe.mirai.internal.utils.detectFileTypeAndClose
import net.mamoe.mirai.utils.AbstractExternalResource.ResourceCleanCallback
import java.io.InputStream

/**
 * 一个实现了基本方法的外部资源
 *
 * ## 实现
 *
 * [AbstractExternalResource] 实现了大部分必要的方法,
 * 只有 [ExternalResource.inputStream], [ExternalResource.size] 还未实现
 *
 * 其中 [ExternalResource.inputStream] 要求每次读取的内容都是一致的
 *
 * Example:
 * ```
 * class MyCustomExternalResource: AbstractExternalResource() {
 *      override fun inputStream0(): InputStream = FileInputStream("/test.txt")
 *      override val size: Long get() = File("/test.txt").length()
 * }
 * ```
 *
 * ## 资源释放
 *
 * 如同 mirai 内置的 [ExternalResource] 实现一样,
 * [AbstractExternalResource] 也会被注册进入资源泄露监视器
 * (即意味着 [AbstractExternalResource] 也要求手动关闭)
 *
 * 为了确保逻辑正确性, [AbstractExternalResource] 不允许覆盖其 [close] 方法,
 * 必须在构造 [AbstractExternalResource] 的时候给定一个 [ResourceCleanCallback] 以进行资源释放
 *
 * 对于 [ResourceCleanCallback], 有以下要求
 *
 * - 没有对 [AbstractExternalResource] 的访问 (即没有 [AbstractExternalResource] 的任何引用)
 *
 * Example:
 * ```
 * class MyRes(
 *      cleanup: ResourceCleanCallback,
 *      val delegate: Closable,
 * ): AbstractExternalResource(cleanup) {
 * }
 *
 * // 错误, 该写法会导致 Resource 永远也不会被自动释放
 * lateinit var myRes: MyRes
 * val cleanup = ResourceCleanCallback {
 *      myRes.delegate.close()
 * }
 * myRes = MyRes(cleanup, fetchDelegate())
 *
 * // 正确
 * val delegate: Closable
 * val cleanup = ResourceCleanCallback {
 *      delegate.close()
 * }
 * val myRes = MyRes(cleanup, delegate)
 * ```
 *
 * @since 2.9
 *
 * @see ExternalResource
 * @see AbstractExternalResource.setResourceCleanCallback
 * @see AbstractExternalResource.registerToLeakObserver
 */
@Suppress("MemberVisibilityCanBePrivate")
public abstract class AbstractExternalResource
@JvmOverloads
public constructor(
    displayName: String? = null,
    cleanup: ResourceCleanCallback? = null,
) : ExternalResource {

    public constructor(
        cleanup: ResourceCleanCallback? = null,
    ) : this(null, cleanup)

    public fun interface ResourceCleanCallback {
        @Throws(IOException::class)
        public fun cleanup()
    }

    override val md5: ByteArray by lazy { inputStream().md5() }
    override val sha1: ByteArray by lazy { inputStream().sha1() }
    override val formatName: String by lazy {
        inputStream().detectFileTypeAndClose() ?: ExternalResource.DEFAULT_FORMAT_NAME
    }

    private val leakObserverRegistered = atomic(false)

    /**
     * 注册 [ExternalResource] 资源泄露监视器
     *
     * 受限于类继承构造器调用顺序, [AbstractExternalResource] 无法做到在完成初始化后马上注册监视器
     *
     * 该方法以允许 实现类 在完成初始化后直接注册资源监视器以避免意外的资源泄露
     *
     * 在不调用本方法的前提下, 如果没有相关的资源访问操作, `this` 可能会被意外泄露
     *
     * 正确示例:
     * ```
     * // Kotlin
     * public class MyResource: AbstractExternalResource() {
     *      init {
     *          val res: SomeResource
     *          // 一些资源初始化
     *          registerToLeakObserver()
     *          setResourceCleanCallback(Releaser(res))
     *      }
     *
     *      private class Releaser(
     *          private val res: SomeResource,
     *      ) : AbstractExternalResource.ResourceCleanCallback {
     *          override fun cleanup() = res.close()
     *      }
     * }
     *
     * // Java
     * public class MyResource extends AbstractExternalResource {
     *      public MyResource() throws IOException {
     *          SomeResource res;
     *          // 一些资源初始化
     *          registerToLeakObserver();
     *          setResourceCleanCallback(new Releaser(res));
     *      }
     *
     *      private static class Releaser implements ResourceCleanCallback {
     *          private final SomeResource res;
     *          Releaser(SomeResource res) { this.res = res; }
     *
     *          public void cleanup() throws IOException { res.close(); }
     *      }
     * }
     * ```
     *
     * @see setResourceCleanCallback
     */
    protected fun registerToLeakObserver() {
        // 用户自定义 AbstractExternalResource 也许会在 <init> 的时候失败
        // 于是在第一次使用 ExternalResource 相关的函数的时候注册 LeakObserver
        if (leakObserverRegistered.compareAndSet(expect = false, update = true)) {
            ExternalResourceLeakObserver.register(this, holder)
        }
    }

    /**
     * 该方法用于告知 [AbstractExternalResource] 不需要注册资源泄露监视器。
     * **仅在我知道我在干什么的前提下调用此方法**
     *
     * 不建议取消注册监视器, 这可能带来意外的错误
     *
     * @see registerToLeakObserver
     */
    protected fun dontRegisterLeakObserver() {
        leakObserverRegistered.value = true
    }

    final override fun inputStream(): InputStream {
        registerToLeakObserver()
        return inputStream0()
    }

    protected abstract fun inputStream0(): InputStream

    /**
     * 修改 `this` 的资源释放回调。
     * **仅在我知道我在干什么的前提下调用此方法**
     *
     * ```
     * class MyRes {
     * // region kotlin
     *
     *      private inner class Releaser : ResourceCleanCallback
     *
     *      private class NotInnerReleaser : ResourceCleanCallback
     *
     *      init {
     *          // 错误, 内部类, Releaser 存在对 MyRes 的引用
     *          setResourceCleanCallback(Releaser())
     *          // 错误, 匿名对象, 可能存在对 MyRes 的引用, 取决于编译器
     *          setResourceCleanCallback(object : ResourceCleanCallback {})
     *          // 正确, 无 inner 修饰, 等同于 java 的 private static class
     *          setResourceCleanCallback(NotInnerReleaser(directResource))
     *      }
     *
     * // endregion kotlin
     *
     * // region java
     *
     *      private class Releaser implements ResourceCleanCallback {}
     *      private static class StaticReleaser implements ResourceCleanCallback {}
     *
     *      MyRes() {
     *          // 错误, 内部类, 存在对 MyRes 的引用
     *          setResourceCleanCallback(new Releaser());
     *          // 错误, 匿名对象, 可能存在对 MyRes 的引用, 取决于 javac
     *          setResourceCleanCallback(new ResourceCleanCallback() {});
     *          // 正确
     *          setResourceCleanCallback(new StaticReleaser(directResource));
     *      }
     *
     * // endregion java
     * }
     * ```
     *
     * @see registerToLeakObserver
     */
    protected fun setResourceCleanCallback(cleanup: ResourceCleanCallback?) {
        holder.cleanup = cleanup
    }

    private class UsrCustomResHolder(
        @JvmField var cleanup: ResourceCleanCallback?,
        private val resourceName: String,
    ) : ExternalResourceHolder() {

        override val closed: Deferred<Unit> = CompletableDeferred()

        override fun closeImpl() {
            cleanup?.cleanup()
        }

        // display on logger of ExternalResourceLeakObserver
        override fun toString(): String = resourceName
    }

    private val holder = UsrCustomResHolder(cleanup, displayName ?: buildString {
        append("ExternalResourceHolder<")
        append(this@AbstractExternalResource.javaClass.name)
        append('@')
        append(System.identityHashCode(this@AbstractExternalResource))
        append('>')
    })

    final override val closed: Deferred<Unit> get() = holder.closed.also { registerToLeakObserver() }

    @Throws(IOException::class)
    final override fun close() {
        holder.close()
    }

    @OptIn(MiraiInternalApi::class)
    @MiraiExperimentalApi
    override fun input(): Input {
        return inputStream().asInput()
    }
}