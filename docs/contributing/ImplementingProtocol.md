# 实现协议

本文将说明实现一些协议的通常步骤，并以示例逐步演示。本文还将介绍 mirai 的消息处理流程。

通常请于 `commonMain` 编写代码。`commonMain` 的 Kotlin
代码将可以用于全平台，即同时（自动）支持 JVM 和 Native 目标。

## 添加一个消息类型

### 1. 抽象接口

在 `mirai-core-api` 的 `message.data` 中添加新类型接口（消息元素），如 `Audio`
，`FileMessage`。

在设计消息元素时应使其继承 `MessageMetadata`（元数据） 或 `MessageContent`
（内容）。若该消息在官方客户端中只能单独出现，那么还应该让它继承 `ConstrainSingle` 来维持这一性质。

消息元素应是不可变的，即不允许出现 `var` 或可变类型。

作为示例，假设现在要支持视频消息，在 `mirai-core-api` 定义接口 `Video`：

```kotlin
public interface Video : MessageContent, ConstrainSingle {
    override val key: Key get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, Video>(
            MessageContent,
            { it.safeCast() })

    /**
     * 文件名
     */
    val filename: String

    /**
     * 长度, 单位为秒.
     */
    val length: Long
}
```

由于视频不可与文字等其他消息内容元素同时出现，`Video` 需要实现 `ConstrainSingle`
，并提供 `companion object Key`。

为了实现最好的协议支持，我们将 `Video` 分为 `OnlineVideo` 和 `OfflineVideo`。`OnlineVideo`
表示从服务器接收的，而 `OfflineVideo` 本地构造的。之后在实现时 `OnlineVideo`
可以存储来自服务器的原数据，以实现快速转发等优化。

```kotlin
public sealed interface Video : MessageContent, ConstrainSingle {
    override val key: Key get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, Video>(
            MessageContent,
            { it.safeCast() })

    /**
     * 文件名
     */
    val filename: String

    /**
     * 长度, 单位为秒.
     */
    val length: Long
}

@NotStableForInheritance // 表示此 API 不可由用户自行实现
public interface OnlineVideo : Video {
    override val key: Key get() = Key

    /**
     * 获取下载链接 URL
     */
    val urlForDownload: String

    // 需要覆盖 Key
    public companion object Key :
        AbstractPolymorphicMessageKey<Video, OnlineVideo>(
            Video, { it.safeCast() }
        ) {
        // SERIAL_NAME为之后支持序列化做准备
        public const val SERIAL_NAME: String = "OnlineVideo"
    }
}

@NotStableForInheritance // 表示此 API 不可由用户自行实现
public interface OfflineVideo : Video {
    override val key: Key get() = Key

    // 需要覆盖 Key
    public companion object Key :
        AbstractPolymorphicMessageKey<Video, OfflineVideo>(
            Video, { it.safeCast() }
        ) {
        // SERIAL_NAME为之后支持序列化做准备
        public const val SERIAL_NAME: String = "OfflineVideo"
    }
}
```

这样设计接口的好处包括未来很容易修改。例如 mirai 2.0 设计的 `Image`
接口一直维护到现在（2.13）也没有遇到问题，其内部结构也变更了多次，但用户的使用方法没有改变。

### 2. 提供工厂或构建器

考虑添加相应的 `Factory`（抽象工厂）或 `Builder`（生成器）来提供构造消息实例的方式，而避免直接提供构造器。
直接提供公开构造器是一种沉重的承诺，将增加维护难度（现在已有的部分公开构造器是历史遗留）。

在 `Video` 示例中，由于 `OnlineVideo` 是来自服务器的，我们无序提供构造方式。可为 `OfflineVideo`
增加构建工厂：

```kotlin
// ...
public interface OfflineVideo : Video {
    // ...

    public interface Factory {
        /** 构建一个 [OfflineVideo] 实例 */
        public fun create(filename: String, length: Long): OfflineVideo

        companion object INSTANCE : Factory by loadService()
        // `loadService` 是 mirai 实现的全平台服务加载方式，类似于 JDK `ServiceLoader`。
    }
}
```

