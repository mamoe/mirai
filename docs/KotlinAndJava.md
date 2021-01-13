# Mirai - Kotlin And Java


本章介绍部分 Kotlin 定义对应的 Java 定义，以帮助 Java 使用者理解 Mirai 的源代码。

每部分第一个代码块为 Kotlin 代码，第二个代码块为 Java 代码。

预计阅读时间：5 分钟

#### 通用
- Kotlin 的定义都默认是 `public` 和 `final`
- Kotlin 不需要句末分号，通常以换行作为一个语句的结束

#### `class`
```kotlin
class A
```
```java
public final class A {
}
```

#### 构造器定义

以下几种 Kotlin 定义是等价的。

```kotlin
class A {
    private val value: String
    
    constructor(value: String) {
        this.value = value
    }
    
    constructor(integer: Int) {
        this.value = integer.toString()
    }
}
```

```kotlin
class A(val value: String) { // 类定义后面的括号表示主构造器
    constructor(integer: Int) : this(integer.toString())
}
```

对应的 Java 定义为：
```java
public final class A {
    private final String value;
    
    public A(String value) {
        this.value = value;
    }
    
    public A(int integer) {
        this.value = String.valueOf(integer);
    }
}
```

通常 Kotlin class 都会有一个主构造器。

#### 构造器调用

Kotlin 不需要 `new` 关键字。

```kotlin
val a = A("test")
```

```java
A a = new A("test");
```

#### 函数
```kotlin
class A {
    fun test(string: String): Int = 1
}
```

```java
public final class A {
    public int test(String string) {
        return 1;
    }
}
```

#### 属性 `val`
- Kotlin 的 `val` 是不可变的，只能被赋值一次
- 编译器为 `val` 创建 `getter`

```kotlin
class A {
    val value: String = "test"
}
```

```java
public final class A {
    private final String value = "test";
    public final String getValue() {
        return value;
    }
}
```

#### 属性 `var`
- Kotlin 的 `var` 相较于 `val` 是可变的，可以被多次赋值。
- 编译器为 `var` 创建 `getter` 和 `setter`

```kotlin
class A {
    var value: String = "test"
}
```

```java
public final class A {
    private String value = "test";
    public final String getValue() {
        return value;
    }
    public final String setValue(String value) {
        this.value = value;
    }
}
```

#### 顶层定义和 `const`
- Kotlin 的定义不一定需要在 `class` 中，允许直接存在于文件中的「顶层函数」和「顶层属性」
- `XXX.kt` 中的顶层定义会被编译为名为 `XXXKt` 的 `class`
- 顶层定义会被编译为 `static`
- `const` 可以修饰一个属性，编译器会把它编译为 Java 静态字段。

```kotlin
// Test.kt
val x: String = "xx"

const val CONST_VALUE: String = "cc"

fun foo() { }
```

```java
// TestKt.java
public final class TestKt {
    public static final String CONST_VALUE = "cc"; // const val 没有 getter
    
    private static final String x = "xx";
    public static String getX(){
        return x; 
    }
    
    public static void foo() { }
}
```

#### 单例对象
- Kotlin `object` 定义一个单例对象

```kotlin
object Test
```

```java
public final class Test {
    public static final Test INSTANCE = new Test();

    private Test() {}
}
```

#### 静态

```kotlin
object Test {
    val x = "x"             // public String getX()
    @JvmField val y = "y"   // public static final String y;
    @JvmStatic val z = "z"  // public static String getZ()
} 
```

```java
public final class Test {
    public static final Test INSTANCE = new Test();
    private Test() {}

    private final String x = "x";       // val
    public String getX() {
        return x;
    }
    
    public static final String y = "y"; // @JvmField val
    
    private final String z = "z";       // @JvmStatic val
    public static String getZ() {
        return z;
    }
} 
```


#### 静态

- Kotlin 没有 `static` 关键字，但可以通过 `@JvmStatic` 将一个函数编译为 `static`

```kotlin
object Test {
    fun a() { }
    @JvmStatic
    fun b() { }
} 
```

```java
public final class Test {
    public static final Test INSTANCE = new Test();
    private Test() {}
    
    public void a() { }
    public static void b() { }
} 
```

#### 伴生对象

- `class` 可以拥有 `companion object`
- 伴生对象内的 `@JvmField` 定义将会被编译到外部 `class`
- 伴生对象内的 `@JvmStatic` 函数以成员方法编译到伴生对象，然后以静态方法编译到外部 `class`

```kotlin
class Test {
    companion object {
        @JvmField
        val CONST: String = ""
    
        fun a() { }
        @JvmStatic
        fun b() { }
    }
} 
```

```java
public final class Test {
    public static final Companion Companion = new Companion();
    public static final String CONST = "";
    
    public static void b() { 
        Companion.b();
    }

    public static final class Companion {
        public void a() { }
        public void b() { }
    }
} 
```

#### 协程

Kotlin 协程是语言级特性，`suspend` 修饰的函数会在编译期被处理。

```kotlin
class A {
    suspend fun getValue(): String { /* ... */ } 
}
```

```java
public final class A {
    public Object getValue(Continuation<? super String> $completion) {
        // 由 Kotlin 编译器生成非常复杂的方法体
    }
}
```

`$completion` 参数类似于一个回调。需要熟悉 Kotlin 协程原理才能实现。为帮助 Java 用户，mirai 使用编译器插件处理 `suspend` 函数。

```kotlin
class A {
    @JvmBlockingBridge
    suspend fun getValue(): String { /* ... */ } 
}
```

```java
public final class A {
    public Object getValue(Continuation<? super String> $completion) {
        // 由 Kotlin 编译器生成非常复杂的方法体
    }
    
    // 通过 @JvmBlockingBridge 生成的方法
    public String getValue() {
        // 由 @JvmBlockingBridge 的编译器生成方法体，调用 getValue(Continuation)
    }
}
```

Java 使用者可以认为 `@JvmBlockingBridge suspend fun getValue(): String` 相当于 `fun getValue(): String`。


----

> [回到 Mirai 文档索引](README.md#jvm-平台-mirai-开发)