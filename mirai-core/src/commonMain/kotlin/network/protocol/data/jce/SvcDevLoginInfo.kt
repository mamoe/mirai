/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

@Serializable
internal data class SvcDevLoginInfo(
    @JvmField @TarsId(0) val iAppId: Long,
    // @JvmField @TarsId(1) val vecGuid: ByteArray? = null,
    @JvmField @TarsId(2) val iLoginTime: Long,
    @JvmField @TarsId(3) val iLoginPlatform: Long? = null, // 1: ios, 2: android, 3: windows, 4: symbian, 5: feature
    @JvmField @TarsId(4) val loginLocation: String? = "",
    @JvmField @TarsId(5) val deviceName: String? = "",
    @JvmField @TarsId(6) val deviceTypeInfo: String? = "",
    // @JvmField @TarsId(7) val stDeviceItemDes: DeviceItemDes? = null,
    @JvmField @TarsId(8) val iTerType: Long? = null, // 1:windows, 2: mobile, 3: ios
    @JvmField @TarsId(9) val iProductType: Long? = null, // always 0
    @JvmField @TarsId(10) val iCanBeKicked: Long? = null // isOnline
) : JceStruct

/*
vecCurrentLoginDevInfo=[SvcDevLoginInfo#1676411955 {
        deviceName=mirai
        deviceTypeInfo=mirai
        iAppId=0x000000002002E738(537061176)
        iCanBeKicked=0x0000000000000001(1)
        iLoginPlatform=0x0000000000000002(2)
        iLoginTime=0x000000005FE4A45C(1608819804)
        iProductType=0x0000000000000000(0)
        iTerType=0x0000000000000002(2)
}, SvcDevLoginInfo#1676411955 {
        deviceName=xxx的iPad
        deviceTypeInfo=iPad
        iAppId=0x000000002002FB7C(537066364)
        iCanBeKicked=0x0000000000000001(1)
        iLoginPlatform=0x0000000000000001(1)
        iLoginTime=0x000000005FE4A418(1608819736)
        iProductType=0x0000000000000000(0)
        iTerType=0x0000000000000003(3)
}, SvcDevLoginInfo#1676411955 {
        deviceName=Mi 10 Pro
        deviceTypeInfo=Mi 10 Pro
        iAppId=0x000000002002FBB7(537066423)
        iCanBeKicked=0x0000000000000001(1)
        iLoginPlatform=0x0000000000000002(2)
        iLoginTime=0x000000005FE4A628(1608820264)
        iProductType=0x0000000000000000(0)
        iTerType=0x0000000000000002(2)
}, SvcDevLoginInfo#1676411955 {
        deviceName=DESKTOP-KMQEB7V
        deviceTypeInfo=电脑
        iAppId=0x0000000000000001(1)
        iCanBeKicked=0x0000000000000001(1)
        iLoginPlatform=0x0000000000000003(3)
        iLoginTime=0x000000005FE4A5C1(1608820161)
        iProductType=0x0000000000000000(0)
        iTerType=0x0000000000000001(1)
        loginLocation=中国湖北省武汉市
}]
 */
@Serializable
internal class SvcReqGetDevLoginInfo(
    @JvmField @TarsId(0) val vecGuid: ByteArray,
    @JvmField @TarsId(1) val appName: String = "",
    @JvmField @TarsId(2) val iLoginType: Long = 1L,
    @JvmField @TarsId(3) val iTimeStamp: Long,
    @JvmField @TarsId(4) val iNextItemIndex: Long,
    @JvmField @TarsId(5) val iRequireMax: Long,
    @JvmField @TarsId(6) val iGetDevListType: Long? = 7L // 1: online list 2: recent list? 4: getAuthLoginDevList?
) : JceStruct

