package com.pcistudio.processor.test.writer;

import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class TaskWriterHelper {
    private final TaskWriter taskWriter;

    public CompletableFuture<Void> writeSyncData(String handlerName, int limit, Supplier<Object> supplier) throws ExecutionException, InterruptedException {
        return CompletableFuture.runAsync(() -> writeData(handlerName, limit, supplier));
    }

    private void writeData(String handlerName, int limit, Supplier<Object> supplier) {
        Stream.iterate(1, integer -> integer + 1)
                .limit(limit)
                .forEach(integer -> {
                    taskWriter.writeTasks(TaskParams.builder()
                            .handlerName(handlerName)
                            .payload(supplier.get())
                            .build());
                });
        log.info("\n-------------------------------------------------");
    }
}
