package com.github.k24.rrrpagination.entity;

import rx.Observable;

/**
 * Created by k24 on 2016/12/06.
 */

public class PaginationResult<T> {
    private final T result;
    private final Observable<PaginationResult<T>> next;
    private final int page;

    public PaginationResult(T result, Observable<PaginationResult<T>> next, int page) {
        this.result = result;
        this.next = next;
        this.page = page;
    }

    public T getResult() {
        return result;
    }

    public Observable<PaginationResult<T>> getNext() {
        return next;
    }
}
