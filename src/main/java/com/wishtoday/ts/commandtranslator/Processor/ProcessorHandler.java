package com.wishtoday.ts.commandtranslator.Processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProcessorHandler {

    private final Map<String, Processor<?>> processorMap = new HashMap<>();

    public <T> boolean registerProcessor(
            Processor<T> processor
    ) {
        if (processorMap.containsKey(processor.getName()))
            return false;
        processorMap.put(processor.getName(), processor);
        return true;
    }

    public void unregisterProcessor(
            String name
    ) {
        processorMap.remove(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Processor<T>> getProcessor(
            String name,
            Class<T> type
    ) {
        Processor<?> processor = processorMap.get(name);
        if (processor == null) return Optional.empty();
        if (!type.equals(processor.getTaskClass())) return Optional.empty();
        return Optional.of((Processor<T>) processor);
    }

    public <T extends Processor<?>> Optional<T> getProcessor(Class<T> type) {
        String simpleName = type.getSimpleName();
        Processor<?> processor = processorMap.get(simpleName);
        if (processor == null) return Optional.empty();
        if (!type.isInstance(processor)) return Optional.empty();
        return Optional.of(type.cast(processor));
    }


    public void tick() {
        for (Processor<?> processor : processorMap.values()) {
            processor.tick();
        }
    }
}
