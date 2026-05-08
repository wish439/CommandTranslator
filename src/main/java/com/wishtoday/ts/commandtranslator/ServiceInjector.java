package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Services.Container;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ServiceInjector {
    public static void inject(Object object) {

        Class<?> aClass = object.getClass();
        if (!aClass.isAnnotationPresent(ServiceClass.class)) {
            return;
        }
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.trySetAccessible()) continue;
            if (!field.isAnnotationPresent(ServiceField.class)) {
                continue;
            }
            Optional<?> o = Container.getInstance().get(field.getType());

            if (o.isPresent()) {
                Object obj = null;
                if (!Modifier.isStatic(field.getModifiers())) {
                    obj = object;
                }
                try {
                    field.set(obj, o.get());
                } catch (IllegalAccessException e) {
                    Commandtranslator.LOGGER.errorWithCaller("Cannot inject field " + field.getName(), e);
                }
                return;
            }
            Commandtranslator.LOGGER.warnWithCaller("Cannot inject field {} because container does not have {}", field.getName(), object.getClass());
        }
    }
}
