/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


let ostype = require('os').type();
let child_process = require('child_process');

if (ostype.toLowerCase().indexOf('windows') !== -1) {
    child_process.spawnSync('taskkill',
        ['/f', '/im', 'java*'],
        {
            stdio: "inherit"
        }
    );
} else {
    child_process.spawnSync('pkill',
        ['-9', 'java'],
        {
            stdio: "inherit"
        }
    );
}
