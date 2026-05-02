package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.ValuableConfig;

import java.util.Optional;

public class TestConfig implements ValuableConfig {

    private final ConfigEntry<TestConfig, String> language =
            ConfigEntry.<TestConfig, String>builder()
                    .serializedName("language")
                    .defaultValue("cn")
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "配置文件语言")
                            .comment("en", "Config file language")
                            .build())
                    .build();

    private final ConfigEntry<TestConfig, Integer> test =
            ConfigEntry.<TestConfig, Integer>builder()
                    .serializedName("testint")
                    .defaultValue(10)
                    .build();

    private final ConfigEntry<TestConfig, Double> trial =
            ConfigEntry.<TestConfig, Double>builder()
                    .serializedName("trial")
                    .defaultValue(114514D)
                    .build();

    public ConfigEntry<TestConfig, TestEnum> getTestEnum() {
        return testEnum;
    }

    private final ConfigEntry<TestConfig, TestEnum> testEnum =
            ConfigEntry.<TestConfig, TestEnum>builder()
                    .serializedName("testEnum")
                    .defaultValue(TestEnum.TESTA)
                    .build();


    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getConfigValue(String key, Class<T> type) {
        try {
            return Optional.ofNullable(((ConfigEntry<TestConfig, T>) this.getClass()
                    .getDeclaredField(key)
                    .get(this)).getValue());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public enum TestEnum {
        TESTA,
        TESTB,
        TESTC
    }
}
