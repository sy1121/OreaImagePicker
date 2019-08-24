package com.example.yishe.oreaimagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.util.Utils;
import com.example.yishe.oreaimagepicker.widget.CropImageView;

import java.io.File;


public class CropActivity extends BaseActivity implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {
    private static final String TAG = "CropActivity";
    ImageView back_btn;
    TextView title_tv;
    TextView send_tv;
    CropImageView mCropImage;
    RelativeLayout mHeader;

    private ImagePickModel mModel;
    private ImageItem mSelctedImage;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crop);
        mModel = ImagePickModel.getInstance();
        mSelctedImage = getIntent().getParcelableExtra("crop_image");
        initView();
    }

    private void initView(){
        back_btn = findViewById(R.id.back_icon);
        title_tv  = findViewById(R.id.title);
        send_tv = findViewById(R.id.send_text);
        mCropImage = findViewById(R.id.crop_image);
        mHeader = findViewById(R.id.crop_image_head);

        back_btn.setOnClickListener(this);
        title_tv.setText(R.string.crop);
        send_tv.setText(R.string.send_disable);
        send_tv.setOnClickListener(this);
        send_tv.setEnabled(true);
        mCropImage.setOnBitmapSaveCompleteListener(this);
        mCropImage.setmFocusedWidth(mModel.getmFocusWidth());
        mCropImage.setmFocusedHeight(mModel.getmFocusHeight());
        mCropImage.setmStyle(mModel.getmStyle());
        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mSelctedImage.path, options);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(mSelctedImage.path, options);
        mCropImage.setImageBitmap(mBitmap);

        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHeader.getLayoutParams();
            params.topMargin = Utils.getStatusHeight(this);
            mHeader.setLayoutParams(params);
        }

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.back_icon){
            finish();
        }else if(id == R.id.send_text){
            Log.i(TAG,"完成");
            mCropImage.saveBitmapToFile(mModel.getmCropCacheFolder(this),mModel.getmOutputX(),mModel.getmOutPutY(),mModel.getmIsSaveRectangle());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropImage.setOnBitmapSaveCompleteListener(null);
        if(mBitmap != null){
            mBitmap.recycle();
        }
    }

    private void sendResult(File file){
        Intent intent = new Intent();
        ImageItem imageItem = new ImageItem();
        imageItem.path = file.getAbsolutePath();
        intent.putExtra("crop_image",imageItem);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }


    @Override
    public void onBitmapSaveSucess(File file) {
        sendResult(file);
        Log.i(TAG,"保存图片成功");
    }

    @Override
    public void onBitmapSaveError(File file) {
        Log.i(TAG,"保存图片失败");
    }
}
