package net.mamoe.mirai.console.command;

import kotlin.NotImplementedError;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.future.FutureKt;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Java 适配的 {@link CommandManagerKt}
 */
@SuppressWarnings("unused")
public final class JCommandManager {
    private JCommandManager() {
        throw new NotImplementedError();
    }

    /**
     * 获取指令前缀
     *
     * @return 指令前缀
     */
    @NotNull
    public static String getCommandPrefix() {
        return CommandManagerKt.getCommandPrefix();
    }

    /**
     * 获取一个指令所有者已经注册了的指令列表.
     *
     * @param owner 指令所有者
     * @return 指令列表
     */
    @NotNull
    public static List<@NotNull Command> getRegisteredCommands(final @NotNull CommandOwner owner) {
        return CommandManagerKt.getRegisteredCommands(Objects.requireNonNull(owner, "owner"));
    }

    /**
     * 注册一个指令.
     *
     * @param command  指令实例
     * @param override 是否覆盖重名指令.
     *                 <p>
     *                 若原有指令 P, 其 {@link Command#getNames()} 为 'a', 'b', 'c'. <br>
     *                 新指令 Q, 其 {@link Command#getNames()}  为 'b', 将会覆盖原指令 A 注册的 'b'.
     *                 <p>
     *                 即注册完成后, 'a' 和 'c' 将会解析到指令 P, 而 'b' 会解析到指令 Q.
     * @return 若已有重名指令, 且 <code>override</code> 为 <code>false</code>, 返回 <code>false</code>; <br>
     * 若已有重名指令, 但 <code>override</code> 为 <code>true</code>, 覆盖原有指令并返回 <code>true</code>.
     */
    public static boolean register(final @NotNull Command command, final boolean override) {
        Objects.requireNonNull(command, "command");
        return CommandManagerKt.register(command, override);
    }

    /**
     * 注册一个指令, 已有重复名称的指令时返回 <code>false</code>
     *
     * @param command 指令实例
     * @return 若已有重名指令, 返回 <code>false</code>, 否则返回 <code>true</code>.
     */
    public static boolean register(final @NotNull Command command) {
        Objects.requireNonNull(command, "command");
        return register(command, false);
    }

    /**
     * 查找并返回重名的指令. 返回重名指令.
     */
    @Nullable
    public static Command findDuplicate(final @NotNull Command command) {
        Objects.requireNonNull(command, "command");
        return CommandManagerKt.findDuplicate(command);
    }

    /**
     * 取消注册这个指令. 若指令未注册, 返回 <code>false</code>.
     */
    public static boolean unregister(final @NotNull Command command) {
        Objects.requireNonNull(command, "command");
        return CommandManagerKt.unregister(command);
    }

    /**
     * 取消注册所有属于 <code>owner</code> 的指令
     *
     * @param owner 指令所有者
     */
    public static void unregisterAllCommands(final @NotNull CommandOwner owner) {
        Objects.requireNonNull(owner, "owner");
        CommandManagerKt.unregisterAllCommands(owner);
    }

    /**
     * 解析并执行一个指令
     *
     * @param args 接受 {@link String} 或 {@link Message} , 其他对象将会被 {@link Object#toString()}
     * @see CommandExecuteResult
     * @see #executeCommandAsync(CoroutineScope, CommandSender, Object...)
     */
    public static CommandExecuteResult executeCommand(final @NotNull CommandSender sender, final @NotNull Object... args) throws InterruptedException {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(args, "args");
        for (Object arg : args) {
            Objects.requireNonNull(arg, "element of args");
        }

        return BuildersKt.runBlocking(EmptyCoroutineContext.INSTANCE, (scope, completion) -> CommandManagerKt.executeCommand(sender, args, completion));
    }

    /**
     * 异步 (在 Kotlin 协程线程池) 解析并执行一个指令
     *
     * @param scope 协程作用域 (用于管理协程生命周期). 一般填入 {@link JavaPlugin} 实例.
     * @param args  接受 {@link String} 或 {@link Message} , 其他对象将会被 {@link Object#toString()}
     * @see CommandExecuteResult
     * @see #executeCommand(CommandSender, Object...)
     */
    public static CompletableFuture<CommandExecuteResult> executeCommandAsync(final @NotNull CoroutineScope scope, final @NotNull CommandSender sender, final @NotNull Object... args) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(scope, "scope");
        for (Object arg : args) {
            Objects.requireNonNull(arg, "element of args");
        }

        return FutureKt.future(scope, EmptyCoroutineContext.INSTANCE, CoroutineStart.DEFAULT, (sc, completion) -> CommandManagerKt.executeCommand(sender, args, completion));
    }
}
