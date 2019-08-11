package net.mamoe.mirai.utils.config;


import org.ini4j.Profile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MiraiMapSection extends ConcurrentHashMap<String, Object> implements MiraiConfigSection {

    public Object get(String key){
        return this.get(key,null);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        if (key == null || key.isEmpty()){
            return defaultValue;
        }
        if (super.containsKey(key)){
            return (T) super.get(key);
        }
        return defaultValue;
    }

    public void set(String key, Object value){
        this.put(key,value);
    }

    public void remove(String key){
        super.remove(key);
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(String.valueOf(this.get(key, defaultValue)));
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0D);
    }

    public double getDouble(String key, double defaultValue) {
        return Double.parseDouble(String.valueOf(this.get(key, defaultValue)));
    }

    public float getFloat(String key) {
        return this.getFloat(key, 0F);
    }

    public float getFloat(String key, float defaultValue) {
        return Float.parseFloat(String.valueOf(this.get(key, defaultValue)));
    }

    public String getString(String key) {
        return this.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return String.valueOf(this.get(key, defaultValue));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> asList(){
        return this.values().stream().map(a -> (T)(a)).collect(Collectors.toList());
    }

    @Override
    public synchronized void saveAsSection(Profile.Section section) {
        section.clear();
        this.forEach(section::put);
    }

    @Override
    public void close() throws IOException {

    }

}

