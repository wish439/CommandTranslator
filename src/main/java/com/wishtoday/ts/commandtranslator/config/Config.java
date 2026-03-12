package com.wishtoday.ts.commandtranslator.config;

import com.wishtoday.ts.commandtranslator.Translator.TranslatorType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Setter
@Getter
public class Config {
    private static Config instance;

    @Comment("""
            翻译提供商类型
            可选:
            OPENAI(默认,deepseek/ChatGPT可用此类型)
            DEEPL https://www.deepl.com/zh/products/api
            """)
    @SerializedName("translateType")
    private TranslatorType type;
    @Comment("""
            翻译提供商信息
            """)
    @SerializedName("translateProvider")
    private TranslateProvider provider;
    @Comment("AI翻译所用模型(仅在translateType为OPENAI时需要)")
    @SerializedName("AITranslateModel")
    private String model;
    @Comment("""
            命令匹配模式
            可选:
            WHITELIST -白名单
            BLACKLIST -黑名单
            如果没有特殊需求请不要随意更改，否则可能导致问题
            """)
    private MatchMode matchMode;
    @Comment("""
            匹配命令(依照matchMode)
            如果没有特殊需求请不要随意更改，否则可能导致问题
            """)
    private List<String> commands;
    @Comment("开启翻译")
    private boolean enableTranslate;
    @Comment("翻译数据包中的函数")
    private boolean translateFunctions;
    @Comment("翻译命令方块")
    private boolean translateCommandBlocks;
    @Comment("""
            翻译命令方块的策略
            可选:
            TRIGGER -触发时翻译,触发时可能会卡
            LOADING -加载后每tick翻译定量命令方块
            """)
    private CommandBlockTranslateStrategy commandBlockTranslateStrategy;

    private static final ReentrantLock lock = new ReentrantLock();

    public static synchronized Config getInstance() {
        try {
            lock.lock();
            return instance;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Config{" +
                "type=" + type +
                ", provider=" + provider +
                ", model='" + model + '\'' +
                ", enableTranslate=" + enableTranslate +
                ", translateFunctions=" + translateFunctions +
                ", translateCommandBlocks=" + translateCommandBlocks +
                ", matchMode=" + matchMode +
                ", commands=" + commands +
                '}';
    }

    public static synchronized void setConfig(@Nullable Config instances) {
        synchronized (Config.class) {
            if (instances == null) {
                instance = new Config();
                return;
            }
            instance = instances;
        }
    }

    public boolean isEmpty() {
        return this.type == null && this.provider == null && this.commands == null;
    }

    public Config() {
        this.enableTranslate = false;
        this.translateFunctions = false;
        this.translateCommandBlocks = false;
        this.model = "";
        this.type = TranslatorType.OPENAI;
        this.provider = new TranslateProvider();
        this.matchMode = MatchMode.WHITELIST;
        this.commands = List.of("me", "msg", "say", "teammsg", "tellraw", "title");
        this.commandBlockTranslateStrategy = CommandBlockTranslateStrategy.LOADING;
    }

    public boolean validate(@NotNull String command) {
        return matchMode == MatchMode.BLACKLIST ? !commands.contains(command) : matchMode == MatchMode.WHITELIST && commands.contains(command);
    }

    @Getter
    public static class TranslateProvider {
        @Comment("""
                你的key
                """)
        @NotNull
        private String key;
        @Comment("""
                翻译提供商api
                如
                Deepseek:https://api.deepseek.com/chat/completions
                
                DEEPL无需填此项
                """)
        @NotNull
        private String api;

        public TranslateProvider(String key, String api) {
            this.key = key;
            this.api = api;
        }

        @Override
        public String toString() {
            return "TranslateProvider{" +
                    "key='" + key + '\'' +
                    ", api='" + api + '\'' +
                    '}';
        }

        public TranslateProvider() {
            this.key = "";
            this.api = "";
        }
    }

    public enum MatchMode {
        WHITELIST,
        BLACKLIST
    }

    public enum CommandBlockTranslateStrategy {
        TRIGGER,
        LOADING
    }
}
