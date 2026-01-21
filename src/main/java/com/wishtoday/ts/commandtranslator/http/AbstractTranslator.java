package com.wishtoday.ts.commandtranslator.http;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;

public abstract class AbstractTranslator implements ITranslator {
    protected final String api;
    protected final String key;
    public static final Gson gson = new Gson();
    protected static final OkHttpClient client = new OkHttpClient();
    public AbstractTranslator(String api, String key) {
        this.api = api;
        this.key = key;
    }
}
