package com.wishtoday.ts.commandtranslator.Config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.AnnotationAdapter;
import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.ClassInfo;
import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter.FieldTypeAdapter;
import com.wishtoday.ts.commandtranslator.Config.Annotation.SerializedName;
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

public class ConfigLoader<T> implements IConfigLoader<T> {

    private final Map<Class<?>, List<Field>> fields = new ConcurrentHashMap<>();

    private final Map<Class<? extends Annotation>, AnnotationAdapter<? extends Annotation>> annotationAdapters = new ConcurrentHashMap<>();

    private final Set<FieldTypeAdapter<?>> fieldTypeAdapters = new TreeSet<>(
            Comparator.<FieldTypeAdapter<?>>comparingInt(FieldTypeAdapter::priority)
                    .thenComparing(a -> a.getClass().getName())
    );
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
            Commandtranslator.LOGGER.error("ConfigLoader#load threw{}", e.getMessage());
            throw new UncheckedException(e);
        }
    }

    @Override
    public <A extends Annotation> void registerAnnotationAdapter(Class<A> clazz, AnnotationAdapter<A> annotationAdapter) {
        if (!clazz.isAssignableFrom(annotationAdapter.getAnnotationClass())) {
            throw new IllegalArgumentException(annotationAdapter.getAnnotationClass() + " is not assignable to " + clazz);
        }
        this.annotationAdapters.put(annotationAdapter.getAnnotationClass(), annotationAdapter);
    }

    @Override
    public <A> void registerFieldTypeAdapter(FieldTypeAdapter<A> fieldTypeAdapter) {
        this.fieldTypeAdapters.add(fieldTypeAdapter);
    }

    public void loadFields(CommentedFileConfig config, Object obj, String prefix)
            throws Exception {

        Class<?> aClass = obj.getClass();
        if (!fields.containsKey(aClass)) {
            cacheOrderedFields(aClass);
        }

        List<Field> list = fields.get(aClass);

        OUTSIDE:
        for (Field field : list) {

            field.trySetAccessible();

            String name = getName(field);

            String key = prefix + name;
            Object value = field.get(obj);
            Class<?> type = field.getType();



            /*for (Map.Entry<Class<? extends Annotation>, AnnotationAdapter<? extends Annotation>> entry : annotationAdapters.entrySet()) {
                AnnotationInfo<?, Comment> info = new AnnotationInfo<>(key, value, field, comment, new ClassInfo<>(obj.getClass().getName(), obj, obj.getClass()));
                AnnotationAdapter<? extends Annotation> adapter = entry.getValue();
                adapter.apply(config, info);
            }*/
            /*Comment comment = field.getAnnotation(Comment.class);

            if (comment != null) {
                //System.out.println(comment.value());
                config.setComment(key, filterUnblank("\n", comment.value()));
            }

            if (isSimpleType(type)) {

                if (!config.contains(key)) {
                    config.set(key, value);
                }

                Object read = config.get(key);

                Range range = field.getAnnotation(Range.class);
                if (range != null && read instanceof Number n) {
                    read = clampNumber(n, range, type);
                }

                if (type.isEnum() && read != null) {
                    read = Enum.valueOf((Class<Enum>) type, read.toString());
                }

                field.set(obj, read);
                continue;
            }*/

            /*if (List.class.isAssignableFrom(type)) {

                if (!config.contains(key)) {
                    config.set(key, value);
                }

                Object read = config.get(key);
                field.set(obj, read);
                continue;
            }*/

            for (FieldTypeAdapter<?> fieldTypeAdapter : this.fieldTypeAdapters) {
                System.out.println("fieldTypeAdapter: " + fieldTypeAdapter.getClass().getName());
                System.out.println("read:" + config.get(key));
                FieldInfo<Object> objectFieldInfo = new FieldInfo<>(key, value, field, new ClassInfo<>(obj));
                if (!fieldTypeAdapter.shouldApply(new ProcessingContext(config, this::loadFields), objectFieldInfo)) {
                    System.out.println("matching failed");
                    continue;
                }
                if (!config.contains(key)) {
                    config.set(key, value);
                }
                Object read = fieldTypeAdapter.read(new ProcessingContext(config, this::loadFields), new FieldInfo<>(key, value, field, new ClassInfo<>(obj)));
                fieldTypeAdapter.write(new ProcessingContext(config, this::loadFields), objectFieldInfo, read);

                Annotation[] annotations = field.getAnnotations();
                INSIDE:
                for (Annotation annotation : annotations) {
                    AnnotationAdapter<? extends Annotation> adapter = annotationAdapters.get(annotation.annotationType());
                    if (adapter == null) {
                        continue INSIDE;
                    }
                    AnnotationInfo<?, Annotation> info = new AnnotationInfo<>(key, value, field, new ClassInfo<>(obj.getClass().getName(), obj, obj.getClass()), annotation);
                    boolean apply = adapter.apply(config, info);
                    if (apply) {
                        continue OUTSIDE;
                    }
                }
                continue OUTSIDE;
            }

            /*if (value == null) {
                value = type.getDeclaredConstructor().newInstance();
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

    private static Number clampNumber(Number value, Range range, Class<?> type) {

        if (type == int.class || type == Integer.class) {
            int v = value.intValue();
            return Math.min(Math.max(range.minInt(), v), range.maxInt());
        }

        if (type == long.class || type == Long.class) {
            long v = value.longValue();
            return Math.min(Math.max(range.minLong(), v), range.maxLong());
        }

        if (type == float.class || type == Float.class) {
            float v = value.floatValue();
            return Math.min(Math.max(range.minFloat(), v), range.maxFloat());
        }

        if (type == double.class || type == Double.class) {
            double v = value.doubleValue();
            return Math.min(Math.max(range.minDouble(), v), range.maxDouble());
        }

        return value;
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
    }*/
}