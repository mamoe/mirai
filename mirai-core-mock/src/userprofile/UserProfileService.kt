/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.userprofile

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.userprofile.MockUserProfileBuilder.Companion.invoke
import net.mamoe.mirai.utils.runBIO
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 用户资料服务, 用于 [IMirai.queryProfile] 查询用户资料
 *
 * implementation note: Java 请实现 [JUserProfileService]
 *
 * @see MockBot.userProfileService
 * @see MockUserProfileBuilder
 */
@JvmBlockingBridge
public interface UserProfileService {
    public suspend fun doQueryUserProfile(id: Long): UserProfile

    /**
     * 将 [id] 的用户资料指定为 [profile]
     *
     * implementation note:
     *
     * 框架内部并不会使用此接口, 该接口是设计于测试单元动态注册 [UserProfile],
     * 如无调用此接口的需求可以实现为 `throw new UnsupportedOperationException()`
     */
    public suspend fun putUserProfile(id: Long, profile: UserProfile)

    public companion object {
        @JvmStatic
        public fun getInstance(): UserProfileService {
            return UserProfileServiceImpl()
        }
    }
}

/**
 * 用于资料服务, 用于 [IMirai.queryProfile] 查询用户资料
 *
 * 该接口是为了方便 Java 实现 [UserProfileService],
 * kotlin 请实现 [UserProfileService]
 */
@Suppress("ILLEGAL_JVM_NAME", "INAPPLICABLE_JVM_NAME")
public interface JUserProfileService : UserProfileService {
    override suspend fun doQueryUserProfile(id: Long): UserProfile {
        return runBIO {
            doQueryUserProfileJ(id) ?: buildUserProfile { }
        }
    }

    override suspend fun putUserProfile(id: Long, profile: UserProfile) {
        runBIO {
            putUserProfileJ(id, profile)
        }
    }

    // override UserProfileService @JvmBlockingBridge
    @JvmName("doQueryUserProfile")
    public fun doQueryUserProfileJ(id: Long): UserProfile?

    @JvmName("putUserProfile")
    public fun putUserProfileJ(id: Long, profile: UserProfile)
}

/**
 * [UserProfile] 的构造器
 *
 * @see [invoke]
 * @see [buildUserProfile]
 */
public interface MockUserProfileBuilder {
    public fun build(): UserProfile

    public fun nickname(value: String): MockUserProfileBuilder
    public fun email(value: String): MockUserProfileBuilder
    public fun age(value: Int): MockUserProfileBuilder
    public fun qLevel(value: Int): MockUserProfileBuilder
    public fun sex(value: UserProfile.Sex): MockUserProfileBuilder
    public fun sign(value: String): MockUserProfileBuilder
    public fun friendGroupId(value: Int): MockUserProfileBuilder

    public companion object {
        @JvmStatic
        @JvmName("newBuilder")
        public operator fun invoke(): MockUserProfileBuilder = MockUPBuilderImpl()
    }
}

/**
 * 构造一个 [UserProfile]
 *
 * @see MockUserProfileBuilder
 */
public inline fun buildUserProfile(block: MockUserProfileBuilder.() -> Unit): UserProfile {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return MockUserProfileBuilder().apply(block).build()
}

internal class MockUPBuilderImpl : MockUserProfileBuilder, UserProfile {
    override var nickname: String = ""
    override var email: String = ""
    override var age: Int = -1
    override var qLevel: Int = -1
    override var sex: UserProfile.Sex = UserProfile.Sex.UNKNOWN
    override var sign: String = ""
    override var friendGroupId: Int = 0

    // unmodifiable
    override fun build(): UserProfile {
        return object : UserProfile by this {}
    }

    override fun nickname(value: String): MockUserProfileBuilder = apply {
        nickname = value
    }

    override fun email(value: String): MockUserProfileBuilder = apply {
        email = value
    }

    override fun age(value: Int): MockUserProfileBuilder = apply {
        age = value
    }

    override fun qLevel(value: Int): MockUserProfileBuilder = apply {
        qLevel = value
    }

    override fun sex(value: UserProfile.Sex): MockUserProfileBuilder = apply {
        sex = value
    }

    override fun sign(value: String): MockUserProfileBuilder = apply {
        sign = value
    }

    override fun friendGroupId(value: Int): MockUserProfileBuilder = apply {
        friendGroupId = value
    }

}

internal class UserProfileServiceImpl : UserProfileService {
    val db = ConcurrentHashMap<Long, UserProfile>()
    val def = buildUserProfile {
    }

    override suspend fun doQueryUserProfile(id: Long): UserProfile {
        return db[id] ?: def
    }

    override suspend fun putUserProfile(id: Long, profile: UserProfile) {
        db[id] = profile
    }

}
