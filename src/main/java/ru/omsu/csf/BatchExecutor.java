package ru.omsu.csf;

import com.google.common.collect.Lists;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
class BatchExecutor implements AutoCloseable {

    private final ExecutorService executor;
    private final int threadCount;

    BatchExecutor(int threadCount) {
        assert threadCount > 0;

        this.threadCount = threadCount;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    <T> Optional<T> call(List<T> list, Predicate<T> predicate, BinaryOperator<T> reduce) {
        if (list.isEmpty()) {
            return Optional.empty();
        }

        final var futures = Lists.partition(list, list.size() / threadCount)
                .stream()
                .map(batch -> CompletableFuture.supplyAsync(doExecute(predicate, reduce, batch), executor))
                .collect(Collectors.toList());

        return futures.stream().map(CompletableFuture::join).flatMap(Optional::stream).reduce(reduce);
    }

    private <T> Supplier<Optional<T>> doExecute(Predicate<T> predicate, BinaryOperator<T> reduce, List<T> batch) {
        return () -> batch.stream().filter(predicate).reduce(reduce);
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
