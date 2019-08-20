package com.example.yishe.oreaimagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.widget.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CropActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "CropActivity";

    @BindView(R.id.back_icon)
    ImageView back_btn;
    @BindView(R.id.title)
    TextView title_tv;
    @BindView(R.id.send_text)
    TextView send_tv;
    @BindView(R.id.crop_image)
    CropImageView mCropImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);
        ImageItem selectedImage = getIntent().getParcelableExtra("crop_image");
        Glide.with(this).load(selectedImage.path).placeholder(getResources().getDrawable(R.color.c_white)).into(mCropImage);
        initView();
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        title_tv.setText(R.string.crop);
        send_tv.setText(R.string.send_disable);
        send_tv.setOnClickListener(this);
        send_tv.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.back_icon:
                finish();
                break;
            case R.id.send_text:
                Log.i(TAG,"完成");
                mCropImage.saveCropViewToBitmap();
                sendResult();
                break;
        }
    }

    private void sendResult(){
        Intent intent = new Intent();
        ImageItem imageItem = new ImageItem();
        imageItem.path = mCropImage.getCropImagePath();
        intent.putExtra("crop_image",imageItem);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }


}
