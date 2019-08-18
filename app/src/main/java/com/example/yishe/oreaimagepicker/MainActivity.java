package com.example.yishe.oreaimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.ui.GridActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_ALBNUM = 0x01;
    private Button mGoAlbum;
    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        mGoAlbum = findViewById(R.id.go_album);
        mGoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GridActivity.class);
                startActivityForResult(intent,REQUEST_CODE_ALBNUM);
            }
        });

        mImageView = findViewById(R.id.image_view);
    }

    private void setImageToIV(ImageItem item){
        Glide.with(this).load(item.path).placeholder(getDrawable(R.color.color_accent)).into(mImageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG,"onActivityResult requestCode = " + requestCode + ", resultCode = " + resultCode);
        switch (requestCode){
            case REQUEST_CODE_ALBNUM:
                if(resultCode == Activity.RESULT_OK){
                    if(data == null) return ;
                    Bundle bundle = data.getBundleExtra("bundle");
                    if(bundle == null) return ;
                    ArrayList<ImageItem> items = bundle.getParcelableArrayList("images");
                    if(!items.isEmpty()) {
                        setImageToIV(items.get(0));
                    }
                }
                break;
        }

    }
}
