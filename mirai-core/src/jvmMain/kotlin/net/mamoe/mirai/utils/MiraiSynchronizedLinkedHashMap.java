package net.mamoe.mirai.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 实现了可以直接被继承的 SynchronizedLinkedListMap<K,V>
 *
 * @param <K> the type of key
 * @param <V> the type of value
 *
 * @author NaturalHG
 */
public class MiraiSynchronizedLinkedHashMap<K, V> extends AbstractMap<K, V> {

    public MiraiSynchronizedLinkedHashMap() {
        this.sortedMap = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    protected final Map<K, V> sortedMap;

    public MiraiSynchronizedLinkedHashMap(LinkedHashMap<K, V> map) {
        this.sortedMap = Collections.synchronizedMap(map);
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

    @Override
    public V put(K key, V value) {
        return sortedMap.put(key,value);
    }

    @Override
    public V remove(Object key) {
        return sortedMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        sortedMap.putAll(m);
    }

    @Override
    public void clear() {
        sortedMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return sortedMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return sortedMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return sortedMap.entrySet();
    }

    @Override
    public String toString() {
        return this.sortedMap.toString();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return this.sortedMap.getOrDefault(key,defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.sortedMap.forEach(action);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return this.sortedMap.replace(key,oldValue,newValue);
    }

    @Override
    public V replace(K key, V value) {
        return this.sortedMap.replace(key,value);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.sortedMap.replaceAll(function);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.sortedMap.compute(key,remappingFunction);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return this.sortedMap.computeIfAbsent(key,mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.sortedMap.computeIfPresent(key,remappingFunction);
    }

    @Override
    public int hashCode() {
        return this.sortedMap.hashCode();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return this.sortedMap.putIfAbsent(key,value);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return this.sortedMap.merge(key,value,remappingFunction);
    }

    public boolean equals(MiraiSynchronizedLinkedHashMap o) {
        return this.sortedMap.equals(o.sortedMap);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MiraiSynchronizedLinkedHashMap ? this.equals((MiraiSynchronizedLinkedHashMap) o) : super.equals(o);
    }

}
