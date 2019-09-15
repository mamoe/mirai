package net.mamoe.mirai.utils.setting;

import net.mamoe.mirai.plugin.MiraiPluginBase;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Mirai Config <br>
 * Only supports <code>INI</code> format <br>
 * Supports {@link Map} and {@link List}
 *
 * @author NaturalHG
 */
public class MiraiSettings {

    private File file;

    private Ini ini;

    private volatile Map<String, MiraiSettingSection> cacheSection = new ConcurrentHashMap<>();

    public MiraiSettings(MiraiPluginBase pluginBase, String filename) {
        // TODO: 2019/9/6 每个插件独立文件夹存放
        this(new File(filename));
    }

    public MiraiSettings(@NotNull File file) {
        Objects.requireNonNull(file);
        if (!file.getName().contains(".")) {
            file = new File(file.getPath() + ".ini");
        }
        this.file = file;
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new RuntimeException("cannot create config file " + file);
            }
            Config config = new Config();
            config.setMultiSection(true);
            ini = new Ini();
            ini.setConfig(config);
            ini.load(this.file.toURI().toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setSection(String key, MiraiSettingSection section) {
        cacheSection.put(key, section);
    }


    public synchronized MiraiSettingMapSection getMapSection(String key) {
        if (!cacheSection.containsKey(key)) {
            MiraiSettingMapSection section = new MiraiSettingMapSection();
            if (ini.containsKey(key)) {
                section.putAll(ini.get(key));
            }
            cacheSection.put(key, section);
        }
        return (MiraiSettingMapSection) cacheSection.get(key);
    }

    public synchronized MiraiSettingListSection getListSection(String key) {
        if (!cacheSection.containsKey(key)) {
            MiraiSettingListSection section = new MiraiSettingListSection();
            if (ini.containsKey(key)) {
                section.addAll(ini.get(key).values());
            }
            cacheSection.put(key, section);
        }
        return (MiraiSettingListSection) cacheSection.get(key);
    }


    public synchronized void save() {
        cacheSection.forEach((k, a) -> {
            if (!ini.containsKey(k)) {
                ini.put(k, "", new HashMap<>());
            }
            a.saveAsSection(ini.get(k));
        });
        this.clearCache();
        try {
            ini.store(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void clearCache() {
        cacheSection.clear();
    }
}

