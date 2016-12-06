package com.github.k24.rrrpagination.gateway;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.github.k24.retrofit2.converter.jsonic.JsonicConverterFactory;
import com.github.k24.rrrpagination.entity.GithubRepository;
import com.github.k24.rrrpagination.entity.GithubRepositoryFields;
import com.github.k24.rrrpagination.entity.PaginationResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by k24 on 2016/12/06.
 */

public class GithubSearchGateway {
    private final int per_page;
    private final GithubSearchApi api;
    private final boolean useCache;
    private final Realm realm = Realm.getDefaultInstance();

    public GithubSearchGateway() {
        this(100, true); // per_page is up to 100...
    }

    public GithubSearchGateway(int per_page, boolean useCache) {
        this.per_page = per_page;
        this.useCache = useCache;
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
        return loadRepositories(keywords, page)
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

    private Observable<List<GithubRepository>> loadRepositories(final String keywords, final int page) {
        if (!useCache) return fetchRepositories(keywords, page);

        Date expiry = new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
        return realm.where(GithubRepository.class)
                .equalTo(GithubRepositoryFields.QUERY_PARAMETERS, GithubRepository.queryParameters(keywords, page))
                .greaterThan(GithubRepositoryFields.FETCHED_AT, expiry)
                .findAllAsync()
                .asObservable()
                .filter(new Func1<RealmResults<GithubRepository>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<GithubRepository> githubRepositories) {
                        return githubRepositories.isLoaded();
                    }
                })
                .flatMap(new Func1<RealmResults<GithubRepository>, Observable<List<GithubRepository>>>() {
                    @Override
                    public Observable<List<GithubRepository>> call(RealmResults<GithubRepository> githubRepositories) {
                        if (githubRepositories.isEmpty()) {
                            return fetchRepositories(keywords, page);
                        }

                        return Observable.from(githubRepositories)
                                .toList();
                    }
                });
    }

    private Observable<List<GithubRepository>> fetchRepositories(final String keywords, final int page) {
        if (!useCache) {
            return api.repositories(keywords, page, per_page)
                    .flatMap(new Func1<GithubSearchResponse<GithubRepository>, Observable<List<GithubRepository>>>() {
                        @Override
                        public Observable<List<GithubRepository>> call(GithubSearchResponse<GithubRepository> response) {
                            return Observable.just(response.items);
                        }
                    });
        }

        return api.repositories(keywords, page, per_page)
                .map(new Func1<GithubSearchResponse<GithubRepository>, List<GithubRepository>>() {
                    @Override
                    public List<GithubRepository> call(GithubSearchResponse<GithubRepository> response) {
                        final Date now = new Date();
                        final String queryParameters = GithubRepository.queryParameters(keywords, page);

                        final Realm realm = Realm.getDefaultInstance();
                        final List<GithubRepository> reposList = new ArrayList<>(response.items.size());
                        realm.beginTransaction();
                        try {
                            for (GithubRepository item : response.items) {
                                item.queryParameters = queryParameters;
                                item.fetchedAt = now;
                                realm.insertOrUpdate(item);
                                reposList.add(item);
                            }
                            realm.commitTransaction();
                        } catch (Exception e) {
                            realm.cancelTransaction();
                            Timber.e(e, "not put to cache");
                        } finally {
                            realm.close();
                        }
                        return reposList;
                    }
                });
    }
}
