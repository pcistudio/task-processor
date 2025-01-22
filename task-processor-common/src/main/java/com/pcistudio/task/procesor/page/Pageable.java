package com.pcistudio.task.procesor.page;


import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

public record Pageable<T>(List<T> results, @Nullable String nextPageToken) {
    public boolean finished() {
        return nextPageToken == null;
    }
    public Pageable {
        results =  List.copyOf(results);
    }
}
