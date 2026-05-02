package com.wishtoday.ts.commandtranslator.Services;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.ValuableConfig;
import com.wishtoday.ts.commandtranslator.Exception.RecursionDeepException;
import com.wishtoday.ts.commandtranslator.Util.TypeUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ObjectFactory {
    private final Container container;
    private final ValuableConfig valuableConfig;
    private static final int MAX_DEPTH = 10;

    public ObjectFactory(Container container, ValuableConfig valuableConfig) {
        this.container = container;
        this.valuableConfig = valuableConfig;
    }

    public <T> Optional<T> create(Class<T> clazz) {
        return this.create(clazz, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> create(Class<T> clazz, int depth) {
        Constructor<?> constructor = this.getAppropriateConstructor(clazz);
        if (constructor == null) {
            return Optional.empty();
        }
        constructor.setAccessible(true);
        Class<?>[] types = constructor.getParameterTypes();
        List<Object> args = new ArrayList<>();
        int i = 0;
        try {
            for (Class<?> type : types) {
                Optional<?> o = this.getObject(type, tryGetConfigValue(type, i++, constructor));
                if (o.isPresent()) {
                    args.add(o.get());
                    continue;
                }
                if (depth >= MAX_DEPTH) {
                    Commandtranslator.LOGGER.error("Recursion is too deep, stopped.", new RecursionDeepException());
                    return Optional.empty();
                }
                Optional<?> t = this.create(type, ++depth);
                t.ifPresent(args::add);
            }
            return Optional.of((T) constructor.newInstance(args.toArray()));
        } catch (Exception e) {
            Commandtranslator.LOGGER.error("threw a exception when created {}", clazz.getName(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("OptionalAssignedToNull")
    private Optional<?> getObject(Class<?> type, ConfigValue configValue) throws ReflectiveOperationException {
        Optional<?> o = Optional.empty();
        if (!TypeUtils.isSimpleType(type)) {
            o = this.container.get(type);
        }
        if (o.isEmpty() && TypeUtils.isSimpleType(type) && configValue != null) {
            o = this.valuableConfig.getConfigValue(configValue.value(), type);
        }
        return o == null || o.isEmpty() ? Optional.empty() : o;
    }

    @Nullable
    private ConfigValue tryGetConfigValue(Class<?> clazz, int argIndex, Constructor<?> constructor) {
        ConfigValue annotation = clazz.getAnnotation(ConfigValue.class);
        if (annotation != null) {
            return annotation;
        }
        Annotation[][] annotations = constructor.getParameterAnnotations();
        Annotation[] annotation1 = annotations[argIndex];
        for (Annotation annotation2 : annotation1) {
            if (annotation2 instanceof ConfigValue configValue) {
                return configValue;
            }
        }
        return null;
    }

    private Constructor<?> getAppropriateConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        int count = 0;
        Constructor<?> c = null;
        for (Constructor<?> constructor : constructors) {
            if (!constructor.isAnnotationPresent(CreateConstruction.class)) {
                continue;
            }
            count++;
            c = constructor;
        }
        if (count > 1) {
            throw new IllegalStateException(clazz.getName() + " has more than one constructor annotated with @CreateConstruction");
        }
        return c;
    }
}
