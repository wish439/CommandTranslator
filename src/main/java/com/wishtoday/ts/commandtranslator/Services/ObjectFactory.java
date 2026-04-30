package com.wishtoday.ts.commandtranslator.Services;

import com.wishtoday.ts.commandtranslator.Commandtranslator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObjectFactory {
    private final Container container;
    public ObjectFactory(Container container) {
        this.container = container;
    }

    public <T> Optional<T> create(Class<T> clazz) {
        Constructor<?> constructor = this.getAppropriateConstructor(clazz);
        if (constructor == null) {
            return Optional.empty();
        }
        constructor.setAccessible(true);
        Class<?>[] types = constructor.getParameterTypes();
        List<Object> args = new ArrayList<>();
        for (Class<?> type : types) {
            Optional<?> o = this.container.get(type);
            o.ifPresent(args::add);
        }
        try {
            return Optional.of((T) constructor.newInstance(args.toArray()));
        } catch (Exception e) {
            Commandtranslator.LOGGER.error("threw a exception when created {}", clazz.getName(), e);
            return Optional.empty();
        }
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
