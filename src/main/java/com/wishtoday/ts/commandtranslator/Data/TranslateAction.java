package com.wishtoday.ts.commandtranslator.Data;

@FunctionalInterface
public interface TranslateAction<T> {
    T translate(T t);
}
