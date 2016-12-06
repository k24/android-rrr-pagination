package com.github.k24.rrrpagination;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.k24.rrrpagination.presentation.DummyContent;
import com.github.k24.rrrpagination.presentation.GithubRepositoryPresentation;
import com.github.k24.rrrpagination.usecase.GithubRepositoryUseCase;

import java.util.List;

import rx.Subscription;

/**
 * An activity representing a list of GithubRepositories. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link GithubRepositoryDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class GithubRepositoryListActivity extends AppCompatActivity implements GithubRepositoryPresentation {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private GithubRepositoryUseCase githubRepositoryUseCase;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_githubrepository_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View recyclerView = findViewById(R.id.githubrepository_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.githubrepository_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        githubRepositoryUseCase = new GithubRepositoryUseCase().bind(this);
        subscription = githubRepositoryUseCase.loadRepositories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        final int limit = 20;
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = getLastVisibleItemPosition(recyclerView);
                int updatePosition = recyclerView.getAdapter().getItemCount() - 1 - (limit / 2);
                if (position >= updatePosition) {
                    githubRepositoryUseCase.nextPage();
                }
            }
        });
    }

    private static int getLastVisibleItemPosition(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        return layoutManager.findLastVisibleItemPosition();
    }

    @Override
    public void refreshViews(List<DummyContent.DummyItem> items) {
        DummyContent.clear();
        DummyContent.addItems(items);
        setupRecyclerView((RecyclerView) findViewById(R.id.githubrepository_list));
    }

    @Override
    public void showEmptyView() {
        Toast.makeText(this, "No Content", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addItems(List<DummyContent.DummyItem> items) {
        int size = DummyContent.ITEMS.size();
        DummyContent.addItems(items);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.githubrepository_list);
        recyclerView.getAdapter().notifyItemRangeInserted(size, items.size());
    }

    @Override
    public boolean isViewAvailable() {
        return !isFinishing() && !isDestroyed();
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.githubrepository_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(GithubRepositoryDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        GithubRepositoryDetailFragment fragment = new GithubRepositoryDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.githubrepository_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, GithubRepositoryDetailActivity.class);
                        intent.putExtra(GithubRepositoryDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
