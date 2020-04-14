package com.defalt.lelangonline.ui.auctions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.defalt.lelangonline.R;
import com.defalt.lelangonline.data.auctions.AuctionsTask;
import com.defalt.lelangonline.ui.recycle.PaginationListener;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import static com.defalt.lelangonline.ui.recycle.PaginationListener.PAGE_START;

public class AuctionsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private AuctionsAdapter adapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private AuctionsUI auctionsUI;
    private String itemID;
    private int totalPage = 4;
    private int currentPage = PAGE_START;
    private static boolean isLastPage = false;
    private static boolean isLoading = false;
    private static int itemCount = 0;
    private static boolean isConnectionError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auctions_by_item);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("TAG_EXTRA");

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.startShimmer();

        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new AuctionsAdapter(this, new ArrayList<Auction>());
        mRecyclerView.setAdapter(adapter);

        auctionsUI = new AuctionsUI(mShimmerViewContainer, mRecyclerView, swipeRefresh, this);

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager, totalPage) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                prepareData();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    @Override
    public void onRefresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();

        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        AuctionsActivity.setIsConnectionError(false);
        prepareData();
    }

    @Override
    public void onStop() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();

        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        AuctionsActivity.setIsConnectionError(false);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareData();
    }

    public static void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    public static void setLoading(boolean loading) {
        isLoading = loading;
    }

    public static int getItemCount() {
        return itemCount;
    }

    public static void setItemCount(int newItemCount) {
        itemCount = newItemCount;
    }

    private void prepareData() {
        new AuctionsTask(adapter, new ArrayList<Auction>(), currentPage, totalPage, auctionsUI)
                .execute(String.valueOf(totalPage), String.valueOf(itemCount), itemID);
    }

    public static boolean isConnectionError() {
        return isConnectionError;
    }

    public static void setIsConnectionError(boolean isConnectionError) {
        AuctionsActivity.isConnectionError = isConnectionError;
    }

    public static class AuctionsUI {
        private final ShimmerFrameLayout mShimmerViewContainer;
        private final RecyclerView mRecyclerView;
        private final SwipeRefreshLayout swipeRefresh;
        private final Context CONTEXT;

        AuctionsUI(ShimmerFrameLayout mShimmerViewContainer, RecyclerView mRecyclerView, SwipeRefreshLayout swipeRefresh, Context context) {
            this.mShimmerViewContainer = mShimmerViewContainer;
            this.swipeRefresh = swipeRefresh;
            this.mRecyclerView = mRecyclerView;
            this.CONTEXT = context;
        }

        public void setRefreshing(boolean refreshingState) {
            swipeRefresh.setRefreshing(refreshingState);
        }

        public void updateUI() {
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        public void showError() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CONTEXT);
            alertDialog.setTitle(R.string.alert_conn_title)
                    .setMessage(R.string.alert_conn_desc)
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setPositiveButton(CONTEXT.getString(R.string.alert_agree), null)
                    .show();
        }
    }
}
