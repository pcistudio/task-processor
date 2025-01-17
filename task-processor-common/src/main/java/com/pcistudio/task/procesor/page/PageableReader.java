package com.pcistudio.task.procesor.page;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public class PageableReader<T> implements Iterable<T> {
    private final Function<String, Pageable<T>> reader;
    private static final int MAX_PAGE_SIZE = 100_000;

    public List<T> readAll() {
        List<T> results = new ArrayList<>();
        Pageable<T> pageable = reader.apply(null);
        results.addAll(pageable.results());
        int page = 1;
        while (!pageable.finished()) {

            pageable = reader.apply(pageable.nextPageToken());
            results.addAll(pageable.results());
            page+=1;
            if (log.isDebugEnabled()) {
                log.debug("found {} items in page={}", pageable.results().size(), page);
            }
            if (results.size() > MAX_PAGE_SIZE) {
                if (log.isWarnEnabled()) {
                    log.warn("page size is too big: {}", pageable.results().size());
                }
                return results;
            }
        }
        if (log.isInfoEnabled()) {
            log.info("read: {} items", results.size());
        }
        return results;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Pageable<T> pageable = reader.apply(null);
            private Iterator<T> iterator = pageable.results().iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) {
                    return true;
                }
                if (pageable.finished()) {
                    return false;
                }
                pageable = reader.apply(pageable.nextPageToken());
                iterator = pageable.results().iterator();
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
