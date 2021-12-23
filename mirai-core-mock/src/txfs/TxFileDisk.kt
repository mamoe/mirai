/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.txfs

import net.mamoe.mirai.mock.internal.txfs.TxFileDiskImpl
import java.nio.file.Path

public interface TxFileDisk {
    public val availableSystems: Sequence<TxFileSystem>
    public fun newFsSystem(): TxFileSystem

    public companion object {
        @JvmStatic
        public fun newFileDisk(storage: Path): TxFileDisk {
            return TxFileDiskImpl(storage)
        }
    }
}
