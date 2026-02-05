package com.wishtoday.ts.commandtranslator.Cache;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentBiHashMap<K,V> {
    @SerializedName("map")
    @Getter
    private ConcurrentHashMap<K, V> k2v;
    private transient ConcurrentHashMap<V, K> v2k;

    public ConcurrentBiHashMap() {
        k2v = new ConcurrentHashMap<>();
        v2k = new ConcurrentHashMap<>();
    }

    public void rebuildReverseMap() {
        if (v2k == null) {
            v2k = new ConcurrentHashMap<>();
        }
        v2k.clear();
        for (HashMap.Entry<K, V> entry : k2v.entrySet()) {
            v2k.put(entry.getValue(), entry.getKey());
        }
    }

    public V getValue(K key) {
        return k2v.get(key);
    }

    public K getKey(V value) {
        if (v2k == null) {
            rebuildReverseMap();
        }
        return v2k.get(value);
    }

    public void put(K key, V value) {
        k2v.put(key, value);
        if (v2k == null) {
            v2k = new ConcurrentHashMap<>();
        }
        v2k.put(value, key);
    }

    public void removeValue(K key) {
        k2v.remove(key);
    }

    public void removeKey(V key) {
        v2k.remove(key);
    }

    public void remove(K key, V value) {
        removeValue(key);
        removeKey(value);
    }

    public void clear() {
        k2v.clear();
        v2k.clear();
    }

    @Override
    public String toString() {
        return this.k2v.toString() + "\n" + this.v2k.toString();
    }

    public boolean containsKey(K key) {
        return k2v.containsKey(key);
    }
    public boolean containsValue(V value) {
        return v2k.containsKey(value);
    }
}
