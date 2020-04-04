package net.mamoe.mirai.console.scheduler;

/**
 * Java开发者的SchedulerTask
 * 使用kt实现, java的API
 */

/**
 *         PluginScheduler.RepeatTaskReceipt repeatTaskReceipt = this.getScheduler().repeat(() -> {
 *             getLogger().info("I repeat");
 *         },100);
 *
 *
 *         this.getScheduler().delay(() -> {
 *             repeatTaskReceipt.setCancelled(true);
 *         },10000);
 *
 *
 *         Future<String> future = this.getScheduler().async(() -> {
 *             //do some task
 *             return "success";
 *         });
 *
 *         try {
 *             getLogger().info(future.get());
 *         } catch (InterruptedException | ExecutionException e) {
 *             e.printStackTrace();
 *         }
 */

public class SchedulerTaskManager {
    public static SchedulerTaskManagerInstance getInstance(){
        return SchedulerTaskManagerInstance.INSTANCE;
    }
}

