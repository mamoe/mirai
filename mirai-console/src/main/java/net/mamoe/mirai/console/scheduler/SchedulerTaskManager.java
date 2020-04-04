package net.mamoe.mirai.console.scheduler;

/**
 * Java开发者的SchedulerTask
 * 使用kt实现, java的API
 */

public class SchedulerTaskManager {
    public static SchedulerTaskManagerInstance getInstance(){
        return SchedulerTaskManagerInstance.INSTANCE;
    }
}
