package com.wishtoday.ts.commandtranslator.Reader;

public interface CommandParser<R> {
    String getCommand();
    R parse();
}
