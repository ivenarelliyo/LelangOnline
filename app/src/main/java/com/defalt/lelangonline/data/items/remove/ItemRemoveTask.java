package com.defalt.lelangonline.data.items.remove;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.defalt.lelangonline.R;
import com.defalt.lelangonline.data.RestApi;
import com.defalt.lelangonline.ui.SharedFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemRemoveTask extends AsyncTask<String, Void, Void> {
    private int success;
    private RemoveUI removeUI;

    public ItemRemoveTask(RemoveUI removeUI) {
        this.removeUI = removeUI;
    }

    protected Void doInBackground(String... args) {
        RestApi server = SharedFunctions.getRetrofit().create(RestApi.class);
        RequestBody itemID = RequestBody.create(MediaType.parse("text/plain"), args[0]);
        RequestBody userToken = RequestBody.create(MediaType.parse("text/plain"), args[1]);

        Call<ResponseBody> req = server.removeItem(itemID, userToken);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject json = new JSONObject(response.body().string());
                        success = json.getInt("success");

                        if (success == 1) {
                            removeUI.showSuccessDialog();
                        } else {
                            removeUI.showConnError();
                        }
                    } else {
                        removeUI.showConnError();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    removeUI.showConnError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                removeUI.showConnError();
            }
        });

        return null;
    }

    public static class RemoveUI {
        private Activity mActivity;

        public RemoveUI(Activity mActivity) {
            this.mActivity = mActivity;
        }

        void showSuccessDialog() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
            alertDialog.setTitle(R.string.alert_remove_success_title)
                    .setMessage(R.string.alert_remove_success_desc)
                    .setIcon(R.drawable.ic_check_circle_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.recreate();
                        }
                    })
                    .show();
        }

        void showConnError() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
            alertDialog.setTitle(R.string.alert_conn_title)
                    .setMessage(R.string.alert_conn_desc)
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setPositiveButton(R.string.alert_agree, null)
                    .show();
        }
    }
}
