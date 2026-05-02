package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.*;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.AnnotationAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter.FieldTypeAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.SerializedName;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigClassInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;
import org.apache.commons.lang3.exception.UncheckedException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AnnotationConfigLoader<T> implements IConfigLoader<T> {



    public AnnotationConfigLoader(Map<Class<? extends Annotation>, AnnotationAdapter<? extends Annotation>> annotationAdapters, Set<FieldTypeAdapter<?>> fieldTypeAdapters) {
        this.annotationAdapters = annotationAdapters;
        this.fieldTypeAdapters = fieldTypeAdapters;
    }

    private final Map<Class<?>, List<Field>> fields = new ConcurrentHashMap<>();

    private final Map<Class<? extends Annotation>, AnnotationAdapter<? extends Annotation>> annotationAdapters;

    private final Set<FieldTypeAdapter<?>> fieldTypeAdapters;
    @Override
    public T load(Supplier<T> supplier, Path path) {

        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .autosave()
                .sync()
                .preserveInsertionOrder()
                .build();

        config.load();

        try {
            T instance = supplier.get();

            loadFields(config, instance, "");

            config.save();

            return instance;
        } catch (Exception e) {
            Commandtranslator.setModActive(false);
            Commandtranslator.LOGGER.error("ConfigLoader#load loading failed, threw{}", e.getMessage());
            throw new UncheckedException(e);
        }
    }

    public void loadFields(CommentedFileConfig config, Object obj, String prefix)
            throws Exception {

        Class<?> aClass = obj.getClass();
        if (!fields.containsKey(aClass)) {
            cacheOrderedFields(aClass);
        }

        List<Field> list = fields.get(aClass);

        ProcessingContext context = new ProcessingContext(config, this::loadFields);

        OUTSIDE:
        for (Field field : list) {

            field.trySetAccessible();

            String name = getName(field);

            String key = prefix + name;
            Object value = field.get(obj);

            for (FieldTypeAdapter<?> fieldTypeAdapter : this.fieldTypeAdapters) {
                ConfigFieldInfo<Object> objectConfigFieldInfo = new ConfigFieldInfo<>(key, value, field, new ConfigClassInfo<>(obj));
                if (!fieldTypeAdapter.shouldApply(context, objectConfigFieldInfo)) {
                    continue;
                }
                if (!config.contains(key)) {
                    config.set(key, value);
                }
                Object read = fieldTypeAdapter.read(context, new ConfigFieldInfo<>(key, value, field, new ConfigClassInfo<>(obj)));
                fieldTypeAdapter.write(context, objectConfigFieldInfo, read);

                Annotation[] annotations = field.getAnnotations();
                INSIDE:
                for (Annotation annotation : annotations) {
                    AnnotationAdapter<? extends Annotation> adapter = annotationAdapters.get(annotation.annotationType());
                    if (adapter == null) {
                        continue INSIDE;
                    }
                    AnnotationInfo<?, Annotation> info = new AnnotationInfo<>(key, value, field, new ConfigClassInfo<>(obj.getClass().getName(), obj, obj.getClass()), annotation);
                    boolean apply = adapter.apply(config, info);
                    if (apply) {
                        continue OUTSIDE;
                    }
                }
                continue OUTSIDE;
            }

            /*if (value == null) {
                value = instance.getDeclaredConstructor().newInstance();
                field.set(obj, value);
            }*/

            //loadFields(config, value, key + ".");
        }
    }

    private static String getName(Field field) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        return serializedName == null ? field.getName() : serializedName.value();
    }

    /**
     * This is a way to ensure the order of the fields, using ASM
     *
     */
    private void cacheOrderedFields(Class<?> aClass) throws IOException {
        ClassReader reader = new ClassReader(Objects.requireNonNull(aClass.getResourceAsStream(
                "/" + aClass.getName().replace('.', '/') + ".class"
        )));

        Map<String, Field> fieldMap = new HashMap<>();
        List<String> names = new ArrayList<>();

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (Modifier.isStatic(access)) return super.visitField(access, name, descriptor, signature, value);
                names.add(name);
                return super.visitField(access, name, descriptor, signature, value);
            }
        };
        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (Field field : aClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) continue;
            fieldMap.put(field.getName(), field);
        }
        List<Field> list = names.stream()
                .map(fieldMap::get)
                .filter(Objects::nonNull)
                .toList();
        fields.put(aClass, list);
    }

    /*private static String filterUnblank(String delimiter, String s) {
        String[] split = s.split(delimiter);
        List<String> list = Arrays.stream(split)
                .filter(a -> !a.isBlank())
                .toList();
        return String.join(delimiter, list);
    }

    private static Number clampNumber(Number value, Range range, Class<?> instance) {

        if (instance == int.class || instance == Integer.class) {
            int v = value.intValue();
            return Math.min(Math.max(range.minInt(), v), range.maxInt());
        }

        if (instance == long.class || instance == Long.class) {
            long v = value.longValue();
            return Math.min(Math.max(range.minLong(), v), range.maxLong());
        }

        if (instance == float.class || instance == Float.class) {
            float v = value.floatValue();
            return Math.min(Math.max(range.minFloat(), v), range.maxFloat());
        }

        if (instance == double.class || instance == Double.class) {
            double v = value.doubleValue();
            return Math.min(Math.max(range.minDouble(), v), range.maxDouble());
        }

        return value;
    }

    private static boolean isSimpleType(Class<?> instance) {

        return instance.isPrimitive()
                || instance == String.class
                || Number.class.isAssignableFrom(instance)
                || instance == Boolean.class
                || instance == Integer.class
                || instance == Long.class
                || instance == Double.class
                || instance == Float.class
                || instance == Short.class
                || instance == Byte.class
                || instance == Character.class
                || instance.isEnum();
    }*/
}