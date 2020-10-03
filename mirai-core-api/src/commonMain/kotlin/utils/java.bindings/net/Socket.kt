@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "RedundantModalityModifier",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE",
    "NON_FINAL_MEMBER_IN_OBJECT",
    "ConvertSecondaryConstructorToPrimary"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.net

public expect open class Socket : java.io.Closeable {
    public constructor()
    public constructor(proxy: Proxy)
    public constructor(host: String, port: Int)
    public constructor(address: InetAddress, port: Int)
    public constructor(host: String, port: Int, stream: Boolean)
    public constructor(host: InetAddress, port: Int, stream: Boolean)

    public open fun connect(endpoint: SocketAddress)
    public open fun connect(endpoint: SocketAddress, timeout: Int)
    public open fun bind(bindpoint: SocketAddress)
    public open fun getInetAddress(): InetAddress
    public open fun getLocalAddress(): InetAddress
    public open fun getPort(): Int
    public open fun getLocalPort(): Int
    public open fun getRemoteSocketAddress(): SocketAddress
    public open fun getLocalSocketAddress(): SocketAddress

    // public open fun getChannel(): java.nio.channels.SocketChannel
    public open fun getInputStream(): java.io.InputStream
    public open fun getOutputStream(): java.io.OutputStream
    public open fun setTcpNoDelay(on: Boolean)
    public open fun getTcpNoDelay(): Boolean
    public open fun setSoLinger(on: Boolean, linger: Int)
    public open fun getSoLinger(): Int
    public open fun sendUrgentData(data: Int)
    public open fun setOOBInline(on: Boolean)
    public open fun getOOBInline(): Boolean
    public open fun setSoTimeout(timeout: Int)
    public open fun getSoTimeout(): Int
    public open fun setSendBufferSize(size: Int)
    public open fun getSendBufferSize(): Int
    public open fun setReceiveBufferSize(size: Int)
    public open fun getReceiveBufferSize(): Int
    public open fun setKeepAlive(on: Boolean)
    public open fun getKeepAlive(): Boolean
    public open fun setTrafficClass(tc: Int)
    public open fun getTrafficClass(): Int
    public open fun setReuseAddress(on: Boolean)
    public open fun getReuseAddress(): Boolean
    public open fun close()
    public open fun shutdownInput()
    public open fun shutdownOutput()
    public open fun toString(): String
    public open fun isConnected(): Boolean
    public open fun isBound(): Boolean
    public open fun isClosed(): Boolean
    public open fun isInputShutdown(): Boolean
    public open fun isOutputShutdown(): Boolean
    public open fun <T> setOption(name: SocketOption<T>, value: T): Socket
    public open fun <T> getOption(name: SocketOption<T>): T
    public open fun supportedOptions(): Set<SocketOption<*>>

    public companion object {
        //  @JvmStatic public open fun setSocketImplFactory(fac: SocketImplFactory)
    }
}  