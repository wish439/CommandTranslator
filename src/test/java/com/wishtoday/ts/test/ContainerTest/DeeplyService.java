package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Services.ConfigValue;

public record DeeplyService(SimpleService simpleService, double trial) {
    public DeeplyService(SimpleService simpleService, @ConfigValue("testClass.c") double trial) {
        this.simpleService = simpleService;
        this.trial = trial;
    }
}
