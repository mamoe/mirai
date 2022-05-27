/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

// use std::ops::DerefMut;
// use std::ptr::{null, null_mut};
//
// use chashmap::CHashMap;
// use libc::c_void;
// //
// #[no_mangle]
// pub extern "C" fn mirai_chmap_create() -> *mut c_void {
//     let map = CHashMap::<*mut c_void, *mut c_void>::new();
//     // Box::into_raw(Box::new(map))
//     return Box::into_raw(Box::new(map)) as *mut c_void;
// }
//
// #[no_mangle]
// pub unsafe extern "C" fn mirai_chmap_put(map: *const c_void, key: *const c_void, value: *const c_void) -> *const c_void {
//     let chmap = Box::from_raw(map as *mut CHashMap::<*const c_void, *const c_void>);
//     return chmap.insert(key, value).unwrap_or(null());
// }
