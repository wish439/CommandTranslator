package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Services.ConfigValue;

public record SimpleService(String lang, int test) {
    public SimpleService(@ConfigValue("language") String lang
            , @ConfigValue("test") int test) {
        this.lang = lang;
        this.test = test;
    }
}
