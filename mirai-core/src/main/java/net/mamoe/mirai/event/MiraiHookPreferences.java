package net.mamoe.mirai.event;

import lombok.Data;

@Data
public class MiraiHookPreferences {
    private int priority = 0;
    private boolean ignoreCanceled = true;
}
