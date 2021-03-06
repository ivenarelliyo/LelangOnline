package com.defalt.lelangonline.ui.items.edit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation;
import com.bumptech.glide.Glide;
import com.defalt.lelangonline.R;
import com.defalt.lelangonline.data.items.edit.ItemByIDTask;
import com.defalt.lelangonline.data.items.edit.ItemsEditTask;
import com.defalt.lelangonline.data.login.LoginRepository;
import com.defalt.lelangonline.ui.SharedFunctions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import me.abhinay.input.CurrencyEditText;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ItemsEditActivity extends AppCompatActivity {
    private Activity mActivity;
    private Context mContext;

    private ShimmerFrameLayout mShimmerViewContainer;
    private ScrollView mScrollView;
    private ItemsEditUI itemsEditUI;

    private EditText nameEditText;
    private EditText descEditText;
    private EditText categoryEditText;
    private CurrencyEditText valueEditText;
    private ImageView thumbImageView;
    private View overlay;
    private CardView progressBarCard;
    private AwesomeValidation mAwesomeValidation;

    private static boolean isLoading = true;
    private static boolean isImageEmpty = true;
    private Uri mCropImageUri;
    private boolean isImageChange;
    private String itemID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.startShimmer();

        mScrollView = findViewById(R.id.container);

        mActivity = ItemsEditActivity.this;
        mContext = getApplicationContext();
        itemsEditUI = new ItemsEditUI();

        Intent intent = getIntent();
        itemID = intent.getStringExtra("TAG_EXTRA");

        nameEditText = findViewById(R.id.name);
        categoryEditText = findViewById(R.id.category);
        valueEditText = SharedFunctions.setEditTextCurrency((CurrencyEditText) findViewById(R.id.value));
        descEditText = findViewById(R.id.description);
        thumbImageView = findViewById(R.id.thumbnail);
        overlay = findViewById(R.id.overlay);
        progressBarCard = findViewById(R.id.progressCard);

        SimpleCustomValidation validationEmpty = new SimpleCustomValidation() {
            @Override
            public boolean compare(String input) {
                return input.length() > 0;
            }
        };

        mAwesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.addValidation(mActivity, R.id.name, validationEmpty, R.string.form_invalid_item_name);
        mAwesomeValidation.addValidation(mActivity, R.id.category, validationEmpty, R.string.form_invalid_item_cat);
        mAwesomeValidation.addValidation(mActivity, R.id.value, validationEmpty, R.string.form_invalid_item_val);
        mAwesomeValidation.addValidation(mActivity, R.id.description, validationEmpty, R.string.form_invalid_item_desc);

        new ItemByIDTask(itemsEditUI).execute(itemID);
    }

    private void enableImageAdd() {
        thumbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CropImage.isExplicitCameraPermissionRequired(mContext)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                    }
                } else {
                    CropImage.startPickImageActivity(mActivity);
                }
            }
        });
    }

    private void enableImageRemove() {
        thumbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Hapus"};
                final AlertDialog.Builder imgDialog = new AlertDialog.Builder(mActivity);
                imgDialog.setTitle(R.string.item_post_img_hint);
                imgDialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Hapus")) {
                            thumbImageView.setImageResource(R.drawable.placeholder_image);
                            isImageChange = true;
                            setIsImageEmpty(true);
                            mCropImageUri = null;
                            enableImageAdd();
                        }
                    }
                });
                imgDialog.show();
            }
        });
    }

    private void disableImage() {
        thumbImageView.setOnClickListener(null);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setFixAspectRatio(true)
                .setAspectRatio(16, 9)
                .setRequestedSize(800, 450, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case (CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE): {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CropImage.startPickImageActivity(this);
                } else {
                    Toast.makeText(this, "Batal, izin kamera tidak diberikan", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case (CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE): {
                if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCropImageActivity(mCropImageUri);
                } else {
                    Toast.makeText(this, "Batal, izin penyimpanan tidak diberikan", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE): {
                    Uri imageUri = CropImage.getPickImageResultUri(this, data);

                    if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                        mCropImageUri = imageUri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                        }
                    } else {
                        startCropImageActivity(imageUri);
                    }
                    break;
                }
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE): {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mCropImageUri = result.getUri();
                    Glide.with(mActivity).load(mCropImageUri).into(thumbImageView);
                    enableImageRemove();
                    isImageChange = true;
                    setIsImageEmpty(false);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (!isLoading()) {
                if (mAwesomeValidation.validate()) {
                    uploadToServer();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadToServer() {
        setIsLoading(true);
        overlay.setVisibility(View.VISIBLE);
        progressBarCard.setVisibility(View.VISIBLE);

        SharedFunctions.disableEditText(nameEditText);
        SharedFunctions.disableEditText(descEditText);
        SharedFunctions.disableEditText(categoryEditText);
        SharedFunctions.disableEditText(valueEditText);
        disableImage();

        RequestBody itemID = RequestBody.create(MediaType.parse("text/plain"), this.itemID);
        RequestBody itemName = RequestBody.create(MediaType.parse("text/plain"), nameEditText.getText().toString());
        RequestBody itemDesc = RequestBody.create(MediaType.parse("text/plain"), descEditText.getText().toString());
        RequestBody itemCat = RequestBody.create(MediaType.parse("text/plain"), categoryEditText.getText().toString());
        RequestBody itemVal = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(valueEditText.getCleanIntValue()));
        RequestBody isImageEmpty = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isImageEmpty()));
        RequestBody isImageChange = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(this.isImageChange));
        RequestBody userToken = RequestBody.create(MediaType.parse("text/plain"), LoginRepository.getLoggedInUser().getToken());

        new ItemsEditTask(isImageEmpty(), mCropImageUri, this.isImageChange, itemsEditUI).execute(itemID, itemName, itemDesc, itemCat, itemVal, isImageEmpty, isImageChange, userToken);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public static boolean isLoading() {
        return isLoading;
    }

    public static void setIsLoading(boolean isLoading) {
        ItemsEditActivity.isLoading = isLoading;
    }

    public static boolean isImageEmpty() {
        return isImageEmpty;
    }

    public static void setIsImageEmpty(boolean isImageEmpty) {
        ItemsEditActivity.isImageEmpty = isImageEmpty;
    }

    public class ItemsEditUI {
        ItemsEditUI() { }

        public void updateEditText(String itemName, String itemCat, Double itemValue, String itemDesc, String itemImg) {
            nameEditText.setText(itemName);
            categoryEditText.setText(itemCat);
            valueEditText.setText(SharedFunctions.formatRupiah(itemValue));
            descEditText.setText(itemDesc);
            thumbImageView.setImageDrawable(null);
            if (!itemImg.equals("null")) {
                String IMAGE_URL = "https://dev.projectlab.co.id/mit/1317003/images/items/";
                Glide.with(mContext).load(IMAGE_URL + itemImg).into(thumbImageView);
                setIsImageEmpty(false);
                enableImageRemove();
            } else {
                Glide.with(mContext).load(R.drawable.placeholder_image).into(thumbImageView);
                setIsImageEmpty(true);
                enableImageAdd();
            }
            updateUI();
        }

        public void updateUI() {
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            setIsLoading(false);
        }

        public void showConnError() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
            alertDialog.setTitle(R.string.alert_conn_title)
                    .setMessage(R.string.alert_conn_desc)
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton(mActivity.getString(R.string.alert_agree), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    })
                    .show();
        }

        public void updateUIAfterUpload(int endType) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
            if (endType == 0) {
                alertDialog.setMessage(R.string.alert_update_success_item_0_desc);
            } else {
                alertDialog.setMessage(R.string.alert_update_success_item_1_desc);
            }
            alertDialog.setTitle(R.string.alert_update_success_title)
                    .setIcon(R.drawable.ic_check_circle_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton(mActivity.getString(R.string.alert_agree), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    })
                    .show();
        }

        public void showConnErrorThenRetry() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
            alertDialog.setTitle(R.string.alert_conn_title)
                    .setMessage(R.string.alert_conn_desc)
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton(mActivity.getString(R.string.alert_agree), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedFunctions.enableEditText(nameEditText);
                            SharedFunctions.enableEditText(descEditText);
                            SharedFunctions.enableEditText(categoryEditText);
                            SharedFunctions.enableEditText(valueEditText);
                            if (isImageEmpty()) {
                                enableImageAdd();
                            } else {
                                enableImageRemove();
                            }

                            overlay.setVisibility(View.GONE);
                            progressBarCard.setVisibility(View.GONE);
                            setIsLoading(false);
                        }
                    }).show();
        }
    }
}
