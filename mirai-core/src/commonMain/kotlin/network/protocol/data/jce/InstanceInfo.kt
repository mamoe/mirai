/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package  net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

@Serializable
internal data class InstanceInfo(
    @JvmField @TarsId(0) val iAppId: Int? = null,
    @JvmField @TarsId(1) val tablet: Byte? = null,
    @JvmField @TarsId(2) val iPlatform: Long? = null,
    /**
     * @see ClientKind
     */
    @JvmField @TarsId(3) val iProductType: Long? = null,
    @JvmField @TarsId(4) val iClientType: Long? = null
) : JceStruct
