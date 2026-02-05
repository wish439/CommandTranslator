package com.wishtoday.ts.commandtranslator.Cache;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import lombok.Getter;

@Getter
public class CacheInstance {
    private static CacheInstance INSTANCE;

    private ConcurrentBiHashMap<String, String> allCommando2t;
    //private ConcurrentBiHashMap<String, String> textNodeo2t;

    public CacheInstance() {
        this.allCommando2t = new ConcurrentBiHashMap<>();
        //this.textNodeo2t = new ConcurrentBiHashMap<>();
    }

    public synchronized static CacheInstance getINSTANCE() {
        if (INSTANCE == null) loadFromFileOrNew(Commandtranslator.dataSaver);
        return INSTANCE;
    }

    private static void loadFromFileOrNew(DataSaver dataSaver) {
        CacheInstance load = dataSaver.load();
        if (load == null) INSTANCE = new CacheInstance();
        else INSTANCE = load;

        if (INSTANCE.allCommando2t != null) {
            INSTANCE.allCommando2t.rebuildReverseMap();
        }
    }

    @Override
    public String toString() {
        return "CacheInstance{" +
                "allCommando2t=" + allCommando2t +
                '}';
    }

    public boolean isEmpty() {
        return allCommando2t == null; //|| textNodeo2t == null;
    }
}
