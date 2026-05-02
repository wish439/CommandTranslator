package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.DisplayControlAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.RangeAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class CopyToBuilderConfig implements MultiLanguageConfig, ValuableConfig, ApplicationConfig {


    private final ConfigEntry<CopyToBuilderConfig, String> language =
            ConfigEntry.<CopyToBuilderConfig, String>builder()
                    .serializedName("language")
                    .defaultValue("cn")
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "配置文件语言")
                            .comment("en", "Config file language")
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, TranslatorType> translateType =
            ConfigEntry.<CopyToBuilderConfig, TranslatorType>builder()
                    .serializedName("translateType")
                    .defaultValue(TranslatorType.OPENAI)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", """
                                    翻译提供商类型
                                    可选:
                                    OPENAI(默认,目前多数模型api均兼容OPENAI参数,如deepseek/chatgpt)
                                    DEEPL https://www.deepl.com/zh/products/api
                                    如需要其他翻译提供商请提交PR或issue,项目地址:https://github.com/wish439/CommandTranslator
                                    """)
                            .comment("en", """
                                    Translation provider type
                                    Optional:
                                    OPENAI (default, most current model APIs are compatible with OPENAI parameters, such as deepseek/chatgpt)
                                    DEEPL https://www.deepl.com/zh/products/api
                                    If you need other translation providers, please submit a PR or issue at the project address: https://github.com/wish439/CommandTranslator
                                    """)
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, TranslateProvider> translateProvider =
            ConfigEntry.<CopyToBuilderConfig, TranslateProvider>builder()
                    .serializedName("translateProvider")
                    .defaultValue(new TranslateProvider())
                    .child(
                            ConfigEntry.<TranslateProvider, String>builder()
                                    .serializedName("key")
                                    .defaultValue("")
                                    .setter(TranslateProvider::setKey)
                                    .adapter(TranslatableCommentAttitude.builder()
                                            .comment("cn", """
                                                    你的key
                                                    """)
                                            .comment("en", """
                                                    Your key
                                                    """)
                                            .build())
                                    .build()
                    )
                    .child(
                            ConfigEntry.<TranslateProvider, String>builder()
                                    .serializedName("api")
                                    .defaultValue("")
                                    .setter(TranslateProvider::setApi)
                                    .adapter(TranslatableCommentAttitude.builder()
                                            .comment("cn", """
                                                    翻译提供商api
                                                    如
                                                    Deepseek:https://api.deepseek.com/chat/completions
                                                    DEEPL无需填此项
                                                    """
                                            )
                                            .comment("en", """
                                                    translation provider api
                                                    optional
                                                    Deepseek：https：//api.deepseek.com/chat/completions
                                                    DEEPL does not need to fill in this field
                                                    """)
                                            .build())
                                    .build()
                    )
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", """
                                    翻译提供商信息
                                    """)
                            .comment("en", """
                                    Translation provider info
                                    """)
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, String> model =
            ConfigEntry.<CopyToBuilderConfig, String>builder()
                    .serializedName("model")
                    .defaultValue("")
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "AI翻译所用模型(仅在translateType为OPENAI时需要)")
                            .comment("en", "Model used for AI translation (only required if translateType is OPENAI)")
                            .build())
                    .build();


    private final ConfigEntry<CopyToBuilderConfig, MatchMode> matchMode =
            ConfigEntry.<CopyToBuilderConfig, MatchMode>builder()
                    .serializedName("matchMode")
                    .defaultValue(MatchMode.WHITELIST)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", """
                                    命令匹配模式
                                    可选:
                                    WHITELIST -白名单
                                    BLACKLIST -黑名单
                                    如果没有特殊需求请不要随意更改，否则可能导致问题
                                    """)
                            .comment("en", """
                                    Command matching mode
                                    Optional:
                                    WHITELIST - Whitelist
                                    BLACKLIST - BLACKLIST
                                    If you don't have special needs, don't change it at will, otherwise it may cause problems
                                    """)
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, List<String>> commands =
            ConfigEntry.<CopyToBuilderConfig, List<String>>builder()
                    .serializedName("commands")
                    .defaultValue(List.of("me", "msg", "say", "teammsg", "tellraw", "title"))
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", """
                                    匹配命令(依照matchMode)
                                    如果没有特殊需求请不要随意更改，否则可能导致问题
                                    """
                            )
                            .comment("en", """
                                    Matching commands (according to matchMode)
                                    If you don't have special needs, don't change it at will, otherwise it may cause problems
                                    """
                            )
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, Boolean> enableTranslate =
            ConfigEntry.<CopyToBuilderConfig, Boolean>builder()
                    .serializedName("enableTranslate")
                    .defaultValue(false)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "开启翻译")
                            .comment("en", "enable translation")
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, Boolean> translateFunctions =
            ConfigEntry.<CopyToBuilderConfig, Boolean>builder()
                    .serializedName("translateFunctions")
                    .defaultValue(false)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "翻译数据包中的函数")
                            .comment("en", "Translate functions in datapack")
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, Boolean> translateCommandBlocks =
            ConfigEntry.<CopyToBuilderConfig, Boolean>builder()
                    .serializedName("translateCommandBlocks")
                    .defaultValue(false)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "翻译命令方块")
                            .comment("en", "Translate command block")
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, CommandBlockTranslateStrategy> commandBlockTranslateStrategy =
            ConfigEntry.<CopyToBuilderConfig, CommandBlockTranslateStrategy>builder()
                    .serializedName("commandBlockTranslateStrategy")
                    .defaultValue(CommandBlockTranslateStrategy.LOADING)
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", """
                                    翻译命令方块的策略
                                    可选:
                                    TRIGGER -触发时翻译,触发时可能会卡
                                    LOADING -加载后每tick翻译定量命令方块
                                    """
                            )
                            .comment("en", """
                                    Strategies for translating command blocks
                                    Optional:
                                    TRIGGER - Translate when triggered, may get stuck when triggered
                                    LOADING - Translate quantitative command blocks per tick after loading
                                    """
                            )
                            .build())
                    .build();

    private final ConfigEntry<CopyToBuilderConfig, Double> ChineseSentenceJudgmentRange =
            ConfigEntry.<CopyToBuilderConfig, Double>builder()
                    .serializedName("ChineseSentenceJudgmentRange")
                    .defaultValue(0.6D)
                    .adapter(
                            TranslatableCommentAttitude.builder()
                                    .comment("cn", """
                                            汉字占整句话的比例大于此值时被认为是中文句子(中文句子则无需翻译)
                                            范围:-1~1
                                            0以下为关闭此功能
                                            """)
                                    .build()
                    )
                    .adapter(
                            RangeAttitude.builder()
                                    .min(-1D)
                                    .max(1D)
                                    .build()
                    )
                    .adapter(
                            DisplayControlAttitude
                                    .builder()
                                    .onlyDisplay("cn")
                                    .build()
                    )
                    .build();


    private final ConfigEntry<CopyToBuilderConfig, Integer> batchSize =
            ConfigEntry.<CopyToBuilderConfig, Integer>builder()
                    .serializedName("batchSize")
                    .defaultValue(20)
                    .adapter(
                            TranslatableCommentAttitude.builder()
                                    .comment("cn", """
                                            多行翻译一次翻译的数量
                                            """
                                    )
                                    .comment("en", """
                                            Multi-line translations The number of translations at a time
                                            """
                                    )
                                    .build()
                    )
                    .build();


    private final ConfigEntry<CopyToBuilderConfig, Integer> timeout =
            ConfigEntry.<CopyToBuilderConfig, Integer>builder()
                    .serializedName("timeout")
                    .defaultValue(50)
                    .adapter(
                            TranslatableCommentAttitude.builder()
                                    .comment("cn", """
                                            多行翻译超时时间(单位:毫秒)
                                            """
                                    )
                                    .comment("en", """
                                            Multi-line translation timeout time (in milliseconds)
                                            """
                                    )
                                    .build()
                    )
                    .build();

    public TranslatorType getTranslateType() {
        return this.translateType.getValue();
    }

    public TranslateProvider getTranslateProvider() {
        return this.translateProvider.getValue();
    }

    public String getModel() {
        return this.model.getValue();
    }

    public MatchMode getMatchMode() {
        return this.matchMode.getValue();
    }

    public List<String> getCommands() {
        return this.commands.getValue();
    }

    public boolean isEnableTranslate() {
        return this.enableTranslate.getValue();
    }

    public boolean isTranslateCommandBlocks() {
        return this.translateCommandBlocks.getValue();
    }

    public boolean isTranslateFunctions() {
        return this.translateFunctions.getValue();
    }

    public int getBatchSize() {
        return this.batchSize.getValue();
    }

    public int getTimeout() {
        return this.timeout.getValue();
    }

    public double getChineseSentenceJudgmentRange() {
        return this.ChineseSentenceJudgmentRange.getValue();
    }

    public CommandBlockTranslateStrategy getCommandTranslateStrategy() {
        return this.commandBlockTranslateStrategy.getValue();
    }

    @Override
    public String toString() {
        return "CopyToBuilderConfig{" +
                "language=" + language +
                ", translateType=" + translateType +
                ", translateProvider=" + translateProvider +
                ", model=" + model +
                ", matchMode=" + matchMode +
                ", commands=" + commands +
                ", enableTranslate=" + enableTranslate +
                ", translateFunctions=" + translateFunctions +
                ", translateCommandBlocks=" + translateCommandBlocks +
                ", commandBlockTranslateStrategy=" + commandBlockTranslateStrategy +
                ", ChineseSentenceJudgmentRange=" + ChineseSentenceJudgmentRange +
                ", batchSize=" + batchSize +
                ", timeout=" + timeout +
                '}';
    }

    @Override
    public String getCurrentLanguage() {
        return language.getValue();
    }

    public boolean validateCommand(@NotNull String command) {
        return matchMode.getValue() == MatchMode.BLACKLIST ? !commands.getValue().contains(command) : matchMode.getValue() == MatchMode.WHITELIST && commands.getValue().contains(command);
    }

    @Override
    public <T> Optional<T> getConfigValue(String key, Class<T> type) throws ReflectiveOperationException {
        return this.getConfigValue(this, key, type);
    }

    public <T> Optional<T> getConfigValue(Object obj, String key, Class<T> type) throws ReflectiveOperationException {
        ConfigPathReader reader = new ConfigPathReader(key);
        if (!reader.hasNextPath()) return Optional.empty();
        String s = reader.readNextPath();
        Class<?> aClass = obj.getClass();
        Field field = aClass.getDeclaredField(s);
        Object o = field.get(obj);
        if (!(o instanceof ConfigEntry<?, ?> entry)) {
            return Optional.empty();
        }

        Object value = entry.getValue();

        //逆天Class#isAssignableFrom,int跟Integer给我返回false
        if (ClassUtils.isAssignable(type, value.getClass())) {
            return Optional.of((T) value);
        }
        return getConfigValue(value, type, reader);
    }

    public <T> Optional<T> getConfigValue(Object obj, Class<T> type, ConfigPathReader reader) throws ReflectiveOperationException {
        if (!reader.hasNextPath()) return Optional.empty();
        String s = reader.readNextPath();
        Class<?> aClass = obj.getClass();
        Field field = aClass.getDeclaredField(s);
        Object o = field.get(obj);

        if (ClassUtils.isAssignable(o.getClass(), type)) {
            return Optional.of((T) o);
        }
        return getConfigValue(o, type, reader);
    }

    @Override
    public boolean canWorkOn(TranslateEnvironment environment) {
        if (!enableTranslate.getValue()) {
            return false;
        }
        return switch (environment) {
            case COMMAND_BLOCK -> this.translateCommandBlocks.getValue();
            case FUNCTION -> this.translateFunctions.getValue();
            default -> false;
        };
    }

    @Getter
    @Setter
    public static class TranslateProvider {
        @NotNull
        private String key;
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

    public enum MatchMode {
        WHITELIST,
        BLACKLIST
    }

    public enum CommandBlockTranslateStrategy {
        TRIGGER,
        LOADING
    }


}
