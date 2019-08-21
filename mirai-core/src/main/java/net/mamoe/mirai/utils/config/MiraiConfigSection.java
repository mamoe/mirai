package net.mamoe.mirai.utils.config;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MiraiConfigSection<T> extends ConcurrentSkipListMap<String, T> {

    public MiraiConfigSection(){
        /*
        * Ensure the key will be in order
        * */
        super((a,b) -> 1);
    }

    @SuppressWarnings("unchecked")
    public <D extends T> D getAs(String key){
        return (D)this.get(key);
    }

    @SuppressWarnings("unchecked")
    public <D extends T> D getAs(String key, D defaultV){
        return (D)(this.getOrDefault(key,defaultV));
    }

    public Integer getInt(String key){
        return Integer.valueOf(this.get(key).toString());
    }

    public Double getDouble(String key){
        return Double.valueOf(this.get(key).toString());
    }

    public Float getFloat(String key){
        return Float.valueOf(this.get(key).toString());
    }

    public String getString(String key){
        return String.valueOf(this.get(key));
    }

    @SuppressWarnings("unchecked")
    public <D extends T> MiraiConfigSection<D> getTypedSection(String key){
        return (MiraiConfigSection<D>) this.getAs(key);
    }

    @SuppressWarnings("unchecked")
    public MiraiConfigSection<Object> getSection(String key){
        return (MiraiConfigSection<Object>) this.getAs(key);
    }


}
