package net.mamoe.mirai.utils.config;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mirai Config
 * Only support {INI} format
 * Support MAP and LIST
 * Thread safe
 */
public class MiraiConfig {

    private File file;

    private Ini ini;

    private volatile Map<String, MiraiConfigSection> cacheSection = new ConcurrentHashMap<>();
    private volatile List<String> needSaving = new Vector<>();

    public MiraiConfig(File file){
        if(!file.getName().contains(".")){
            file = new File(file.getParent() + file.getName() + ".ini");
        }
        this.file = file;
        try {
            Config config = new Config();
            config.setMultiSection(true);
            ini = new Ini();
            ini.setConfig(config);
            ini.load(this.file.toURI().toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSection(String key, MiraiConfigSection section){
        cacheSection.put(key, section);
        needSaving.add(key);
    }


    public MiraiMapSection getMapSection(String key){
        if(!cacheSection.containsKey(key)) {
            MiraiMapSection section = new MiraiMapSection();
            section.putAll(ini.get(key));
            cacheSection.put(key, section);
        }
        return (MiraiMapSection) cacheSection.get(key);
    }

    public MiraiListSection getListSection(String key){
        if(!cacheSection.containsKey(key)) {
            MiraiListSection section = new MiraiListSection();
            section.addAll(ini.get(key).values());
            cacheSection.put(key, section);
        }
        return (MiraiListSection) cacheSection.get(key);
    }


    public synchronized void save(){
        needSaving.forEach(a -> {
            cacheSection.get(a).saveAsSection(ini.get(a));
        });
        try {
            ini.store(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        needSaving.clear();
    }

    public void clearCache(){
        needSaving.clear();
        cacheSection.clear();
    }
}

