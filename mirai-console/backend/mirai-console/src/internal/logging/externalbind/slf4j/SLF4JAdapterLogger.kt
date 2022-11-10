/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.logging.externalbind.slf4j

import net.mamoe.mirai.utils.MiraiLogger
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.SubstituteLoggingEvent
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.nio.CharBuffer
import java.text.MessageFormat
import java.util.regex.Pattern
import org.slf4j.event.Level as SLF4JEventLevel

@Suppress("RegExpRedundantEscape")
internal class SLF4JAdapterLogger(
    private val logger: MiraiLogger
) : Logger {
    // Copied from Log4J
    internal companion object {
        private const val FORMAT_SPECIFIER = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"

        private val MSG_PATTERN = Pattern.compile(FORMAT_SPECIFIER)
        private const val DELIM_START = '{'
        private const val DELIM_STOP = '}'
        private const val ESCAPE_CHAR = '\\'

        @JvmStatic
        internal fun String.simpleFormat(args: Array<out Any?>): String {
            val buffer = StringBuilder()
            val reader = CharBuffer.wrap(this)
            var isEscape = false
            var index = 0
            while (reader.hasRemaining()) {
                when (val next = reader.get()) {
                    ESCAPE_CHAR -> {
                        if (isEscape) {
                            buffer.append(ESCAPE_CHAR)
                        }
                        isEscape = !isEscape
                    }
                    DELIM_START -> {
                        if (isEscape) {
                            buffer.append(next)
                        } else {
                            if (reader.hasRemaining()) {
                                if (reader.get(reader.position()) == DELIM_STOP) {
                                    reader.get()
                                    buffer.append(args.getOrNull(index))
                                    index++
                                } else {
                                    buffer.append(DELIM_START)
                                }
                            } else {
                                buffer.append(DELIM_START)
                            }
                        }
                        isEscape = false
                    }
                    else -> buffer.append(next).also { isEscape = false }
                }
            }
            return buffer.toString()
        }

        internal fun String.format1(vararg arguments: Any?): String = format2(arguments)

        // (java.lang.String, java.lang.Object[]): java.lang.String
        @Suppress("LocalVariableName")
        private val formatWithLog4JMH: MethodHandle? = kotlin.runCatching {
            val c_ParameterizedMessage = Class.forName("org.apache.logging.log4j.message.ParameterizedMessage")

            val mhLookup = MethodHandles.lookup()
            val mh_newParameterizedMessage = mhLookup.findConstructor(
                c_ParameterizedMessage, MethodType.methodType(Void.TYPE, String::class.java, Array<Any>::class.java)
            ).asFixedArity()

            val mh_getFormattedMessage = mhLookup.findVirtual(
                c_ParameterizedMessage, "getFormattedMessage", MethodType.methodType(String::class.java)
            )

            MethodHandles.filterReturnValue(mh_newParameterizedMessage, mh_getFormattedMessage)

        }.getOrNull()

        @JvmStatic
        internal fun String.format2(args: Array<out Any?>): String {
            kotlin.runCatching {
                val formatter = MessageFormat(this)
                val formats = formatter.formats
                if (formats.isNotEmpty()) {
                    return formatter.format(args)
                }
            }
            kotlin.runCatching {
                if (MSG_PATTERN.matcher(this).find()) {
                    return String.format(this, *args)
                }
            }
            kotlin.runCatching {
                // Try format with Log4J
                formatWithLog4JMH?.let { formatWithLog4JMH ->
                    return formatWithLog4JMH.invoke(this@format2, args) as String
                }
            }
            return simpleFormat(args)
        }
    }

    //////////////////////////////////////////////////////////////////
    override fun isTraceEnabled(): Boolean = logger.isVerboseEnabled
    override fun isTraceEnabled(marker: Marker?): Boolean = logger.isVerboseEnabled
    override fun isDebugEnabled(): Boolean = logger.isDebugEnabled
    override fun isDebugEnabled(marker: Marker?): Boolean = logger.isDebugEnabled
    override fun isInfoEnabled(): Boolean = logger.isInfoEnabled
    override fun isInfoEnabled(marker: Marker?): Boolean = logger.isInfoEnabled
    override fun isWarnEnabled(): Boolean = logger.isWarningEnabled
    override fun isWarnEnabled(marker: Marker?): Boolean = logger.isWarningEnabled
    override fun isErrorEnabled(): Boolean = logger.isErrorEnabled
    override fun isErrorEnabled(marker: Marker?): Boolean = logger.isErrorEnabled
    //////////////////////////////////////////////////////////////////

    override fun getName(): String = logger.identity ?: "<unknown>"

    @Suppress("DuplicatedCode")
    internal fun process(event: SubstituteLoggingEvent) {
        val msg = event.message
        val argx = event.argumentArray
        val throwx = event.throwable

        val evtlv = event.level ?: return

        val isEnabled = when (evtlv) {
            SLF4JEventLevel.ERROR -> isErrorEnabled
            SLF4JEventLevel.WARN -> isWarnEnabled
            SLF4JEventLevel.INFO -> isInfoEnabled
            SLF4JEventLevel.DEBUG -> isDebugEnabled
            SLF4JEventLevel.TRACE -> isTraceEnabled
        }
        if (!isEnabled) return

        if (argx == null) {
            when (evtlv) {
                SLF4JEventLevel.ERROR -> error(msg, t = throwx)
                SLF4JEventLevel.WARN -> warn(msg, t = throwx)
                SLF4JEventLevel.INFO -> info(msg, t = throwx)
                SLF4JEventLevel.DEBUG -> debug(msg, t = throwx)
                SLF4JEventLevel.TRACE -> trace(msg, t = throwx)
            }
            return
        }

        if (throwx == null) {
            when (evtlv) {
                SLF4JEventLevel.ERROR -> error(msg, arguments = argx)
                SLF4JEventLevel.WARN -> warn(msg, arguments = argx)
                SLF4JEventLevel.INFO -> info(msg, arguments = argx)
                SLF4JEventLevel.DEBUG -> debug(msg, arguments = argx)
                SLF4JEventLevel.TRACE -> trace(msg, arguments = argx)
            }
            return
        }

        val msg2 = msg.format2(argx)
        when (evtlv) {
            SLF4JEventLevel.ERROR -> error(msg2, t = throwx)
            SLF4JEventLevel.WARN -> warn(msg2, t = throwx)
            SLF4JEventLevel.INFO -> info(msg2, t = throwx)
            SLF4JEventLevel.DEBUG -> debug(msg2, t = throwx)
            SLF4JEventLevel.TRACE -> trace(msg2, t = throwx)
        }
    }

    //////////////////////////////////////////////////////////////////

    private inline fun iT(a: () -> Unit) {
        if (isTraceEnabled) a()
    }

    private inline fun iD(a: () -> Unit) {
        if (isDebugEnabled) a()
    }

    private inline fun iI(a: () -> Unit) {
        if (isInfoEnabled) a()
    }

    private inline fun iW(a: () -> Unit) {
        if (isWarnEnabled) a()
    }

    private inline fun iE(a: () -> Unit) {
        if (isErrorEnabled) a()
    }

    //////////////////////////////////////////////////////////////////

    override fun trace(msg: String?) {
        logger.verbose(msg)
    }

    override fun trace(msg: String?, t: Throwable?) {
        logger.verbose(msg, t)
    }

    override fun trace(format: String, arg: Any?) = iT { trace(format.format1(arg)) }
    override fun trace(format: String, arg1: Any?, arg2: Any?) = iT { trace(format.format1(arg1, arg2)) }
    override fun trace(format: String, arguments: Array<out Any?>) = iT { trace(format.format2(arguments)) }

    override fun trace(marker: Marker?, msg: String?) = trace(msg)
    override fun trace(marker: Marker?, format: String, arg: Any?) = trace(format, arg)
    override fun trace(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = trace(format, arg1, arg2)
    override fun trace(marker: Marker?, format: String, argArray: Array<out Any?>) = trace(format, argArray)
    override fun trace(marker: Marker?, msg: String?, t: Throwable?) = trace(msg, t)

    //////////////////////////////////////////////////////////////////

    override fun debug(msg: String?) {
        logger.debug(msg)
    }

    override fun debug(msg: String?, t: Throwable?) {
        logger.debug(msg, t)
    }

    override fun debug(format: String, arg: Any?) = iD { debug(format.format1(arg)) }
    override fun debug(format: String, arg1: Any?, arg2: Any?) = iD { debug(format.format1(arg1, arg2)) }
    override fun debug(format: String, arguments: Array<out Any?>) = iD { debug(format.format2(arguments)) }

    override fun debug(marker: Marker?, msg: String?) = debug(msg)
    override fun debug(marker: Marker?, format: String, arg: Any?) = debug(format, arg)
    override fun debug(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = debug(format, arg1, arg2)
    override fun debug(marker: Marker?, format: String, arguments: Array<out Any?>) = debug(format, arguments)
    override fun debug(marker: Marker?, msg: String?, t: Throwable?) = debug(msg, t)

    //////////////////////////////////////////////////////////////////

    override fun info(msg: String?) {
        logger.info(msg)
    }

    override fun info(msg: String?, t: Throwable?) {
        logger.info(msg, t)
    }

    override fun info(format: String, arg: Any?) = iI { info(format.format1(arg)) }
    override fun info(format: String, arg1: Any?, arg2: Any?) = iI { info(format.format1(arg1, arg2)) }
    override fun info(format: String, arguments: Array<out Any?>) = iI { info(format.format2(arguments)) }

    override fun info(marker: Marker?, msg: String?) = info(msg)
    override fun info(marker: Marker?, format: String, arg: Any?) = info(format, arg)
    override fun info(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = info(format, arg1, arg2)
    override fun info(marker: Marker?, format: String, arguments: Array<out Any?>) = info(format, arguments)
    override fun info(marker: Marker?, msg: String?, t: Throwable?) = info(msg, t)

    //////////////////////////////////////////////////////////////////

    override fun warn(msg: String?) {
        logger.warning(msg)
    }

    override fun warn(msg: String?, t: Throwable?) {
        logger.warning(msg, t)
    }

    override fun warn(format: String, arg: Any?) = iW { warn(format.format1(arg)) }
    override fun warn(format: String, arguments: Array<out Any?>) = iW { warn(format.format2(arguments)) }
    override fun warn(format: String, arg1: Any?, arg2: Any?) = iW { warn(format.format1(arg1, arg2)) }

    override fun warn(marker: Marker?, msg: String?) = warn(msg)
    override fun warn(marker: Marker?, format: String, arg: Any?) = warn(format, arg)
    override fun warn(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = warn(format, arg1, arg2)
    override fun warn(marker: Marker?, format: String, arguments: Array<out Any?>) = warn(format, arguments)
    override fun warn(marker: Marker?, msg: String?, t: Throwable?) = warn(msg, t)

    //////////////////////////////////////////////////////////////////

    override fun error(msg: String?) {
        logger.error(msg)
    }

    override fun error(msg: String?, t: Throwable?) {
        logger.error(msg, t)
    }

    override fun error(format: String, arg: Any?) = iE { error(format.format1(arg)) }
    override fun error(format: String, arg1: Any?, arg2: Any?) = iE { error(format.format1(arg1, arg2)) }
    override fun error(format: String, arguments: Array<out Any?>) = iE { error(format.format2(arguments)) }

    override fun error(marker: Marker?, msg: String?) = error(msg)
    override fun error(marker: Marker?, format: String, arg: Any?) = error(format, arg)
    override fun error(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = error(format, arg1, arg2)
    override fun error(marker: Marker?, format: String, arguments: Array<out Any?>) = error(format, arguments)
    override fun error(marker: Marker?, msg: String?, t: Throwable?) = error(msg, t)
}
