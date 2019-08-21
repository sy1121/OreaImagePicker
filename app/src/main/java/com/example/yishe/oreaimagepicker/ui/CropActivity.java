package com.example.yishe.oreaimagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.widget.CropImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CropActivity extends AppCompatActivity implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {
    private static final String TAG = "CropActivity";

    @BindView(R.id.back_icon)
    ImageView back_btn;
    @BindView(R.id.title)
    TextView title_tv;
    @BindView(R.id.send_text)
    TextView send_tv;
    @BindView(R.id.crop_image)
    CropImageView mCropImage;

    private ImagePickModel mModel;
    private ImageItem mSelctedImage;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);
        mModel = ImagePickModel.getInstance();
        mSelctedImage = getIntent().getParcelableExtra("crop_image");
        initView();
       // Glide.with(this).load(mSelectedImage.path).placeholder(getResources().getDrawable(R.color.c_white)).into(mCropImage);
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        title_tv.setText(R.string.crop);
        send_tv.setText(R.string.send_disable);
        send_tv.setOnClickListener(this);
        send_tv.setEnabled(true);
        mCropImage.setOnBitmapSaveCompleteListener(this);
/*        mCropImage.setmFocusedWidth(mModel.getmFocusWidth());
        mCropImage.setmFocusedHeight(mModel.getmFocusHeight());
        mCropImage.setmStyle(mModel.getmStyle());*/
        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mSelctedImage.path, options);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(mSelctedImage.path, options);
        mCropImage.setImageBitmap(mBitmap);

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
        switch(view.getId()){
            case R.id.back_icon:
                finish();
                break;
            case R.id.send_text:
                Log.i(TAG,"完成");
                mCropImage.saveBitmapToFile(mModel.getmCropCacheFolder(this),mModel.getmOutputX(),mModel.getmOutPutY(),mModel.getmIsSaveRectangle());
                break;
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
