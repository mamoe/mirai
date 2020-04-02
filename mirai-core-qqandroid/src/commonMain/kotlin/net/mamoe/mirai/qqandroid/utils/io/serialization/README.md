# io.serialization

**序列化支持**

包含:
- QQ 的 JceStruct 相关的全自动序列化和反序列化: [Jce.kt](jce/JceNew.kt)
- Protocol Buffers 的 optional 支持: [ProtoBufWithNullableSupport.kt](ProtoBufWithNullableSupport.kt)

其中, `ProtoBufWithNullableSupport` 的绝大部分源码来自 `kotlinx.serialization`. 原著权归该项目作者所有.  
Mirai 所做的修改已经标记上了 `MIRAI MODIFY START`