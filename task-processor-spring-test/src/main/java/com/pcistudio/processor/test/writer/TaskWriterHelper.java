package com.pcistudio.processor.test.writer;

import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Immutable
public class TaskWriterHelper {
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final TaskWriter taskWriter;

    public CompletableFuture<Void> writeSyncData(String handlerName, int limit, Supplier<Object> supplier) {
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
