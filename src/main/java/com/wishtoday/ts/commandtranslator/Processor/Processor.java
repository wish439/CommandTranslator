package com.wishtoday.ts.commandtranslator.Processor;

public interface Processor<T> {
    void tick();
    void submitTask(T task);
    default String getName() {
        return this.getClass().getSimpleName();
    }
    Class<T> getTaskClass();

}
