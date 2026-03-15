package com.wishtoday.ts.commandtranslator.Processor;

import lombok.Getter;

@Getter
public class BatchTranslatorProcessorWrapper {
    private BatchTranslatorProcessor wrapped;
    public BatchTranslatorProcessorWrapper(BatchTranslatorProcessor wrapped) {
        this.wrapped = wrapped;
    }
}
