package com.wishtoday.ts.commandtranslator.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import org.apache.commons.lang3.exception.UncheckedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class ConfigLoader {

    public static <T> T load(Supplier<T> supplier, Path path) {

        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .autosave()
                .sync()
                .build();

        config.load();

        try {
            T instance = supplier.get();

            loadFields(config, instance, "");

            config.save();

            return instance;
        } catch (Exception e) {
            Commandtranslator.modActive = false;
            Commandtranslator.LOGGER.error(e.getMessage());
            throw new UncheckedException(e);
        }
    }

    private static void loadFields(CommentedFileConfig config, Object obj, String prefix)
            throws Exception {

        for (Field field : obj.getClass().getDeclaredFields()) {

            if (Modifier.isStatic(field.getModifiers())) continue;

            field.setAccessible(true);

            SerializedName annotation = field.getAnnotation(SerializedName.class);
            String name;
            if (annotation != null) {
                name = annotation.value();
            } else {
                name = field.getName();
            }

            String key = prefix + name;
            Object value = field.get(obj);
            Class<?> type = field.getType();

            Comment comment = field.getAnnotation(Comment.class);

            if (comment != null) {
                config.setComment(key, String.join("\n", comment.value()));
            }

            // ===== 基础类型 =====
            if (isSimpleType(type)) {

                if (!config.contains(key)) {
                    config.set(key, value);
                }

                Object read = config.get(key);

                if (type.isEnum() && read != null) {
                    read = Enum.valueOf((Class<Enum>) type, read.toString());
                }

                field.set(obj, read);
                continue;
            }

            // ===== List =====
            if (List.class.isAssignableFrom(type)) {

                if (!config.contains(key)) {
                    config.set(key, value);
                }

                Object read = config.get(key);
                field.set(obj, read);
                continue;
            }

            // ===== 嵌套对象 =====
            if (value == null) {
                value = type.getDeclaredConstructor().newInstance();
                field.set(obj, value);
            }

            loadFields(config, value, key + ".");
        }
    }

    private static boolean isSimpleType(Class<?> type) {

        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class
                || type.isEnum();
    }

    private static boolean isPrimitive(Class<?> type) {

        return type == int.class ||
                type == boolean.class ||
                type == double.class ||
                type == long.class ||
                type == float.class ||
                type == short.class ||
                type == byte.class ||
                type == char.class;
    }
}