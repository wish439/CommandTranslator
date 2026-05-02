package com.wishtoday.ts.commandtranslator.Services;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.wishtoday.ts.commandtranslator.Commandtranslator.LOGGER;

public class Container {
    @Getter
    @Setter
    private static Container instance = new Container();

    private final Map<Class<?>, Object> instances;

    public Container() {
        this.instances = new ConcurrentHashMap<>();
    }

    public <T> boolean register(Class<T> type, T object) {
        if (!type.isInstance(object)) {
            LOGGER.warnWithStackTrace("Failed to register: {} is not an instance of {}", object.getClass(), type);
            return false;
        }
        LOGGER.debug("Registered: {} -> {}", type.getSimpleName(), object.getClass().getSimpleName());
        this.instances.put(type, object);
        return true;
    }

    public boolean register(Object object, Class<?>... types) {
        for (Class<?> type : types) {
            if (!type.isInstance(object)) {
                return false;
            }
            this.instances.put(type, object);
        }
        return true;
    }

    public void autoRegister(Object object) {
        List<Class<?>> interfaces = ContainerHelper.getInterfaces(object.getClass());
        LOGGER.debug("Auto-registering: {} with interfaces {}",
                object.getClass().getSimpleName(),
                interfaces.stream().map(Class::getSimpleName).toList());
        Class<?> itSelf = object.getClass();
        for (Class<?> aClass : interfaces) {
            this.instances.put(aClass, object);
        }
        this.instances.put(itSelf, object);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> type) {
        T t = (T) this.instances.get(type);
        if (t == null) {
            LOGGER.warnWithStackTrace("Container.get({}) returned empty", type.getSimpleName(), type);
        }
        return Optional.ofNullable(t);
    }
}
