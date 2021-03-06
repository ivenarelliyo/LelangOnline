package com.defalt.lelangonline.data.auctions;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.defalt.lelangonline.data.RestApi;
import com.defalt.lelangonline.ui.SharedFunctions;
import com.defalt.lelangonline.ui.auctions.Auction;
import com.defalt.lelangonline.ui.auctions.AuctionsByUserActivity;
import com.defalt.lelangonline.ui.auctions.AuctionsByUserAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.defalt.lelangonline.ui.recycle.PaginationListener.PAGE_START;

public class AuctionsByUserTask extends AsyncTask<String, Void, Void> {
    private int success;

    private AuctionsByUserAdapter adapter;
    private List<Auction> auctionList;
    private int currentPage;
    private int totalPage;
    private AuctionsByUserActivity.AuctionsByUserUI auctionsByUserUI;

    public AuctionsByUserTask(AuctionsByUserAdapter adapter, List<Auction> auctionList, int currentPage, int totalPage, AuctionsByUserActivity.AuctionsByUserUI auctionsByUserUI) {
        this.adapter = adapter;
        this.auctionList = auctionList;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.auctionsByUserUI = auctionsByUserUI;
    }

    protected Void doInBackground(String... args) {
        RestApi server = SharedFunctions.getRetrofit().create(RestApi.class);
        RequestBody desiredCount = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(args[0]));
        RequestBody dataOffset = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(args[1]));
        RequestBody token = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(args[2]));

        Call<ResponseBody> req = server.getAuctionsByUser(desiredCount, dataOffset, token);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        success = json.getInt("success");

                        if (success == 1) {
                            Timestamp serverTime = SharedFunctions.parseDate(json.getString("serverTime"));
                            JSONArray items = json.getJSONArray("auctions");

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject c = items.getJSONObject(i);
                                AuctionsByUserActivity.setItemCount(AuctionsByUserActivity.getItemCount() + 1);

                                String auctionID = c.getString("auctionID");
                                Timestamp auctionStart = SharedFunctions.parseDate(c.getString("auctionStart"));
                                Timestamp auctionEnd = SharedFunctions.parseDate(c.getString("auctionEnd"));
                                String itemName = c.getString("itemName");
                                Double itemValue = c.getDouble("itemValue");
                                Double priceStart = c.getDouble("priceStart");
                                String itemImg = c.getString("itemImg");
                                int favCount = c.getInt("favCount");

                                Auction au = new Auction(auctionID, auctionStart, auctionEnd, itemName, itemValue, priceStart, itemImg, favCount, serverTime);
                                auctionList.add(au);
                            }
                        }

                        postExecute();
                        if (success == 1) {
                            executeSuccess();
                        } else if (success == -1) {
                            executeEmpty();
                        } else if (success == 0) {
                            executeError();
                        }
                        endExecute();
                    } else {
                        postExecute();
                        executeError();
                        endExecute();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();

                    postExecute();
                    executeError();
                    endExecute();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();

                postExecute();
                executeError();
                endExecute();
            }
        });

        return null;
    }

    private void postExecute() {
        if (currentPage != PAGE_START) {
            adapter.removeLoading();
        }
        adapter.addItems(auctionList);
        auctionsByUserUI.setRefreshing(false);
    }

    private void executeSuccess() {
        if (auctionList.size() < totalPage) {
            AuctionsByUserActivity.setLastPage(true);
        } else {
            adapter.addLoading();
        }
        auctionsByUserUI.updateUI();
    }

    private void executeEmpty() {
        AuctionsByUserActivity.setLastPage(true);
        auctionsByUserUI.updateUI();
    }

    private void executeError() {
        AuctionsByUserActivity.setLastPage(true);
        if (!AuctionsByUserActivity.isConnectionError()) {
            auctionsByUserUI.showError();
            AuctionsByUserActivity.setIsConnectionError(true);
        }
    }

    private void endExecute() {
        AuctionsByUserActivity.setLoading(false);
    }

}