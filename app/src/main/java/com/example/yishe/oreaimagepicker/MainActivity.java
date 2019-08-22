package com.example.yishe.oreaimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.adapter.SelectedImageAdapter;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.loader.ImageLoader;
import com.example.yishe.oreaimagepicker.widget.CropImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_ALBNUM = 0x01;
    private Button mGoAlbum;
    private ImageView mImageView;
    private RecyclerView mSelectedImagesGrid;

    @BindView(R.id.loader_radio_group)
    RadioGroup loader_group;
    @BindView(R.id.loader_glide)
    RadioButton loader_glide;
    @BindView(R.id.loader_fresco)
    RadioButton loader_fresco;
    @BindView(R.id.loader_picass0)
    RadioButton loader_picasso;
    @BindView(R.id.select_mode_radio_group)
    RadioGroup select_mode_group;
    @BindView(R.id.select_mode_single)
    RadioButton single_mode;
    @BindView(R.id.select_mode_multi)
    RadioButton multi_mode;
    @BindView(R.id.select_limit_seekbar)
    SeekBar select_limit;
    @BindView(R.id.select_max_count)
    TextView max_select_tv;
    @BindView(R.id.do_show_camera)
    CheckBox show_camera;
    @BindView(R.id.do_crop)
    CheckBox crop;
    @BindView(R.id.crop_as_rectangle)
    CheckBox crop_as_rectangle;
    @BindView(R.id.crop_shape_rectangle)
    RadioButton shape_rectangle;
    @BindView(R.id.crop_width_et)
    EditText crop_width;
    @BindView(R.id.crop_height_et)
    EditText crop_height;
    @BindView(R.id.crop_shape_circle)
    RadioButton shape_circle;
    @BindView(R.id.crop_radius)
    EditText crop_radius;
    @BindView(R.id.crop_save_width)
    EditText save_width;
    @BindView(R.id.crop_save_height)
    EditText save_height;


    private ImageLoader imageLoader;
    private boolean isMultiMode;
    private int selectLimit;
    private boolean isShowCamera;
    private boolean isCrop;
    private boolean isSaveAsRectangle;
    private int focusWidth;
    private int focusHeight;
    private int focusRadius;
    private int saveWidth;
    private int saveHeight;
    private CropImageView.Style style;


    private SelectedImageAdapter selectedImageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView(){
        if(loader_glide.isChecked()){
            imageLoader = new GlideImageLoader();
        }
        if(multi_mode.isChecked()){
            isMultiMode = true;
        }
        multi_mode.setOnCheckedChangeListener(this);
        if(shape_rectangle.isChecked()){
            style = CropImageView.Style.RECTANGLE;
        }else{
            style = CropImageView.Style.CIRCLE;
        }
        shape_rectangle.setOnCheckedChangeListener(this);
        selectLimit = Integer.parseInt(max_select_tv.getText().toString());
        isShowCamera = show_camera.isChecked();
        show_camera.setOnCheckedChangeListener(this);
        isCrop = crop.isChecked();
        crop.setOnCheckedChangeListener(this);
        isSaveAsRectangle = crop_as_rectangle.isChecked();
        crop_as_rectangle.setOnCheckedChangeListener(this);
        select_limit.setOnSeekBarChangeListener(this);

        mGoAlbum = findViewById(R.id.go_album);
        mGoAlbum.setOnClickListener(this);

        mImageView = findViewById(R.id.image_view);

        selectedImageAdapter = new SelectedImageAdapter(this,null);
        mSelectedImagesGrid = findViewById(R.id.selected_images_grid);
        mSelectedImagesGrid.setLayoutManager(new GridLayoutManager(this,3));
        mSelectedImagesGrid.setAdapter(selectedImageAdapter);
    }

    private void setImageToIV(ImageItem item){
        Log.i(TAG,"path = " + item.path);
        Glide.with(this).load(item.path).placeholder(getDrawable(R.color.color_accent)).into(mImageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_ALBNUM:
                if(resultCode == Activity.RESULT_OK){
                    if(data == null) return ;
                    ArrayList<ImageItem> items = data.getParcelableArrayListExtra("images");
                    if(!items.isEmpty()) {
                        setImageToIV(items.get(0));
                        selectedImageAdapter.refreshData(items);
                    }
                }
                break;
        }

    }


    private void goAlbum(){
        Orea.from(MainActivity.this).select()
                .setImageLoader(imageLoader)
                .setMultiMode(isMultiMode)
                .setPreview(!isMultiMode)
                .setSelectLimit(selectLimit)
                .setShowCamera(isShowCamera)
                .setCrop(isCrop)
                .setmIsSaveRectangle(isSaveAsRectangle)
                .setmStyle(style)
                .setmFocusHeight(focusHeight)
                .setmFocusWidth(focusWidth)
                .setmOutputX(saveWidth)
                .setmOutPutY(saveHeight)
                .forResult(REQUEST_CODE_ALBNUM);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.go_album:
                focusWidth = Integer.parseInt(crop_width.getText().toString());
                focusHeight = Integer.parseInt(crop_height.getText().toString());
                focusRadius = Integer.parseInt(crop_radius.getText().toString());
                saveWidth = Integer.parseInt(save_width.getText().toString());
                saveHeight = Integer.parseInt(save_height.getText().toString());
                selectLimit = Integer.parseInt(max_select_tv.getText().toString());
                goAlbum();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        max_select_tv.setText(progress +"");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.do_show_camera:
                isShowCamera = isChecked;
                break;
            case R.id.do_crop:
                isCrop = isChecked;
                break;
            case R.id.crop_as_rectangle:
                isSaveAsRectangle = isChecked;
                break;
            case R.id.select_mode_multi:
                isMultiMode = isChecked;
                Log.i(TAG,"isMulti = " + isMultiMode);
                break;
            case R.id.crop_shape_rectangle:
                if(isChecked) {
                    style = CropImageView.Style.RECTANGLE;
                }else{
                    style = CropImageView.Style.CIRCLE;
                }
                break;
        }
    }
}
