package com.pcistudio.task.procesor.page;


import java.util.List;

public record Pageable<T>(List<T> results, String nextPageToken) {
    public boolean finished() {
        return nextPageToken == null;
    }
}
