package com.github.k24.rrrpagination.gateway;

import com.github.k24.rrrpagination.entity.GithubRepository;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by k24 on 2016/12/06.
 */

interface GithubSearchApi {
    String URL = "https://api.github.com/";

    @Headers({
            "Accept: application/vnd.github.v3.full+json",
            "User-Agent: Retrofit-Sample-App"
    })
    @GET("/search/repositories")
    Observable<GithubSearchResponse<GithubRepository>> repositories(@Query("q") String keywords, @Query("page") int page, @Query("per_page") int per_page);
}
