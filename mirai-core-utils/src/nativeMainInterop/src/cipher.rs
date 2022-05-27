/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

use std::io::{BufReader, Read, Write};

use flate2::Compression;
use flate2::write::{DeflateDecoder, DeflateEncoder, GzDecoder, GzEncoder, ZlibDecoder, ZlibEncoder};
use libc::{malloc, read, size_t};
use sha1::{Digest, Sha1};
use sha1::digest::{Output, OutputSizeUser};
use sha1::digest::generic_array::GenericArray;

#[no_mangle]
#[repr(C)]
pub struct SizedByteArray {
    arr: *mut u8,
    size: u32,
}

#[no_mangle]
pub unsafe extern "C" fn mirai_hash_md5(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let result = md5::compute(data);
    let size = 16;
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}

#[no_mangle]
pub unsafe extern "C" fn mirai_hash_sh1(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let mut hasher = Sha1::new();
    hasher.update(data);
    let result = hasher.finalize();
    let size = 16;
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}


#[no_mangle]
pub unsafe extern "C" fn mirai_compression_gzip(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let mut encoder = GzEncoder::new(Vec::new(), Compression::default());

    let result = encoder.write_all(data).and_then(|_| { encoder.finish() });
    if result.is_err() { return false; }
    let result = result.unwrap();

    let size = result.len();
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}

#[no_mangle]
pub unsafe extern "C" fn mirai_compression_ungzip(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let mut encoder = GzDecoder::new(Vec::new());

    let result = encoder.write_all(data).and_then(|_| { encoder.finish() });
    if result.is_err() { return false; }
    let result = result.unwrap();

    let size = result.len();
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}

#[no_mangle]
pub unsafe extern "C" fn mirai_compression_deflate(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let mut encoder = ZlibEncoder::new(Vec::new(), Compression::default());

    let result = encoder.write_all(data).and_then(|_| { encoder.finish() });
    if result.is_err() { return false; }
    let result = result.unwrap();

    let size = result.len();
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}

#[no_mangle]
pub unsafe extern "C" fn mirai_compression_infalte(data: *const u8, len: u32, ret: &mut SizedByteArray) -> bool {
    let data = unsafe { std::slice::from_raw_parts(data, len as usize) };
    let mut encoder = ZlibDecoder::new(Vec::new());

    let result = encoder.write_all(data).and_then(|_| { encoder.finish() });
    if result.is_err() { return false; }
    let result = result.unwrap();

    let size = result.len();
    let mut memory = malloc(size).cast();
    memory.copy_from(result.as_ptr(), size);

    ret.arr = memory;
    ret.size = size as u32;
    return true;
}
