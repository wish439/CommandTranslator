package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.ValuableConfig;

import java.util.Optional;

public class Config implements ValuableConfig {

    private final ConfigEntry<Config, String> language =
            ConfigEntry.<Config, String>builder()
                    .serializedName("language")
                    .defaultValue("cn")
                    .adapter(TranslatableCommentAttitude.builder()
                            .comment("cn", "配置文件语言")
                            .comment("en", "Config file language")
                            .build())
                    .build();

    private final ConfigEntry<Config, Integer> test =
            ConfigEntry.<Config, Integer>builder()
                    .serializedName("testint")
                    .defaultValue(10)
                    .build();

    private final ConfigEntry<Config, Double> trial =
            ConfigEntry.<Config, Double>builder()
                    .serializedName("trial")
                    .defaultValue(114514D)
                    .build();


    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getConfigValue(String key, Class<T> type) {
        try {
            return Optional.ofNullable(((ConfigEntry<Config, T>) this.getClass()
                    .getDeclaredField(key)
                    .get(this)).getValue());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
