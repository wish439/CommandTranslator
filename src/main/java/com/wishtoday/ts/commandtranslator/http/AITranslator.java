package com.wishtoday.ts.commandtranslator.http;

public abstract class AITranslator extends AbstractTranslator {
    protected final String model;
    protected final int contextNumber;
    public AITranslator(String api, String key, String model, int contextNumber) {
        super(api, key);
        this.model = model;
        if (contextNumber <= 0) {
            this.contextNumber = 20;
            return;
        }
        this.contextNumber = contextNumber;
    }

    public AITranslator(String api, String key, String model) {
        super(api, key);
        this.model = model;
        this.contextNumber = 20;
    }

    protected abstract void limitContextList();
}
