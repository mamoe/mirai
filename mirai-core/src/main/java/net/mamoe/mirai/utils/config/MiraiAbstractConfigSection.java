package net.mamoe.mirai.utils.config;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MiraiAbstractConfigSection<K,V> implements Map<K,V> {


    private SortedMap<K, V> sortedMap;

    protected void setContent(SortedMap<K,V> map){
        this.sortedMap = map;
    }


    @Override
    public int size() {
        return sortedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return sortedMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return sortedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return sortedMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return sortedMap.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return sortedMap.put(key,value);
    }

    @Override
    public V remove(Object key) {
        return sortedMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        sortedMap.putAll(m);
    }

    @Override
    public void clear() {
        sortedMap.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return sortedMap.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return sortedMap.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return sortedMap.entrySet();
    }
}
