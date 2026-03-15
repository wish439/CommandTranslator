package com.wishtoday.ts.commandtranslator.Processor;

public interface FunctionProcessor<T, R> extends Processor<T> {
    @Override
    default void submitTask(T task) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    R submit(T task);
}
