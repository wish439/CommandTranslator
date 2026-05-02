package com.wishtoday.ts.commandtranslator;

import lombok.experimental.Delegate;
import org.slf4j.Logger;

import java.util.concurrent.locks.ReentrantLock;

public class LoggerExtension {
    @Delegate
    private final Logger logger;
    private final ReentrantLock lock;

    public LoggerExtension(Logger logger) {
        this.logger = logger;
        this.lock = new ReentrantLock();
    }

    public void infoWithStackTrace(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.info(message, object);
            stackWalker.forEach(s -> logger.info(s.toString()));
        } finally {
            this.lock.unlock();
        }
    }

    public void debugWithStackTrace(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.debug(message, object);
            stackWalker.forEach(s -> logger.debug(s.toString()));
        } finally {
            this.lock.unlock();
        }
    }

    public void warnWithStackTrace(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.warn(message, object);
            stackWalker.forEach(s -> logger.warn(s.toString()));
        } finally {
            this.lock.unlock();
        }
    }

    public void infoWithCaller(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.info("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.info(message, object);
        } finally {
            this.lock.unlock();
        }
    }

    public void warnWithCaller(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.warn("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.warn(message, object);
        } finally {
            this.lock.unlock();
        }
    }

    public void debugWithCaller(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.debug("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.debug(message, object);
        } finally {
            this.lock.unlock();
        }
    }

    public void errorWithCaller(String message, Object... object) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.error("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.error(message, object);
        } finally {
            this.lock.unlock();
        }
    }

    public void errorWithCaller(String message, Throwable throwable) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.error("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.error(message, throwable);
        } finally {
            this.lock.unlock();
        }
    }

    public void errorWithCaller(String message, Object obj, Throwable throwable) {
        try {
            this.lock.lock();
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            this.logger.error("[{}]", stackWalker.getCallerClass().getSimpleName());
            this.logger.error(message, obj, throwable);
        } finally {
            this.lock.unlock();
        }
    }
}
