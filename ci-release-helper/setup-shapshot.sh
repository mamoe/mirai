#!/usr/bin/env bash

#
# Copyright 2019-2021 Mamoe Technologies and contributors.
#
# 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
# Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
#
# https://github.com/mamoe/mirai/blob/dev/LICENSE
#

echo "isSnapshot=true" > keys.properties

echo "snapshot.user=$SNAPSHOTS_PUBLISHING_USER" >> keys.properties
echo "snapshot.key=$SNAPSHOTS_PUBLISHING_KEY"   >> keys.properties
echo "snapshot.url=$SNAPSHOTS_PUBLISHING_URL"   >> keys.properties
echo "snapshot.remote=$( echo "$SNAPSHOTS_PUBLISHING_URL" | base64 )"

tmp=$(git rev-parse HEAD)

echo "version.mirai.core=$tmp" >> keys.properties

cd mirai-console || exit
tmp=$(git rev-parse HEAD)
cd ..
echo "version.mirai.console=$tmp" >> keys.properties
