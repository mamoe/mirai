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
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

// ANDROID PHONE QQ

// 2020-12-23 20:16:57 D/soutv: PK =
//SvcReqMSFLoginNotifyData(iAppId=537066423,
//status=2,
//tablet=0,
//iPlatform=109,
//title=下线通知,
//info=你的帐号在手机上退出了,
//iProductType=0,
//iClientType=65799,
//vecInstanceList=[])

// ANDROID PHONE QQ
// 2020-12-23 20:21:02 D/soutv: PK = SvcReqMSFLoginNotifyData(
//iAppId=537066423,
//status=1,
//tablet=0,
//iPlatform=109, title=上线通知, info=你的帐号在手机上登录了, iProductType=0, iClientType=65799, vecInstanceList=[InstanceInfo(iAppId=537066423, tablet=0, iPlatform=109, iProductType=0, iClientType=65799)])

@Serializable
internal data class SvcReqMSFLoginNotifyData(
    @JvmField @TarsId(0) val iAppId: Long,
    @JvmField @TarsId(1) val status: Byte, // 上线=1, 下线=2
    @JvmField @TarsId(2) val tablet: Byte? = null,
    @JvmField @TarsId(3) val iPlatform: Long? = null,
    @JvmField @TarsId(4) val title: String? = "",
    @JvmField @TarsId(5) val info: String? = "",
    @JvmField @TarsId(6) val iProductType: Long? = null,
    @JvmField @TarsId(7) val iClientType: Long? = null,
    @JvmField @TarsId(8) val vecInstanceList: List<InstanceInfo>? = null
) : JceStruct
