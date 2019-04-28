package ru.omsu.csf;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;

public class BatchExecutorTest {

    private static final int THREAD_COUNT = 4;

    private BatchExecutor executor;

    @Before
    public void setUp() throws Exception {
        executor = new BatchExecutor(THREAD_COUNT);
    }

    @Test
    public void testHappyPath() {
        List<Integer> ints = IntStream.rangeClosed(-10, 10).boxed().collect(Collectors.toList());
        final var result = executor.call(ints, val -> val > 0 && (val & 1) == 0, (val1, val2) -> ++val1);

        assertThat(result, isPresentAndIs(5));
    }
}