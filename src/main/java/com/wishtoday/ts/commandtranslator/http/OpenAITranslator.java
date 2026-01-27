package com.wishtoday.ts.commandtranslator.http;

import com.google.gson.annotations.SerializedName;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenAITranslator extends AITranslator {

    private List<MessageInfo> messages;

    public OpenAITranslator(String api, String key, String model, int contextNumber) {
        super(api, key, model, contextNumber);
        this.messages = new ArrayList<>();
        messages.add(new MessageInfo("system", "你是一个Minecraft使用的翻译机器人,请联系上下文更好的翻译为中文\n请根据以下规则翻译:\n1.只输出翻译结果，原因和思考过程不输出。\n2.不翻译Minecraft术语。\n3.不翻译任何指令格式(如果有)"));
    }

    public OpenAITranslator(String api, String key, String model) {
        super(api, key, model);
        this.messages = new ArrayList<>();
        messages.add(new MessageInfo("system", "你是一个Minecraft使用的翻译机器人,请联系上下文更好的翻译为中文\n请根据以下规则翻译:\n1.只输出翻译结果，原因和思考过程不输出。\n2.不翻译Minecraft术语。\n3.不翻译任何指令格式(如果有)"));
    }

    @Override
    protected void limitContextList() {
        if (messages.size() < this.contextNumber) return;
        List<MessageInfo> newlist = new ArrayList<>();
        newlist.add(messages.getFirst());
        newlist.addAll(messages.subList(Math.max(1, messages.size() - this.contextNumber), messages.size()));
        this.messages = newlist;
    }

    @NotNull
    @Override
    public String translation(String s, String language) {
        this.messages.add(new MessageInfo("user", s + "译为" + language));
        TranslationRequest translationRequest = new TranslationRequest(this.model, this.messages, false);
        RequestBody body = RequestBody.create(gson.toJson(translationRequest), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(this.api)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + this.key)
                .post(body).build();
        String translation = s;
        try (Response execute = client.newCall(request).execute()) {
            if (!execute.isSuccessful()) return translation;
            ResultInfo info = gson.fromJson(execute.body().string(), ResultInfo.class);
            ResultInfo.Result result = info.choices[0];
            translation = result.message.content;
            messages.add(result.message);
            this.limitContextList();
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

    @AllArgsConstructor
    private static class TranslationRequest {
        private String model;
        private List<MessageInfo> messages;
        private boolean stream;
    }

    private static class ResultInfo {
        private Result[] choices;

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
            return gson.toJson(this);
        }
    }
}
