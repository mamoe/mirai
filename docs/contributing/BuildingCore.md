# 构建 Core

本文介绍如何构建 core 的 JVM 和 Native 目标。

## 构建 core 的 JVM 目标

方法与[构建 JVM 目标项目](README.md#构建-jvm-目标项目)
类似，但需要使用 `:mirai-core:compileKotlinJvm` 和 `:mirai-core:jvmTest`
分别用于编译和测试。提示：直接执行测试时也会自动先完成编译。

## 构建 core 的 Native 目标

[OpenSSL.def]: ../../mirai-core/src/nativeMain/cinterop/OpenSSL.def

Kotlin 会自动配置 Native 编译器，要构建 Mirai 的 Native 目标还需要准备相关依赖。

### 操作系统条件

主机操作系统为以下任一：

- Windows x86_64 (amd64)
- macOS x86_64 (amd64)
- macOS aarch64 (arm64)
- Linux x86_64 (amd64)

注意：32 位操作系统不受支持。未列举的操作系统不受支持。

目前 Kotlin 对交叉编译支持有限，只能在一个主机上编译该主机平台的目标。例如在 Windows x86_64 主机上只能编译
Windows x86_64 目标；在 macOS aarch64 主机上只能编译 macOS aarch64 目标。

与其他 Native 语言相同，Kotlin 的应用使用依赖时同样需要配置链接，mirai
已经配置了常用目录。也可以在 `mirai-core/src/nativeMain/cinterop/OpenSSL.def`
修改 `linkerOpts` 即链接器参数，以增加自定义路径。

### 性能提示

在编译和链接时可能需要大量内存，请使用至少拥有 8GB 内存的主机。使用 32GB 内存的主机可以获得不错的体验。mirai
默认启用多项目同时编译，编译时可能会使用大量主机资源。

如果主机可用内存较低，请不要执行 `./gradlew assemble`
编译全部项目，这可能会导致内存溢出，也将会导致编译缓慢。可以单独为某个模块执行批量编译，如 `./gradlew :mirai-console:assemble`
。

#### 编译耗时

若使用 Apple M1 Max 或同等级 CPU (AMD R7 5800X / Intel i7-12700K / Intel
i9-12950HX)，单独执行 `./gradlew assemble` 编译并连接全部项目 (含动态链接库和静态链接库) 需时约 9
分钟。单独执行 `./gradlew check` 需约 4 分钟。
但在 GitHub 的 2 核心 CPU Actions 机器上执行 `assemble` 通常需要约 40 分钟。

### 安装 OpenSSL

所有上述主机都需要进行这一步。

可以访问 OpenSSL 官网 `https://curl.se/download.html` 安装。

#### 在 Ubuntu 通过 Aptitude 安装 OpenSSL

```shell
$ sudo apt install libssl-dev # 安装 OpenSSL
$ sudo apt install gcc-multilib # 若遇到链接问题可额外尝试此命令
```

#### 在 macOS 通过 Homebrew 安装 OpenSSL

```shell
$ brew install openssl@3
```

注意：若遇到无法链接等问题，可以尝试通过源码编译安装。

#### 在 macOS 或 Linux 通过源码编译安装 OpenSSL

请参考 [OpenSSL 文档](https://github.com/openssl/openssl/blob/master/INSTALL.md#prerequisites)
准备 OpenSSL 的要求。

以下命令可能会帮助你（这是 mirai 的 GitHub Actions 使用的命令）。

```shell
$ git clone https://github.com/openssl/openssl.git --recursive
$ cd openssl
$ git checkout tags/openssl-3.0.3
$ ./Configure --prefix=/opt/openssl --openssldir=/usr/local/ssl
$ make
$ sudo make install
```

若在 Ubuntu 遇到链接问题，可额外尝试此命令：

```shell
$ sudo apt install gcc-multilib
```

#### 在 Windows 通过 vcpkg 安装 OpenSSL

你需要提前安装 [vcpkg](https://github.com/microsoft/vcpkg/blob/master/README_zh_CN.md)
。

以下命令可能会帮助你（这是 mirai 的 GitHub Actions 使用的命令）。

```powershell
echo "set(VCPKG_BUILD_TYPE release)" | Out-File -FilePath "$env:VCPKG_INSTALLATION_ROOT\triplets\x64-windows.cmake" -Encoding utf8 -Append
vcpkg install openssl:x64-windows curl[core, ssl]: x64-windows
New-Item -Path $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\crypto.lib -ItemType SymbolicLink -Value $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\libcrypto.lib
New-Item -Path $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\ssl.lib -ItemType SymbolicLink -Value $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\libssl.lib
New-Item -Path $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\curl.lib -ItemType SymbolicLink -Value $env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\lib\libcurl.lib
echo "$env:VCPKG_INSTALLATION_ROOT\installed\x64-windows\bin" | Out-File -FilePath $env:GITHUB_PATH -Encoding utf8 -Append
```

由于链接器只识别不包含 `lib` 前缀的文件（如 `ssl.lib` 而非 `libssl.lib`，vcpkg 编译产物为后者），上述 `New-Item` 创建一个无 `lib` 前缀的链接指向库文件。

注意：

- 你将需要修改链接器配置（位于 `mirai-core/src/nativeMain/cinterop/OpenSSL.def`
  ），增加 `linkerOpts` 和 `compilerOpts` 指向你本地安装的路径。
- 不要将修改路径后的 `OpenSSL.def` 通过 Git 推送到 mirai 仓库或 PR。

#### 在 Windows 通过源码编译安装 OpenSSL

在 Windows，可通过源码编译安装，请使用 Command Prompt (cmd)。

你需要提前安装 [Git](https://git-scm.com/)
和 [Microsoft Visual Studio](https://visualstudio.microsoft.com/zh-hans/)
，并修改以下命令中的路径。

请参考 [OpenSSL 文档](https://github.com/openssl/openssl/blob/master/INSTALL.md#prerequisites)
准备 OpenSSL 的要求。

以下命令可能会帮助你（这是 mirai 的 GitHub Actions 使用过的命令）。

```shell
git clone https://github.com/openssl/openssl.git --recursive
cd openssl
git checkout tags/openssl-3.0.3
perl Configure VC-WIN64A --prefix=C:/openssl --openssldir=C:/openssl/ssl no-asm
"C:\Program Files\Microsoft Visual Studio\2022\Enterprise\VC\Auxiliary\Build\vcvarsall.bat" x86_amd64 && nmake && nmake install
```

注意：

- `--prefix=C:/openssl --openssldir=C:/openssl/ssl` 表示将 OpenSSL
  安装在 `C:/openssl`，mirai
  已经配置会使用此路径寻找链接库。你也可以更换为其他路径，但就需要同步修改配置（位于 `mirai-core/src/nativeMain/cinterop/OpenSSL.def`
  ）；
- 不要将修改路径后的 `OpenSSL.def` 通过 Git 推送到 mirai 仓库或 PR。

### 安装 cURL

mirai 在 Windows 上使用
cURL，在其他平台使用 [Ktor CIO](https://ktor.io/docs/http-client-engines.html#cio)
，因此只有 Windows 系统需要进行这一步。

可以访问 cURL 官网 <https://curl.se/download.html> 安装。

提示：如果在[链接](#链接并测试)时遇到找不到 cURL
相关符号的问题，请尝试修改链接器参数。尽管 `mirai-core/src/nativeMain/cinterop/OpenSSL.def`
是用于 `OpenSSL.def` 的，也可以在这个文件配置 cURL 路径。

### 编译

在任意主机上可以执行所有目标的 Kotlin 编译，但不能执行链接。要执行特定目标的编译，运行 Gradle
任务 `compileKotlinXXX`，其中 `XXX` 可以是：`MacosX64`、`MacosArm64`、`MingwX64`
或 `LinuxX64`。

也可以执行 `compileKotlinHost`，将自动根据当前主机选择合适的目标。

### 链接并测试

执行 core 模块的 `hostTest`，将根据主机选择合适的测试并运行。

详情参考 [Kotlin 官方文档](https://kotlinlang.org/docs/multiplatform-run-tests.html)
。

### 链接并构建动态链接库

注意，只有 mirai-core 可以构建可用的动态链接库。

执行 `:mirai-core:linkDebugSharedHost`
或 `:mirai-core:linkReleaseSharedHost`。Debug 版本会保留调试符号，能显示完整错误堆栈；而
Release 拥有更小体积（比 Debug 减小 50%）。

这也会同时生成一个头文件（`.h`
）供交互使用。详情查看 [Kotlin 官方文档](https://kotlinlang.org/docs/native-c-interop.html)
。

可以在 `mirai-core/build/bin/macosArm64/debugShared/` 类似路径找到生成的动态链接库和头文件。

### 链接并构建静态链接库

注意，只有 mirai-core 可以构建可用的静态链接库。

执行 `:mirai-core:linkDebugStaticHost`
或 `:mirai-core:linkReleaseStaticHost`。Debug 版本会保留调试符号，能显示完整错误堆栈；而
Release 拥有更小体积（比 Debug 减小 50%）。

可以在 `mirai-core/build/bin/macosArm64/debugStatic/` 类似路径找到生成的静态链接库和头文件。
