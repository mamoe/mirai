/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

// extern crate chashmap;
extern crate core;
extern crate flate2;
extern crate libc;
extern crate sha1;

/// cbindgen:ignore
mod bindings;
mod crypto;
mod chmap;

