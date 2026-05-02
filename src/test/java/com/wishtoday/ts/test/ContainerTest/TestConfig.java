package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.ConfigPathReader;
import com.wishtoday.ts.commandtranslator.Config.ValuableConfig;
import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
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

    private final ConfigEntry<TestConfig, TestClass> testClass =
            ConfigEntry.<TestConfig, TestClass>builder()
                    .serializedName("testClass")
                    .defaultValue(new TestClass("", 0, -1D))
                    .child(
                            ConfigEntry.<TestClass, String>builder()
                                    .serializedName("a")
                                    .defaultValue("default A")
                                    .setter(TestClass::setA)
                                    .build()
                    )
                    .child(
                            ConfigEntry.<TestClass, Integer>builder()
                                    .serializedName("b")
                                    .defaultValue(115)
                                    .setter(TestClass::setB)
                                    .build()
                    )
                    .child(
                            ConfigEntry.<TestClass, Double>builder()
                                    .serializedName("c")
                                    .defaultValue(1789D)
                                    .setter(TestClass::setC)
                                    .build()
                    )
                    .build();



    public class TestClass {
        private String a;
        private int b;
        private double c;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        public double getC() {
            return c;
        }

        public void setC(double c) {
            this.c = c;
        }

        @CreateConstruction
        public TestClass(String a, int b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    public ConfigEntry<TestConfig, TestEnum> getTestEnum() {
        return testEnum;
    }

    private final ConfigEntry<TestConfig, TestEnum> testEnum =
            ConfigEntry.<TestConfig, TestEnum>builder()
                    .serializedName("testEnum")
                    .defaultValue(TestEnum.TESTA)
                    .build();

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

        if (ClassUtils.isAssignable(type, value.getClass())) {
            return Optional.of((T) value);
        }
        return getConfigValue(value, s, type, reader);
    }

    public <T> Optional<T> getConfigValue(Object obj, String key, Class<T> type, ConfigPathReader reader) throws ReflectiveOperationException {
        if (!reader.hasNextPath()) return Optional.empty();
        String s = reader.readNextPath();
        Class<?> aClass = obj.getClass();
        Field field = aClass.getDeclaredField(s);
        Object o = field.get(obj);

        System.out.println(o.getClass().getName());
        boolean assignable = ClassUtils.isAssignable(o.getClass(), type);
        System.out.println(assignable);
        if (assignable) {
            System.out.println("this" + o.getClass().getName());
            return Optional.of((T) o);
        }
        return getConfigValue(o, s, type, reader);
    }


    public enum TestEnum {
        TESTA,
        TESTB,
        TESTC
    }
}
