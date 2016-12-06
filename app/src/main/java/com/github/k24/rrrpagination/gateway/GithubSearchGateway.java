package com.github.k24.rrrpagination.gateway;

import android.text.TextUtils;

import com.github.k24.retrofit2.converter.jsonic.JsonicConverterFactory;
import com.github.k24.rrrpagination.entity.GithubRepository;
import com.github.k24.rrrpagination.entity.PaginationResult;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by k24 on 2016/12/06.
 */

public class GithubSearchGateway {
    private final int per_page;
    private final GithubSearchApi api;

    public GithubSearchGateway() {
        this(100); // per_page is up to 100...
    }

    public GithubSearchGateway(int per_page) {
        this.per_page = per_page;
        // Create Logging API
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        api = new Retrofit.Builder()
                .baseUrl(GithubSearchApi.URL)
                .addConverterFactory(JsonicConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .build())
                .build().create(GithubSearchApi.class);
    }

    public Observable<PaginationResult<List<GithubRepository>>> repositories(String... keywords) {
        // keywords=a+b+...
        return repositories(TextUtils.join("+", keywords), 1);
    }

    private Observable<PaginationResult<List<GithubRepository>>> repositories(final String keywords, final int page) {
        return fetchRepositories(keywords, page)
                .map(new Func1<List<GithubRepository>, PaginationResult<List<GithubRepository>>>() {
                    @Override
                    public PaginationResult<List<GithubRepository>> call(List<GithubRepository> githubRepositories) {
                        if (githubRepositories.isEmpty()) {
                            return new PaginationResult<>(githubRepositories, null, page);
                        }

                        return new PaginationResult<>(githubRepositories, repositories(keywords, page + 1), page);
                    }
                });
    }

    private Observable<List<GithubRepository>> fetchRepositories(String keywords, int page) {
        return api.repositories(keywords, page, per_page)
                .flatMap(new Func1<GithubSearchResponse<GithubRepository>, Observable<List<GithubRepository>>>() {
                    @Override
                    public Observable<List<GithubRepository>> call(GithubSearchResponse<GithubRepository> response) {
                        return Observable.just(response.items);
                    }
                });
    }
}
