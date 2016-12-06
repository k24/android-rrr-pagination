package com.github.k24.rrrpagination.gateway;

import java.util.List;

/**
 * Created by k24 on 2016/12/06.
 */

public class GithubSearchResponse<T> {
    public int total_count;
    public boolean incomplete_results;
    public List<T> items;
}
