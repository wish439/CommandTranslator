package com.wishtoday.ts.commandtranslator.http;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenAITranslator implements ITranslator {

    private List<MessageInfo> messages;
    private final AIModelInfo aiModelInfo;
    private final ValidateInfo validateInfo;

    public OpenAITranslator(String api, String key, String model, int contextNumber) {
        this.messages = new ArrayList<>();
        messages.add(new MessageInfo("system", """
                你是一个Minecraft使用的翻译机器人,请联系上下文更好的翻译
                请根据以下规则翻译:
                1.只输出翻译结果，原因和思考过程不输出。
                2.不翻译Minecraft术语。
                3.不翻译任何指令格式(如果有)
                4.如果是json数组，请保持json格式
                """));
        this.aiModelInfo = new AIModelInfo(model, contextNumber);
        this.validateInfo = new ValidateInfo(api, key);
    }

    public OpenAITranslator(String api, String key, String model) {
        this(api, key, model, 20);
    }

    @NotNull
    @Override
    public String translation(String s, String language) {

        List<MessageInfo> requestMessages = new ArrayList<>();

        requestMessages.add(new MessageInfo("user", "请翻译为" + language + "，只输出翻译：\n" + s));

        TranslationRequest translationRequest =
                new TranslationRequest(this.aiModelInfo.model(), requestMessages, false);

        RequestBody body = RequestBody.create(
                Constants.GSON.toJson(translationRequest),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(this.validateInfo.api())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + this.validateInfo.key())
                .post(body)
                .build();

        String translation = s;

        try (Response execute = Constants.client.newCall(request).execute()) {

            if (!execute.isSuccessful()) return translation;

            ResultInfo info = Constants.GSON.fromJson(
                    execute.body().string(),
                    ResultInfo.class
            );

            ResultInfo.Result result = info.choices[0];

            System.out.println(info);

            translation = result.message.content;

        } catch (IOException e) {
            Commandtranslator.LOGGER.error("OpenAITranslator error", e);
        }

        return translation;
    }

    @NotNull
    @Override
    public String translation(String s) {
        return this.translation(s, "中文");
    }

    @Override
    public List<String> translate(List<String> strings) {
        String json = Constants.GSON.toJson(strings);
        Commandtranslator.LOGGER.info("submit to translator:{}", json);
        String translation = this.translation(json);
        Commandtranslator.LOGGER.info("receive the translation:{}", translation);
        return Constants.GSON.fromJson(translation, new TypeToken<List<String>>() {}.getType());
    }

    @AllArgsConstructor
    private static class TranslationRequest {
        private String model;
        private List<MessageInfo> messages;
        private boolean stream;
    }

    @ToString
    private static class ResultInfo {
        private Result[] choices;

        @ToString
        private static class Result {
            private MessageInfo message;
        }
    }

    @Getter
    private static class MessageInfo {
        @SerializedName("role")
        private String role;
        @SerializedName("content")
        private String content;

        public MessageInfo(String role, String content) {
            this.role = role;
            this.content = content;
        }

        @Override
        public String toString() {
            return Constants.GSON.toJson(this);
        }
    }
}
