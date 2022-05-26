/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

extern crate bindgen;
extern crate cbindgen;

use std::env;
use std::path::PathBuf;

use cbindgen::Config;
use cbindgen::Language::C;

fn main() {
    // let crate_dir = env::var("CARGO_MANIFEST_DIR").unwrap();

    // cbindgen::Builder::new()
    //     .with_crate(crate_dir)
    //     .with_language(C)
    //     .generate()
    //     .expect("Unable to generate bindings")
    //     .write_to_file("nativeInterop.h");


    println!("cargo:rustc-link-search=../../build/bin/native/debugShared");
    println!("cargo:rustc-link-lib=mirai_core_utils");
}