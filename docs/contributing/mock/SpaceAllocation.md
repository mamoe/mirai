# TmpFsServer

> 此文件是关于 `mirai-core-mock` 是如何分配文件资源路径的具体说明
> 
> 所有的临时资源的路径 (包括: 图片, 语音, 群文件, etc.)
>
> 注:
> - 此文档说明只适用于默认实现

## 通用资源

TmpFsServer 会为每一个上传的文件分配一个 ResourceID, 规则如下

```text
${resource.size}-${resource.sha1.hex()}-${resource.md5.hex()}
```

数据资源会直接拷贝至 `/storage/$resourceId`

## 图片资源

图片资源除了 ResourceID 外, 还拥有一个 `ImageID`,
mirai-core-api 中 ImageID 的格式为 `{XXXXXX}.yyy`, 而 TmpFsServer 仅截取其中 `XXXX` 部分作为 `ImageID`

在完成资源上传后, TmpFsServer 会额外将 `/images/$imgId` 链接到 `/storage/$resourceId`

# ServerFileDisk / ServerFileSystem (群文件管理系统)

每一个群都有一个自己的 `ServerFileSystem`, 每一个 `ServerFileSystem` 都会分配一块区域存储自己的数据, 路径为

`/fs-disk/${UUID.randomUUID()}` (下文简记为 `/sfs`)

由于群文件的特殊性, 群文件采用随机 UUID 作为文件名

> 特殊性: 群文件允许同名文件的存在

群文件数据结构如下

```text
/sfs/details/root/....
/sfs/details/fileN/....
/sfs/details/dirN/....
/sfs/root/fileN....
/sfs/dirN/fileN....
/sfs/fileN.....
```

## 普通文件

对于普通文件, ServerFileSystem 会随机分配一个 ID, 并将数据拷贝至 `/sfs/$id`,
然后在此文件所在的文件夹创建一个同名空文件, 并生成相关的 details 信息

> 如: 如果是在根目录, 则会创建 `/sfs/root/$id`,
>
> 如果是在目录 `testdir`(实际是随机 UUID), 则会创建 `/sfs/testdir/$id`

## 目录

对于目录, 相关的行为与普通文件一样。
唯一的区别是， `/sfs/$id` 是一个目录而不是一个文件

## details 信息

`/sfs/details/$id/....` 存有每一个文件的详细信息 (对于根目录, `/sfs/details/root/...`)

其中的文件意义为:

- `~/parent` 此文件所在的文件夹
- `~/name` 此 文件/文件夹 的名字, 对于根目录永远为 `""`
- `~/creator` 此 文件/文集夹 的创建者, 编码为 二进制 big-endian int64
- `~/createTime` 此 文件/文件夹 的创建时间, 编码为 二进制 big-endian int64
