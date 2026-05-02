package com.wishtoday.ts.commandtranslator.Cache;

import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import lombok.Getter;

@Getter
public class CacheInstance {

    private ConcurrentBiHashMap<String, String> allCommando2t;
    //private ConcurrentBiHashMap<String, String> textNodeo2t;

    @CreateConstruction
    public CacheInstance(DataSaver dataSaver) {
        CacheInstance load = dataSaver.load();
        if (load != null) {
            this.allCommando2t = load.allCommando2t;
            return;
        }
        this.allCommando2t = new ConcurrentBiHashMap<>();
        allCommando2t.rebuildReverseMap();
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
