package net.mamoe.mirai.task;


public class MiraiTaskManager {

    private static MiraiTaskManager instance;

    public static MiraiTaskManager getInstance(){
        if(MiraiTaskManager.instance == null){
            MiraiTaskManager.instance = new MiraiTaskManager();
        }
        return MiraiTaskManager.instance;
    }

    private MiraiTaskPool pool;

    private MiraiTaskManager(){

    }
}
