package com.defalt.lelangonline.data.items;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.defalt.lelangonline.MainActivity;
import com.defalt.lelangonline.data.RestApi;
import com.defalt.lelangonline.ui.SharedFunctions;
import com.defalt.lelangonline.ui.items.Item;
import com.defalt.lelangonline.ui.items.ItemsAdapter;
import com.defalt.lelangonline.ui.items.ItemsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.defalt.lelangonline.ui.recycle.PaginationListener.PAGE_START;

public class ItemsSearchTask extends AsyncTask<String, Void, Void> {
    private int success;

    private ItemsAdapter adapter;
    private List<Item> itemList;
    private int currentPage;
    private int totalPage;
    private ItemsFragment.ItemsUI itemsUI;

    public ItemsSearchTask(ItemsAdapter adapter, List<Item> itemList, int currentPage, int totalPage, ItemsFragment.ItemsUI itemsUI) {
        this.adapter = adapter;
        this.itemList = itemList;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.itemsUI = itemsUI;
    }

    protected Void doInBackground(String... args) {
        RestApi server = SharedFunctions.getRetrofit().create(RestApi.class);
        RequestBody desiredCount = RequestBody.create(MediaType.parse("text/plain"), args[0]);
        RequestBody dataOffset = RequestBody.create(MediaType.parse("text/plain"), args[1]);
        RequestBody query = RequestBody.create(MediaType.parse("text/plain"), args[2]);

        Call<ResponseBody> req = server.getItemsSearch(desiredCount, dataOffset, query);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        success = json.getInt("success");

                        if (success == 1) {
                            JSONArray items = json.getJSONArray("items");

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject c = items.getJSONObject(i);
                                ItemsFragment.setItemCount(ItemsFragment.getItemCount() + 1);

                                String itemID = c.getString("itemID");
                                String itemName = c.getString("itemName");
                                String itemCat = c.getString("itemCat");
                                Double itemValue = c.getDouble("itemValue");
                                String itemImg = c.getString("itemImg");
                                int favCount = c.getInt("favCount");

                                Item im = new Item(itemID, itemName, itemCat, itemValue, itemImg, favCount);
                                itemList.add(im);
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
        adapter.addItems(itemList);
        itemsUI.setRefreshing(false);
    }

    private void executeSuccess() {
        if (itemList.size() < totalPage) {
            ItemsFragment.setLastPage(true);
        } else {
            adapter.addLoading();
        }
        itemsUI.updateUI();
    }

    private void executeEmpty() {
        ItemsFragment.setLastPage(true);
        itemsUI.updateUI();
    }

    private void executeError() {
        ItemsFragment.setLastPage(true);
        if (!MainActivity.isConnectionError()) {
            itemsUI.showError();
            MainActivity.setIsConnectionError(true);
        }
    }

    private void endExecute() {
        ItemsFragment.setLoading(false);
    }

}
