package com.github.k24.rrrpagination.presentation;

import java.util.List;

/**
 * Created by k24 on 2016/12/06.
 */
public interface GithubRepositoryPresentation {
    GithubRepositoryPresentation NULL = new GithubRepositoryPresentation() {

        @Override
        public void refreshViews(List<DummyContent.DummyItem> items) {

        }

        @Override
        public void showEmptyView() {

        }

        @Override
        public void addItems(List<DummyContent.DummyItem> items) {

        }

        @Override
        public boolean isViewAvailable() {
            return false;
        }
    };

    void refreshViews(List<DummyContent.DummyItem> items);

    void showEmptyView();

    void addItems(List<DummyContent.DummyItem> items);

    boolean isViewAvailable();
}
