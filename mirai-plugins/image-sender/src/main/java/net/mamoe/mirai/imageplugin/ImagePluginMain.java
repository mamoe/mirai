package net.mamoe.mirai.imageplugin;

import net.mamoe.mirai.plugin.PluginBase;

public class ImagePluginMain extends PluginBase {
    @Override
    public void onLoad() {
        System.out.println("Loaded");
    }

    @Override
    public void onEnable() {
        System.out.println("Enabled");
    }
}
