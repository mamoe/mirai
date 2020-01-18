package net.mamoe.mirai.imageplugin;

import net.mamoe.mirai.plugin.PluginBase;

public class ImagePluginMain extends PluginBase {
    static {
        System.out.println("Static Loaded");
    }

    @Override
    public void onLoad() {
        System.out.println("Loaded");
    }

    @Override
    public void onEnable() {
        System.out.println("Enabled");

    }
}
