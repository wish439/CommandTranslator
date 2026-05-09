package com.wishtoday.ts.commandtranslator.http;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class Constants {
    static final Gson GSON = new Gson();
    static final OkHttpClient client =
            new OkHttpClient.Builder()
                    .readTimeout(120, TimeUnit.SECONDS)
            .build();
}
