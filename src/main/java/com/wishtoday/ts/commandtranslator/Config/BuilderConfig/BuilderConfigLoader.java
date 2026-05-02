package com.wishtoday.ts.commandtranslator.Config.BuilderConfig;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.Attitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.AttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.PriorityAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.ConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.MutableConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.IConfigLoader;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigClassInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BuilderConfigLoader<T> implements IConfigLoader<T> {
    private final Map<Class<? extends Attitude>, AttitudeAdapter<? extends Attitude>> ATTITUDES;
    private final Map<Class<?>, List<Field>> fields = new ConcurrentHashMap<>();

    public BuilderConfigLoader(Map<Class<? extends Attitude>, AttitudeAdapter<? extends Attitude>> attitudes) {
        this.ATTITUDES = attitudes;
    }

    @Override
    public T load(Supplier<T> supplier, Path path) {
        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .autosave()
                .sync()
                .preserveInsertionOrder()
                .build();

        config.load();

        try {
            T t = supplier.get();

            this.writeFields(config, t);

            config.save();

            return t;
        } catch (Exception e) {
            Commandtranslator.setModActive(false);
            Commandtranslator.LOGGER.error("ConfigLoader#load loading failed, threw{}", e.getMessage());
            throw new UncheckedException(e);
        }
    }

    private void writeFields(CommentedFileConfig config, Object obj) throws IOException, IllegalAccessException {
        Class<?> aClass = obj.getClass();
        if (!fields.containsKey(aClass)) {
            cacheOrderedFields(aClass, ConfigEntry.class);
        }
        List<Field> list = fields.get(aClass);

        for (Field field : list) {
            field.trySetAccessible();

            Object o = field.get(obj);

            if (!(o instanceof ConfigEntry<?, ?> entry)) {
                continue;
            }

            this.writeField(entry.toMutable(), config, new WritingContext("", field, obj, obj));
        }
    }

    private void writeField(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, WritingContext context) throws IllegalAccessException {
        if (!configEntry.getChildren().isEmpty()) {
            this.writeObjectField(configEntry, config, context);
            return;
        }
        this.writeSimpleField(configEntry, config, context);
    }

    private void writeSimpleField(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, WritingContext context) {
        TreeSet<PriorityAttitude> adapters = configEntry.getAdapters();
        for (PriorityAttitude adapter : adapters) {
            AttitudeAdapter<Attitude> attitudeAdapter = (AttitudeAdapter<Attitude>) ATTITUDES.get(adapter.attitude().getClass());
            attitudeAdapter.preRead(configEntry, config, adapter.attitude());
        }
        Field field = context.field();
        String prefix = context.prefix;
        String name = getName(configEntry, field);
        name = prefix.isEmpty() ? name : prefix + "." + name;
        Object o = config.get(name);
        if (o == null) {
            o = configEntry.getDefaultValue();
        }
        for (PriorityAttitude adapter : adapters) {
            AttitudeAdapter<Attitude> attitudeAdapter = (AttitudeAdapter<Attitude>) ATTITUDES.get(adapter.attitude().getClass());
            o = attitudeAdapter.processRead(configEntry, config, adapter.attitude(), o, name);
        }
        configEntry.setValue(o);
        configEntry.applySetter(context.parentObject, o);

        if (!config.contains(name)) {
            config.set(name, o);
        }
        triggerPostWrite(configEntry, config, context, adapters, field, name, o);
    }

    private void triggerPostWrite(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, WritingContext context, TreeSet<PriorityAttitude> adapters, Field field, String name, Object o) {
        for (PriorityAttitude adapter : adapters) {
            AttitudeAdapter<Attitude> attitudeAdapter = (AttitudeAdapter<Attitude>) ATTITUDES.get(adapter.attitude().getClass());
            attitudeAdapter.postWrite(configEntry, config, adapter.attitude(), new ConfigFieldInfo<>(name, o, field, new ConfigClassInfo<>(context.configObject)));
        }
    }

    private void writeObjectField(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, WritingContext context) throws IllegalAccessException {
        TreeSet<PriorityAttitude> adapters = configEntry.getAdapters();
        Field field = context.field();
        String prefix = context.prefix;
        String name = getName(configEntry, field);
        name = prefix.isEmpty() ? name : prefix + "." + name;

        for (PriorityAttitude adapter : adapters) {
            AttitudeAdapter<Attitude> attitudeAdapter = (AttitudeAdapter<Attitude>) ATTITUDES.get(adapter.attitude().getClass());
            attitudeAdapter.preRead(configEntry, config, adapter.attitude());
        }
        List<MutableConfigEntry<?, ?>> children = configEntry.getChildren();
        if (configEntry.getValue() == null) {
            configEntry.setValue(configEntry.getDefaultValue());
        }
        triggerPostWrite(configEntry, config, context, adapters, field, name, config);
        for (MutableConfigEntry<?, ?> child : children) {
            this.writeField(child, config, context.withPrefix(name).withParentObject(configEntry.getValue()));
        }
    }

    private String getName(MutableConfigEntry<?, ?> configEntry, Field field) {
        String serializedName = configEntry.getSerializedName();
        String name;
        if (serializedName == null || serializedName.isEmpty()) {
            name = field.getName();
        } else {
            name = serializedName;
        }
        return name;
    }

    private record WritingContext(String prefix, Field field, Object parentObject, Object configObject) {
        public WritingContext withPrefix(String prefix) {
            return new WritingContext(prefix, field, parentObject, configObject);
        }
        public WritingContext withParentObject(Object parentObject) {
            return new WritingContext(prefix, field, parentObject, configObject);
        }
        public WritingContext withField(Field field) {
            return new WritingContext(prefix, field, parentObject, configObject);
        }
    }

    private void cacheOrderedFields(Class<?> aClass, @Nullable Class<?> supportFieldType) throws IOException {
        ClassReader reader = new ClassReader(Objects.requireNonNull(aClass.getResourceAsStream(
                "/" + aClass.getName().replace('.', '/') + ".class"
        )));

        Map<String, Field> fieldMap = new HashMap<>();
        List<String> names = new ArrayList<>();

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (Modifier.isStatic(access)) return super.visitField(access, name, descriptor, signature, value);
                if (supportFieldType != null) {
                    String s = Type.getDescriptor(supportFieldType);
                    if (!s.equalsIgnoreCase(descriptor))
                        return super.visitField(access, name, descriptor, signature, value);
                }
                names.add(name);
                return super.visitField(access, name, descriptor, signature, value);
            }
        };
        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (Field field : aClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (supportFieldType != null) {
                if (!supportFieldType.isAssignableFrom(field.getType()))
                    continue;
            }
            fieldMap.put(field.getName(), field);
        }
        List<Field> list = names.stream()
                .map(fieldMap::get)
                .filter(Objects::nonNull)
                .toList();
        fields.put(aClass, list);
    }
}
