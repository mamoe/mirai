/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

let fs = require('fs');
let child_process = require('child_process');

fs.writeFileSync("token.txt", JSON.stringify({
    'isSnapshot': 'true',
    'snapshot.user': process.env.SNAPSHOTS_PUBLISHING_USER,
    'snapshot.key': process.env.SNAPSHOTS_PUBLISHING_KEY,
    'snapshot.url': process.env.SNAPSHOTS_PUBLISHING_URL,
    'version.mirai.core': child_process.execSync('git rev-parse HEAD').toString().trim(),
    'version.mirai.console': child_process.execSync('git rev-parse HEAD', {
        cwd: 'mirai-console'
    }).toString().trim(),
}));