用户在 Kotlin 可通过 `OfflineVideo.Factory.create(filename, length)`，或在 Java
通过 `OfflineVideo.Factory.INSTANCE.create(filename, length)`
取得 `OfflineVideo` 实例。

注意，示例中 `OnlineVideo` 用的属性并不多（只有两个），使用工厂会比使用生成器（`Builder`）方便。但对于 `Image`
等拥有众多属性的类型，建议提供 `Builder`。一种实现方式可参见 `Image.Builder`。

### 3. 实现接口

接口和工厂准备就绪，现在在 `mirai-core` 的 `net.mamoe.mirai.internal.message.data.`
包下实现接口。

请一般使用 `...Impl`
的命名方式，并为抽象类（`abstract class`）增加 `Abstract` 前缀。
如本示例中我们将实现：

- `abstract class AbstractVideo : Video` — 实现基本的 `Video` 类
- `class OnlineVideoImpl : OnlineVideo` — 实现 `OnlineVideo` 的特殊内容
- `class OfflineVideoImpl : OfflineVideo` — 实现 `OfflineVideo` 的特殊内容

```kotlin
internal sealed class AbstractVideo(
    override val filename: String,
    override val length: Long,
) : Video {
    private val toStringTemp by lazy { "[mirai:video:$filename, length=$length]" } // 这不是标准 mirai code，只是为了提供易读的表示方式
    override fun toString(): String = toStringTemp

    override fun contentToString(): String = "[视频]"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        // isSameType 是适用于全平台的类型判断方式
        if (!isSameType(this, other)) return false
        if (this.filename != other.filename) return false
        if (this.length != other.length) return false
        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + length.hashCode()
        return result
    }

    abstract fun toVideoFile(): ImMsgBody.VideoFile
}

internal class OnlineVideoImpl(
    override val filename: String,
    override val length: Long,
    private val original: ImMsgBody.VideoFile? // 协议数据结构
) : OnlineVideo, AbstractVideo() {
    override fun toVideoFile(): ImMsgBody.VideoFile = original
}

internal class OfflineVideoImpl(
    override val filename: String,
    override val length: Long,
) : OfflineVideo, AbstractVideo() {
    override fun toVideoFile(): ImMsgBody.VideoFile = /* ... */
}
```

### 4. 实现工厂

因为 mirai 在 JVM 使用 `ServiceLoader`，需要定义工厂实现类为 `class` 而不是 `object`。

对于 `Video` 示例：

```kotlin
internal class OfflineVideoFactoryImpl : OfflineVideo.Factory {
    override fun create(filename: String, length: Long): OfflineVideo =
        OfflineVideoImpl(filename, length)
}
```

实现后需要同时为 JVM 和 Native 注册工厂：

- JVM：在 jvmBaseMain/resources/META-INF/services/
  新建一个文件名为 `net.mamoe.mirai.message.data.OfflineVideo$Factory`
  ，文件内容为 `net.mamoe.mirai.internal.message.data.OfflineVideoFactoryImpl`
  ；
- Native：
  在 `net.mamoe.mirai.internal.utils.MiraiCoreServices.registerAll`
  末尾增加。如对于 `Video` 示例：

  ```kotlin
  Services.register(
      "net.mamoe.mirai.message.data.OfflineVideo.Factory",
      "net.mamoe.mirai.internal.message.data.OfflineVideoFactoryImpl"
  ) { net.mamoe.mirai.internal.message.data.OfflineVideoFactoryImpl() }
  ```

### 5. 测试接口和实现

请在 `commonTest/kotlin/message/data` 添加新增消息类型的测试。
需要测试的内容可能包含：

- `equals`、`hashCode`
- `toString`、`contentToString`
- 检验参数有效性

