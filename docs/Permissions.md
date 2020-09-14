# Mirai Console Backend - Permissions

权限系统。

[`PermissionService`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/PermissionService.kt
[`Permission`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/Permission.kt
[`RootPermission`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/Permission.kt#L82
[`PermissionId`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/PermissionId.kt
[`PermissionIdNamespace`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/PermissionIdNamespace.kt
[`Permittee`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/Permittee.kt
[`PermitteeId`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/PermitteeId.kt
[`AbstractPermitteeId`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/permission/PermitteeId.kt#L77
[`CommandSender`]: ../backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/command/CommandSender.kt

## 权限

每个权限都由 [`Permission`] 对象表示。

一个 [`Permission`] 拥有这些信息：
```kotlin
interface Permission {
    val id: PermissionId // 唯一识别 ID
    val description: String // 描述信息
    val parent: Permission // 父权限
}
```

「权限」表示的意义是 “做一项工作的能力”。如 “执行指令 /stop”，“操作数据库” 都叫作权限。

[`Permission`] 对象由 Console 内置或者由特定权限插件实现。其他插件不能实现 [`Permission`] 接口。

### 权限 ID

```kotlin
data class PermissionId(
    val namespace: String, // 命名空间
    val name: String, // 名称
)
```

[`PermissionId`] 是 [`Permission`] 的唯一标识符。知道 [`PermissionId`] 就可以获取到对应的 [`Permission`]。

字符串表示为 "$namespace:$name"，如 "console:command.stop", "*:*"

#### 命名空间

命名空间（“namespace”）用于限定权限的创建者，避免冲突。

一些常见命名空间：

| 用途          | 命名空间      |
|:-------------|:------------|
| 根权限         | `"*"`       |
| Console 内置  | `"console"` |
| ID 为 A 的插件 | `"A"`       |

#### 名称

名称则表示特定的含义。如一个指令，某一项操作等。

一些常见名称：

| 用途                       | 名称           |
|:--------------------------|:--------------|
| 根权限                     | `"*"`         |
| Console 内置的名为 A 的指令  | `"command.A"` |
| ID 为 A 的插件的名为 B 的指令 | `"command.B"` |

#### 根权限

[`RootPermission`] 是所有权限的父权限。其 ID 为 "*:*"

## 被许可人

```kotlin
interface Permittee {
    val permitteeId: PermitteeId
}
```

[`Permittee`] 表示一个可被赋予权限的对象，即 '被许可人'。

[`CommandSender`] 实现 [`Permittee`]。

被许可人自身不持有拥有的权限列表，而是拥有 [`PermitteeId`]，标识自己的身份，供 [权限服务][`PermissionService`] 处理。

**注意**：请不要自主实现 [`Permittee`]。

### 被许可人 ID

```kotlin
interface PermitteeId {
    val directParents: Array<out PermitteeId> // 直接父对象
    fun asString(): String // 转换为字符串表示
}
````

[`PermitteeId`] 是被许可人的标识符。

一个这样的标识符即可代表特定的单个 [`Permittee`], 也可以表示多个同类 [`Permittee`].

#### `directParents`
[`PermitteeId`] 允许拥有多个父对象。在检查权限时会首先检查自己, 再递归检查父类。

#### 衍生类型

[`PermitteeId`] 的实现通常是 [`AbstractPermitteeId`]

在 [`AbstractPermitteeId`] 查看其子类。

#### 字符串表示

当使用 `PermitteeId.asString` 时, 不同的类型的返回值如下表所示. 这些格式也适用于 [权限指令](#使用内置权限服务指令).  
(不区分大小写. 不区分 Bot).

|    被许可人类型    | 字符串表示示例 | 备注                                  |
|:----------------:|:-----------:|:-------------------------------------|
|      控制台       |   console   |                                      |
|      精确群       |   g123456   | 表示群, 而不表示群成员                   |
|      精确好友      |   f123456   | 必须通过好友消息                        |
|    精确临时会话    | t123456.789  | 群 123456 内的成员 789. 必须通过临时会话  |
|     精确群成员     | m123456.789 | 群 123456 内的成员 789. 同时包含临时会话. |
|      精确用户      |   u123456   | 同时包含群成员, 好友, 临时会话            |
|      任意群       |     g*      |                                      |
|  任意群的任意群员   |     m*      |                                      |
|  精确群的任意群员   |  m123456.*  | 群 123456 内的任意成员. 同时包含临时会话.  |
| 任意群的任意临时会话 |     t*      | 必须通过临时会话                        |
| 精确群的任意临时会话 |  t123456.*  | 群 123456 内的任意成员. 必须通过临时会话   |
|      任意好友      |     f*      |                                      |
|      任意用户      |     u*      | 任何人在任何环境                        |
|      任意对象      |      *      | 即任何人, 任何群, 控制台                 |

### 获取被许可人

通常使用 `CommandSender.permitteeId`。  
也可以直接构造 [`AbstractPermitteeId`] 的子类。或者在 Kotlin 使用扩展 `User.permitteeId`

## 权限服务

[`PermissionService`] 承载权限的授权和管理。Console 内置一个实现，而权限服务可以由插件提供（见 [扩展](Extensions.md)）。

### 判断权限

在 Kotlin，在该有扩展的对象上 Console 都为它们实现了扩展。可以使用：
```kotlin
fun Permittee.hasPermission(Permission): Boolean
fun Permission.testPermission(Permittee): Boolean
fun PermitteeId.hasPermission(Permission): Boolean
fun PermissionId.testPermission(Permittee): Boolean
fun Permittee.hasPermission(PermissionId): Boolean
fun Permission.testPermission(PermitteeId): Boolean
// ... 
```

请查看 [`PermissionService`] 中的伴生对象。

### 注册权限

每一条指令都会默认自动创建一个权限。

如果希望手动注册一个其他用途的权限，使用 `PermissionService.register`。

**注意**：权限只能在插件 [启用](Plugins.md#启用) 之后才能注册。否则会得到一个异常。

### 使用内置权限服务指令

**根指令**: "/permission", "/perm", "/权限"

```
/permission cancel <被许可人 ID> <权限 ID>   取消授权一个权限
/permission cancelall <被许可人 ID> <权限 ID>   取消授权一个权限及其所有子权限
/permission listpermissions    查看所有权限列表
/permission permit <被许可人 ID> <权限 ID>   授权一个权限
/permission permittedpermissions <被许可人 ID>   查看被授权权限列表
```

其中, 被许可人 ID 使用 [字符串表示](#字符串表示), 权限 ID 参见 [权限 ID](#权限-id)
