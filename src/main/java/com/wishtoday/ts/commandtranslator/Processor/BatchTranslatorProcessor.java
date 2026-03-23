package com.wishtoday.ts.commandtranslator.Processor;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.http.IBatchTranslator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BatchTranslatorProcessor implements FunctionProcessor<String, CompletableFuture<String>>{
    private final int batchSize;
    private final long timeout;
    private long currentTime;

    private final IBatchTranslator translator;

    private final Queue<Task> queue;
    private final Map<String, CompletableFuture<String>> map;

    private final ExecutorService worker;

    private final AtomicBoolean processing = new AtomicBoolean(false);

    public BatchTranslatorProcessor(int batchSize, long timeout, IBatchTranslator translator) {
        this.batchSize = batchSize;
        this.timeout = timeout;
        this.queue = new ConcurrentLinkedQueue<>();
        this.currentTime = -1;
        this.map = new ConcurrentHashMap<>();
        this.translator = translator;
        this.worker = Executors.newSingleThreadExecutor();
    }

    @Override
    public CompletableFuture<String> submit(String task) {
        Commandtranslator.LOGGER.info("receive a translate task:{}", task);
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<String> existing = map.putIfAbsent(task, future);
        if (existing != null) {
            return existing;
        }
        if (queue.isEmpty()) {
            this.currentTime = System.currentTimeMillis();
        }
        queue.add(new Task(task, future));
        map.put(task, future);
        return future;
    }

    @Override
    public void tick() {
        if (queue.isEmpty()) {
            return;
        }
        if (queue.size() >= batchSize) {
            processQueue();
        }
        if (currentTime < 0) return;
        if (System.currentTimeMillis() - currentTime > timeout) {
            processQueue();
        }
    }

    private void processQueue() {
        if (!processing.compareAndSet(false, true)) {
            return;
        }
        worker.submit(() -> {
            try {
                flush();
            } finally {
                processing.set(false);
            }
        });
    }

    private void flush() {
        List<Task> tasks = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            Task task = queue.poll();
            if (task == null) {
                continue;
            }
            tasks.add(task);
        }
        if (tasks.isEmpty()) return;
        List<String> list = tasks.stream().map(Task::getString).toList();

        List<String> translated = translator.translate(list);
        Commandtranslator.LOGGER.info("receive the translate:{}", translated);
        for (int i = 0; i < translated.size(); i++) {
            Task task = tasks.get(i);
            String s = translated.get(i);
            task.future.complete(s);
            map.remove(task.string);
        }
        this.currentTime = -1;
        Commandtranslator.LOGGER.info("This translator has been processed");
    }

    @Override
    public Class<String> getTaskClass() {
        return String.class;
    }

    @AllArgsConstructor
    private static class Task {
        @Getter
        String string;
        CompletableFuture<String> future;
    }
}
