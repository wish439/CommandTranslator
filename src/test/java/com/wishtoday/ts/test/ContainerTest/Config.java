package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.*;
import com.wishtoday.ts.commandtranslator.Config.MultiLanguageConfig;
import com.wishtoday.ts.commandtranslator.Config.ValuableConfig;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

//TODO: switch to BuilderConfig system.
//Completed, look the CopyToBuilderConfig
@Setter
@Getter
@Deprecated
public class Config implements MultiLanguageConfig, ValuableConfig {
    private static Config instance;


    @TranslatableComment(value = {"""
            配置文件语言,默认为中文
            可选:
            cn -中文
            en -English
            """,
            """
            The configuration file language is Chinese by default
            Optional:
            cn - Chinese
            en -English
            """}
            , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    private String configLang;

    @TranslatableComment(value = {
            """
            翻译提供商类型
            可选:
            OPENAI(默认,目前多数模型api均兼容OPENAI参数,如deepseek/chatgpt)
            DEEPL https://www.deepl.com/zh/products/api
            如需要其他翻译提供商请提交PR或issue,项目地址:https://github.com/wish439/CommandTranslator
            """,
            """
            Translation provider type
            Optional:
            OPENAI (default, most current model APIs are compatible with OPENAI parameters, such as deepseek/chatgpt)
            DEEPL https://www.deepl.com/zh/products/api
            If you need other translation providers, please submit a PR or issue at the project address: https://github.com/wish439/CommandTranslator
            """
    }
    , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    @SerializedName("translateType")
    private TranslatorType type;
    @TranslatableComment(value = {
            """
            翻译提供商信息
            """,
            """
            Translation provider info
            """
    }
    , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    @SerializedName("translateProvider")
    private TranslateProvider provider;
    @TranslatableComment(value = {
            "AI翻译所用模型(仅在translateType为OPENAI时需要)",
            "Model used for AI translation (only required if translateType is OPENAI)"
    }
    , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    @SerializedName("AITranslateModel")
    private String model;
    @TranslatableComment(value = {
            """
            命令匹配模式
            可选:
            WHITELIST -白名单
            BLACKLIST -黑名单
            如果没有特殊需求请不要随意更改，否则可能导致问题
            """,
            """
            Command matching mode
            Optional:
            WHITELIST - Whitelist
            BLACKLIST - BLACKLIST
            If you don't have special needs, don't change it at will, otherwise it may cause problems
            """
    }
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private MatchMode matchMode;
    @TranslatableComment(value = {
            """
            匹配命令(依照matchMode)
            如果没有特殊需求请不要随意更改，否则可能导致问题
            """
            , """
            Matching commands (according to matchMode)
            If you don't have special needs, don't change it at will, otherwise it may cause problems
            """
    }
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private List<String> commands;
    @TranslatableComment(value = {"开启翻译", "enable translation"}
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private boolean enableTranslate;
    @TranslatableComment(value = {"翻译数据包中的函数", "Translate functions in datapack"}
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private boolean translateFunctions;
    @TranslatableComment(value = {"翻译命令方块", "Translate command block"}
            , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    private boolean translateCommandBlocks;
    @TranslatableComment(value = {
            """
            翻译命令方块的策略
            可选:
            TRIGGER -触发时翻译,触发时可能会卡
            LOADING -加载后每tick翻译定量命令方块
            """
            , """
            Strategies for translating command blocks
            Optional:
            TRIGGER - Translate when triggered, may get stuck when triggered
            LOADING - Translate quantitative command blocks per tick after loading
            """
    }
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private CommandBlockTranslateStrategy commandBlockTranslateStrategy;

    @Comment("""
            汉字占整句话的比例大于此值时被认为是中文句子(中文句子则无需翻译)
            范围:-1~1
            0以下为关闭此功能
            """)
    @NotDisplayIn(value = "en")
    @Range(minDouble = -1D, maxDouble = 1.0D)
    private double ChineseSentenceJudgmentRange;

    @TranslatableComment(value = {
            """
            多行翻译一次翻译的数量
            """
            , """
            Multi-line translations The number of translations at a time
            """
    }
    , defaultValueIndexInValue = 0
    , language = {"cn", "en"})
    private int batchSize;
    @TranslatableComment(value = {
            """
            多行翻译超时时间(单位:毫秒)
            """
            , """
            Multi-line translation timeout time (in milliseconds)
            """
    }
            , defaultValueIndexInValue = 0
            , language = {"cn", "en"})
    private long timeout;

    private static final ReentrantLock lock = new ReentrantLock();

    public Config() {
        this.configLang = "cn";
        this.enableTranslate = false;
        this.translateFunctions = false;
        this.translateCommandBlocks = false;
        this.model = "";
        this.type = TranslatorType.OPENAI;
        this.provider = new TranslateProvider();
        this.matchMode = MatchMode.WHITELIST;
        this.commands = List.of("me", "msg", "say", "teammsg", "tellraw", "title");
        this.commandBlockTranslateStrategy = CommandBlockTranslateStrategy.LOADING;
        this.ChineseSentenceJudgmentRange = 0.6;
        this.batchSize = 20;
        this.timeout = 50;
    }

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
                "instance=" + type +
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

    public boolean validate(@NotNull String command) {
        return matchMode == MatchMode.BLACKLIST ? !commands.contains(command) : matchMode == MatchMode.WHITELIST && commands.contains(command);
    }

    @Override
    public String getCurrentLanguage() {
        return this.configLang;
    }

    @Override
    public <T> Optional<T> getConfigValue(String key, Class<T> type) {
        return Optional.empty();
    }

    @Getter
    public static class TranslateProvider {
        @TranslatableComment(value = {
                """
                你的key
                """
                , """
                Your key
                """
        }
        , defaultValueIndexInValue = 0
        , language = {"cn", "en"})
        @NotNull
        private String key;

        @TranslatableComment(value = {
                """
                翻译提供商api
                如
                Deepseek:https://api.deepseek.com/chat/completions
                DEEPL无需填此项
                """
                , """
                translation provider api
                optional
                Deepseek：https：//api.deepseek.com/chat/completions
                DEEPL does not need to fill in this field
                """
        }
                , defaultValueIndexInValue = 0
                , language = {"cn", "en"})
        @NotNull
        private String api;

        public TranslateProvider(@NotNull String key, @NotNull String api) {
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
}
