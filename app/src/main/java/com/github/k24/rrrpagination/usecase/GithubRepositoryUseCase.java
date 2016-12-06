package com.github.k24.rrrpagination.usecase;

import com.github.k24.rrrpagination.entity.GithubRepository;
import com.github.k24.rrrpagination.entity.PaginationResult;
import com.github.k24.rrrpagination.gateway.GithubSearchGateway;
import com.github.k24.rrrpagination.presentation.DummyContent;
import com.github.k24.rrrpagination.presentation.GithubRepositoryPresentation;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * Created by k24 on 2016/12/06.
 */

public class GithubRepositoryUseCase {
    private final GithubSearchGateway githubSearchGateway = new GithubSearchGateway();
    private BehaviorSubject<Observable<PaginationResult<List<GithubRepository>>>> searchSubject;
    private GithubRepositoryPresentation presentation;
    private Observable<PaginationResult<List<GithubRepository>>> nextPagination;

    public GithubRepositoryUseCase bind(GithubRepositoryPresentation presentation) {
        this.presentation = presentation;
        return this;
    }

    private GithubRepositoryPresentation presentation() {
        return presentation != null && presentation.isViewAvailable() ? presentation : GithubRepositoryPresentation.NULL;
    }

    public Subscription loadRepositories() {
        searchSubject = BehaviorSubject.create(githubSearchGateway.repositories("android", "language:java"));
        return searchSubject
                .flatMap(new Func1<Observable<PaginationResult<List<GithubRepository>>>, Observable<PaginationResult<List<GithubRepository>>>>() {
                    @Override
                    public Observable<PaginationResult<List<GithubRepository>>> call(Observable<PaginationResult<List<GithubRepository>>> paginationResultObservable) {
                        return paginationResultObservable;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PaginationResult<List<GithubRepository>>>() {
                    @Override
                    public void call(PaginationResult<List<GithubRepository>> listPaginationResult) {
                        List<GithubRepository> result = listPaginationResult.getResult();
                        nextPagination = listPaginationResult.getNext();

                        if (DummyContent.ITEMS.isEmpty()) {
                            if (result.isEmpty()) {
                                showEmptyView();
                            } else {
                                refreshViews(result);
                            }
                        } else {
                            addRepositories(result);
                        }
                    }
                });
    }

    public boolean nextPage() {
        Observable<PaginationResult<List<GithubRepository>>> nextPagination = this.nextPagination;
        this.nextPagination = null;
        if (searchSubject == null || nextPagination == null) return false;
        searchSubject.onNext(nextPagination);
        return true;
    }

    private void showEmptyView() {
        presentation().showEmptyView();
    }

    private void refreshViews(List<GithubRepository> repositories) {
        GithubRepositoryPresentation presentation = presentation();
        if (!presentation.isViewAvailable()) return;
        presentation.refreshViews(convertToItems(repositories));
    }

    private void addRepositories(List<GithubRepository> repositories) {
        GithubRepositoryPresentation presentation = presentation();
        if (!presentation.isViewAvailable()) return;
        presentation.addItems(convertToItems(repositories));
    }

    // Transform
    private static List<DummyContent.DummyItem> convertToItems(List<GithubRepository> repositories) {
        return Observable.just(repositories)
                .flatMapIterable(new Func1<List<GithubRepository>, Iterable<GithubRepository>>() {
                    @Override
                    public Iterable<GithubRepository> call(List<GithubRepository> repositories) {
                        return repositories;
                    }
                })
                .map(new Func1<GithubRepository, DummyContent.DummyItem>() {
                    @Override
                    public DummyContent.DummyItem call(GithubRepository githubRepository) {
                        return new DummyContent.DummyItem(String.valueOf(githubRepository.id), githubRepository.full_name, githubRepository.description);
                    }
                })
                .toList()
                .toBlocking()
                .single();
    }
}
