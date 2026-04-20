    package com.wishtoday.ts.commandtranslator.Config;

    import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
    import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
    import lombok.Data;
    import lombok.Getter;

    @Getter
    public class ABuilderConfig implements MultiLanguageConfig {
        private static ABuilderConfig instance = new ABuilderConfig();

        private final ConfigEntry<Config, String> language =
                ConfigEntry.<Config, String>builder()
                        .serializedName("language")
                        .defaultValue("cn")
                        .adapter(TranslatableCommentAttitude.builder()
                                .comment("cn", "配置文件语言")
                                .comment("en", "Config file language")
                                .build())
                        .build();

        private final ConfigEntry<Config, TestObject> test =
                ConfigEntry.<Config, TestObject>builder()
                        .serializedName("test")
                        .defaultValue(new TestObject())
                        .child(
                                ConfigEntry.<TestObject, String>builder()
                                        .serializedName("a")
                                        .defaultValue("default A")
                                        .setter(TestObject::setA)
                                        .adapter(TranslatableCommentAttitude.builder()
                                                .comment("cn", "A 配置文件 A")
                                                .comment("en", "A config file A")
                                                .build())
                                        .build()
                        )
                        .child(
                                ConfigEntry.<TestObject, String>builder()
                                        .serializedName("b")
                                        .defaultValue("default B")
                                        .setter(TestObject::setB)
                                        .adapter(TranslatableCommentAttitude.builder()
                                                .comment("cn", "B 配置文件 B")
                                                .comment("en", "B config file B")
                                                .build())
                                        .build()
                        )
                        .adapter(TranslatableCommentAttitude.builder()
                                .comment("cn", "test字段主对象")
                                .comment("en", "test field root object")
                                .build())
                        .build();

        private final ConfigEntry<Config, TestObject> test2 =
                ConfigEntry.<Config, TestObject>builder()
                        .serializedName("test2")
                        .defaultValue(new TestObject())
                        .child(
                                ConfigEntry.<TestObject, String>builder()
                                        .serializedName("a")
                                        .defaultValue("default A")
                                        .setter(TestObject::setA)
                                        .adapter(TranslatableCommentAttitude.builder()
                                                .comment("en", "A 配置文件 A")
                                                .comment("cn", "A config file A")
                                                .build())
                                        .build()
                        )
                        .child(
                                ConfigEntry.<TestObject, String>builder()
                                        .serializedName("b")
                                        .defaultValue("default B")
                                        .setter(TestObject::setB)
                                        .adapter(TranslatableCommentAttitude.builder()
                                                .comment("en", "B 配置文件 B")
                                                .comment("cn", "B config file B")
                                                .build())
                                        .build()
                        )
                        .adapter(TranslatableCommentAttitude.builder()
                                .comment("en", "test字段主对象")
                                .comment("cn", "test field root object")
                                .build())
                        .build();


        @Data
        public static class TestObject {
            private String a;
            private String b;
        }

        @Override
        public String getCurrentLanguage() {
            return language.getValue();
        }
    }