语音消息的测试 `net.mamoe.mirai.internal.message.data.AudioTest` 可供参考。

### 6. 实现收发协议

#### 多流水线系统

mirai 的多套系统都使用了"流水线"（pipeline）设计模式。

服务器消息实际上是数种通知中的一种（mirai 称之为 `notice`）。
加密的通知经过*网络层*解密处理后成为二进制数据包 `IncomingPacket`，网络层会分类该 `IncomingPacket`给合适的*
数据包工厂*（`PacketFactory`）处理。
数据包工厂会解析二进制数据包，产生结构数据类型。
结构数据类型将被发送至*通知流水线*（`NoticeProcessorPipeline`），经由负责的*流水线处理员*
（`NoticeProcessor`）处理后最终成为一个 mirai
事件。而负责处理消息事件的处理员会将消息原数据发送至消息流水线处理得到 `MessageChain`。

#### 群消息处理流程

一条群消息的完整处理流程如下图所示：

[![](https://mermaid.ink/img/pako:eNpVkctKw0AUQH9lmFWEth8QqNCmqbgoFnSZzZjcJoPJTEkmgjQFK_hYaNGFIiK6kOLOVvABivg1mehfOElTxdUdzj1zuY8BtrkDWMduSPoe2mhZDKGGlk4P5MWhPJ_Jk4f0eH8pp03NwqvM5gFlbpfYWyA0eTn-r-HCNJTZidw15lMG3Tjyat3NPCimMS6oDQuzpcyVkMf9DkQRccHcBibKnDmvYvAgqKlY0nZO57LhEcpK3EDV6nKSfZxl79fp497X_Z0aIUHNovEi99tl-jpJx7khb04TZBQNF8b37lV2O5HPIzl7yt4-E7NeV99ejuRo-gfbykRqS7iCAwgDQh21vEFexMLCgwAsrKunAz0S-2oUiw2VGvcdIsB0qOAh1nvEj6CCSSz4-g6zsS7CGBZSixJ1i6C0hj_Pm6rs)](https://mermaid-js.github.io/mermaid-live-editor/edit#pako:eNpVkctKw0AUQH9lmFWEth8QqNCmqbgoFnSZzZjcJoPJTEkmgjQFK_hYaNGFIiK6kOLOVvABivg1mehfOElTxdUdzj1zuY8BtrkDWMduSPoe2mhZDKGGlk4P5MWhPJ_Jk4f0eH8pp03NwqvM5gFlbpfYWyA0eTn-r-HCNJTZidw15lMG3Tjyat3NPCimMS6oDQuzpcyVkMf9DkQRccHcBibKnDmvYvAgqKlY0nZO57LhEcpK3EDV6nKSfZxl79fp497X_Z0aIUHNovEi99tl-jpJx7khb04TZBQNF8b37lV2O5HPIzl7yt4-E7NeV99ejuRo-gfbykRqS7iCAwgDQh21vEFexMLCgwAsrKunAz0S-2oUiw2VGvcdIsB0qOAh1nvEj6CCSSz4-g6zsS7CGBZSixJ1i6C0hj_Pm6rs)

#### 消息流水线

**要增加新的消息类型支持，我们需要扩展 *消息流水线*（`MessagePipeline`）**。

事实上为了支持复杂的富文本消息以及多样的消息类型，消息流水线也分有多个子部门。

- `MessageEncoderPipeline`：包含一系列消息编码器（`MessageEncoder`），将 mirai-core-api
  的结构编码为协议数据；
- `MessageDecoderPipeline`：包含一系列消息解码器（`MessageDecoder`），将协议数据解码为
  mirai-core-api 的结构；
- `OutgoingMessagePipeline`：包含一系列消息预处理器（`OutgoingMessagePreprocessor`
  ）、转换器（`OutgoingMessageTransformer`）、发送器（`OutgoingMessageSender`
  ）和后处理器（`OutgoingMessagePostprocessor`
  ）。预处理器可校验消息长度等、转换器可根据情况把普通消息转为长消息或分片消息、发送器则处理非常规类型的消息（如音乐分享）。

通常来说，一个协议上就是"消息"的消息元素，如 mirai `PlainText` 和 `At` 对应协议 `Elem.Text`
、mirai `Face` 对应协议 `Elem.Face`。要支持这样的消息元素，只需要增加一个 `MessageEncoder`
用于编码和一个 `MessageDecoder` 用于解码即可。

[//]: # (> 实际上 `Dice` 的处理还涉及*消息细化*（Message Refinement）机制，该机制将在之后解释。 )

而对于协议上有独立通道，而 mirai 为了易用性将他们设计为消息的消息元素，需要实现 `MessageSender`
并以高于一般消息发送器 `GeneralMessageSender` 的优先级注册。 如（`MusicShare`
）实际上通过独立的音乐分享通道发送；又如文件消息（`FileMessage`）需要通过 `FileManagement.Feed`
通道发送。

#### `MessageProtocol`

无论是协议上就是消息的消息元素，还是协议上有独立通道的消息元素，它们的实现的方式都是类似的 ——
通过 `MessageProtocol.collectProcessors`。
`MessageProtocol` 是对*协议实现*的抽象。一个 `MessageProtocol` 包含了它负责的消息类型的所有处理器。

继续使用 `Video` 示例，我们假设 `Video` 功能在协议上就是"消息"，因此只需要增加 `MessageEncoder`
与 `MessageDecoder`：

```kotlin
package net.mamoe.mirai.internal.message.protocol.impl

internal class VideoProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder()) // 添加 Encoder 到消息流水线
        add(Decoder())
    }

    private class Encoder : MessageEncoder<Video> {
        override suspend fun MessageEncoderContext.process(data: Video) {
            markAsConsumed() // 表示本 `MessageEncoder` 将会完成对输入的 `Video` 的全部处理

            if (data is AbstractVideo) {
                // `collect` 收集一个协议结构
                collect(ImMsgBody.Elem(videoFile = data.toVideoFile()))
            }
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.videoFile == null) return
            markAsConsumed() // 表示本 `MessageDecoder` 将完成对输入的 `ImMsgBody.Elem` 的全部处理

            collect(
                OnlineVideoImpl(
                    data.videoFile.fileName,
                    data.videoFile.fileTime.toLong(),
                    data.videoFile
                )
            )
        }
    }
}
```

假如 `Video` 功能在协议上不是"消息"，就需要实现 `MessageSender`。`MessageSender`
将直接涉及发送合适的数据包，可参考 `net.mamoe.mirai.internal.message.protocol.impl.MusicShareProtocol.Sender`
实现。

最后需要注册 `VideoProtocol`，这与之前注册 `Factory`
的方式一样，只是需要基于 `net.mamoe.mirai.internal.message.protocol.MessageProtocol`
：

- JVM：在 jvmBaseMain/resources/META-INF/services/
  新建一个文件名为 `net.mamoe.mirai.internal.message.protocol.MessageProtocol`
  ，文件内容为 `net.mamoe.mirai.internal.message.protocol.impl.VideoProtocol`
- Native：
  在 `net.mamoe.mirai.internal.utils.MiraiCoreServices.registerAll`
  末尾增加：

  ```kotlin
  Services.register(
      "net.mamoe.mirai.internal.message.protocol.MessageProtocol",
      "net.mamoe.mirai.internal.message.protocol.impl.VideoProtocol"
  ) { net.mamoe.mirai.internal.message.protocol.impl.VideoProtocol() }
  ```

### 7. 添加收发消息测试

mirai
拥有对消息流水线的测试框架，位于 `commonTest/kotlin/message/protocol/impl/AbstractMessageProtocolTest`
。

请为你的 `...Protocol` 在 `commonTest/kotlin/message/protocol/impl`
增加一个 `...ProtocolTest`。可以参考 `TextProtocolTest`
（协议上的消息）和 `MusicShareProtocolTest`（独立通道）。

在单元测试通过之后，也请登录测试账号在本地环境进行收发消息测试。
提示：要运行 `main` 函数，可在 `jvmTest/kotlin/local` 中编写。该目录已被忽略
Git 跟踪。

### 8. 实现序列化

mirai
使用 [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
。
mirai 提供 `MessageSerializers` 来支持消息元素的多态序列化。

序列化器（`KSerializer`）在 `MessageProtocol` 中收集。

对于 `Video` 示例，我们首先为 `OnlineVideoImpl` 和 `OfflineVideoImpl` 实现序列化。

`OfflineVideoImpl` 的序列化很简单，只需要添加两个注解：

```kotlin
@SerialName(OfflineVideo.SERIAL_NAME)
@Serializable // 编译器将会自动生成序列化器
internal class OfflineVideoImpl(
    override val filename: String,
    override val length: Long,
) : OfflineVideo, AbstractVideo() {
    // ...
}

internal class VideoProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        // ...

        // `superclassesScope` 的参数为 `OfflineVideo` 的父类。
        // 这么做是因为反射在 Native 平台不可用。
        MessageSerializer.superclassesScope(
            MessageContent::class,
            SingleMessage::class
        ) {
            add(
                MessageSerializer(
                    OfflineVideoImpl::class,
                    OfflineVideoImpl.serializer()
                )
            ) // 绑定序列化器
        }
    }
}
```

由于 `OnlineVideoImpl` 拥有 `ImMsgBody.VideoFile?` 属性，

```kotlin
@SerialName(OnlineVideo.SERIAL_NAME)
@Serializable // 编译器将会自动生成序列化器
internal class OnlineVideoImpl(
    override val filename: String,
    override val length: Long,
    private val original: @Serializable(VideoFileAsByteArraySerializer::class) ImMsgBody.VideoFile? // 协议数据结构
) : OnlineVideo, AbstractVideo() {
    override fun toVideoFile(): ImMsgBody.VideoFile = original
}

// 将 `VideoFile` 作为十六进制字符串序列化，这样输出的 JSON 字符串更易读。
internal object VideoFileAsHexSerializer :
    KSerializer<ImMsgBody.VideoFile> by String.serializer()
        .map( // 使用 String.serializer() 作为代理序列化器
            resultantDescriptor = ImMsgBody.VideoFile.serializer().descriptor,
            deserialize = { // 反序列化：即字符串到 ImMsgBody.VideoFile
                it.hexToBytes() // 转换为字符串
                    .loadAs(ImMsgBody.VideoFile.serializer()) // 解析 ByteArray 到结构
            },
            serialize = { // 序列化：即 ImMsgBody.VideoFile 到字符串
                it.toByteArray(ImMsgBody.VideoFile.serializer()) // 导出结构数据为 ByteArray
                    .toUHexString() // 转换为十六进制字符串表示
            }
        )

internal class VideoProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        // ...
        MessageSerializer.superclassesScope(
            MessageContent::class,
            SingleMessage::class
        ) {
            // ...
            add(
                MessageSerializer(
                    OnlineVideoImpl::class,
                    OnlineVideoImpl.serializer()
                )
            ) // 绑定序列化器
        }
    }
}
```

### 9. 添加序列化测试

请在第 7 步实现的你的 `MessageProtocolTest` 中添加
添加序列化测试。

你可以仿照已有的测试如 `TextProtocolTest`
等编写你的序列化测试。通常只需要添加一个类似于 `TextProtocolTest`
的 `test serialization for AtAll` 的函数即可。

### 10. 提交 PR

恭喜，到这一步你已经实现了高质量的新消息类型的支持。请在 `https://github.com/mamoe/mirai/compare` 提交
PR，让你的修改能进入 mirai 主分支。感谢你对 mirai 的贡献。
