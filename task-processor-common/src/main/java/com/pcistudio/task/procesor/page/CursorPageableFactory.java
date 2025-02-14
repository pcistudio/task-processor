package com.pcistudio.task.procesor.page;


import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@SuppressWarnings("PMD.GenericsNaming")
public abstract class CursorPageableFactory<ITEM, OFFSET> {
    protected final Class<OFFSET> offsetClass;

    protected CursorPageableFactory() {
        offsetClass = (Class<OFFSET>) GenericTypeUtil.getGenericTypeFromSuperclass(getClass(), 1);
    }

    public Pageable<ITEM> createPageable(BiFunction<Cursor<OFFSET>, Integer, List<ITEM>> resultFunc, int maxSize, String token) {
        Assert.notNull(resultFunc, "resultFunc must not be null");

        List<ITEM> result = resultFunc.apply(decodeCursor(token), maxSize);

        return createPageable(result, maxSize);
    }

    /**
     * This method is expecting the result already sorted the way that should be
     * It will assume the the last element is the last element of the page
     *
     * @param result
     * @param maxSize
     * @return
     */
    public Pageable<ITEM> createPageable(List<ITEM> result, int maxSize) {
        Assert.notNull(result, "result must not be null");
        Assert.isTrue(maxSize > 0, "maxSize must be greater than 0");

        if (result.size() < maxSize) {
            return new Pageable<>(result, null);
        }

        ITEM lastElement = result.get(result.size() - 1);
        if (log.isTraceEnabled() && result.size()>1) {
            Cursor<OFFSET> cursor = createCursor(result.get(0));
            log.trace("First element offset={}, id={}", cursor.offset(), cursor.id());
            log.trace("Number of elements={}", result.size());
        }
        return new Pageable<>(result, generateToken(lastElement));
    }

    @Nullable
    public Cursor<OFFSET> decodeCursor(@Nullable String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return decode(token);
    }

    protected String generateToken(ITEM lastElement) {
        Assert.notNull(lastElement, "lastElement must not be null");
        Cursor<OFFSET> cursor = createCursor(lastElement);
        if (log.isTraceEnabled()) {
            log.trace("Last element offset={}, id={}", cursor.offset(), cursor.id());
        }
        return encode(cursor);
    }

    protected abstract String encode(Cursor<OFFSET> cursor);

    protected abstract Cursor<OFFSET> decode(String token);

    protected abstract Cursor<OFFSET> createCursor(ITEM lastElement);

}
