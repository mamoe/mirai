#
# Copyright 2019-2021 Mamoe Technologies and contributors.
#
#  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
#  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
#
#  https://github.com/mamoe/mirai/blob/master/LICENSE
#

find docs -type f -not -name 'config.js' -delete

cp -v -r ../docs .
mkdir docs/console
mkdir node_modules

cp -v -r ../mirai-console/docs/ -T docs/console/

cp -v -r docs/console/.images -T node_modules/.images
cp -v -r docs/console/.ConfiguringProjects_images -T node_modules/.ConfiguringProjects_images

mkdir -p docs/tools/intellij-plugin/resources
cp -r -v ../mirai-console/tools/intellij-plugin/resources -T docs/tools/intellij-plugin/resources

find docs/console -type f -name "*.md" -exec sed -i -r "s+\.\./+https://github.com/mamoe/mirai-console/tree/master/+g" {} \;
sed -i "s+https://github.com/mamoe/mirai/tree/dev/docs+../README.md+g" docs/console/README.md

yarn install
yarn docs:build
rm -rf node_modules/.images
rm -rf node_modules/.ConfiguringProjects_images
