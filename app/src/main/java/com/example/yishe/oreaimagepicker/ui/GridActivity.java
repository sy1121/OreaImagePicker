package com.example.yishe.oreaimagepicker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.adapter.ImageFolderListAdapter;
import com.example.yishe.oreaimagepicker.adapter.ImageSelectAdapter;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.listener.OnCheckChangeListener;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.util.Utils;
import com.example.yishe.oreaimagepicker.widget.FolderPopupWindow;
import com.example.yishe.oreaimagepicker.widget.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GridActivity extends AppCompatActivity implements View.OnClickListener, OnCheckChangeListener {

    private static final String TAG = "GridActivity";

    @BindView(R.id.back_icon)
    ImageView back_btn;
    @BindView(R.id.title)
    TextView title_tv;
    @BindView(R.id.send_text)
    TextView send_tv;
    @BindView(R.id.image_grid)
    RecyclerView image_grid_rv;
    @BindView(R.id.empty_panel)
    RelativeLayout empty_panel;
    @BindView(R.id.bottom_panel)
    RelativeLayout bottom_panel;
    @BindView(R.id.folder_name)
    TextView folder_name_tv;
    @BindView(R.id.text_indicator)
    ImageView text_indicator_iv;
    @BindView(R.id.preview_btn)
    TextView preview_btn;

    private static final int MSG_LOAD_FINISHED = 0x01;
    private static final int PERMISSION_STORAGE_CODE = 0x10;
    private static final int PERMISSION_CAMERA_CODE  = 0x11;
    private static final int REQUEST_CODE_TAKE_PICTURE = 0x100;
    private static final int REQUEST_CODE_PREVIEW = 0x101;
    private static final int REQUEST_CODE_CROP = 0x102;
    private ImageSelectAdapter mGridAdapter;
    private ImageFolderListAdapter mAlbumAdapter;

    private FolderPopupWindow mPopupWindow;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_LOAD_FINISHED:
                    refreshData();
                    break;
            }
        }
    };

    private ImagePickModel model;
    private List<ImageItem> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_grid);
        ButterKnife.bind(this);
        initView();
        if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
            initData();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_STORAGE_CODE);
        }
        ImagePickModel.getInstance().registerCheckChangeListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ImagePickModel.getInstance().onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        ImagePickModel.getInstance().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        model = ImagePickModel.getInstance();
        Log.i(TAG,"albums = " + ImagePickModel.getInstance().getmAlbums());
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        send_tv.setOnClickListener(this);
        folder_name_tv.setOnClickListener(this);
        text_indicator_iv.setOnClickListener(this);
        preview_btn.setOnClickListener(this);

        mGridAdapter = new ImageSelectAdapter(this,null);
        image_grid_rv.setLayoutManager(new GridLayoutManager(this, 3));
        image_grid_rv.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dpToPx(2), false));
        image_grid_rv.setAdapter(mGridAdapter);

        mGridAdapter.setCameraClickListener(new ImageSelectAdapter.OnCameraClickListener() {
            @Override
            public void onCameraClick() {
                if(checkPermission(Manifest.permission.CAMERA)) {
                    ImagePickModel.getInstance().takePicture(GridActivity.this, REQUEST_CODE_TAKE_PICTURE);
                }else{
                    ActivityCompat.requestPermissions(GridActivity.this,new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA_CODE);
                }
            }
        });

        mGridAdapter.setItemClickListener(new ImageSelectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if (ImagePickModel.getInstance().isMultiMode()) {
                    //预览
                    goPreview(pos);
                }else{
                    if(ImagePickModel.getInstance().isCrop()){
                        goCrop(pos);
                    }else {
                        Intent intent = new Intent();
                        ArrayList<ImageItem> selectedImages = new ArrayList<>();
                        selectedImages.add(ImagePickModel.getInstance().getmAlbums().get(
                                ImagePickModel.getInstance().getmCurSelectedAlbumIndex()
                        ).items.get(pos));
                        intent.putParcelableArrayListExtra("images", selectedImages);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });

        mGridAdapter.setPicCheckChangeListener(new ImageSelectAdapter.OnPicCheckChangeListener() {
            @Override
            public void onChange(int pos,boolean isChecked) {
                if(!ImagePickModel.getInstance().isMultiMode()) return;
                int curAlbumIndex = ImagePickModel.getInstance().getmCurSelectedAlbumIndex();
                ImageItem curImage = ImagePickModel.getInstance().getmAlbums().get(curAlbumIndex).items.get(pos);
                ImagePickModel.getInstance().notifyCheckChanged(curImage,isChecked);
            }
        });

        if(ImagePickModel.getInstance().isPreview()){
            preview_btn.setVisibility(View.VISIBLE);
        }

    }

    private void initData(){
        ImagePickModel.getInstance().scanImage(this, new ImagePickModel.DataLoadCallback() {
            @Override
            public void onLoadFinished() {
                mHandler.sendEmptyMessage(MSG_LOAD_FINISHED);
            }
        });
    }

    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshData(){
        if(ImagePickModel.getInstance().getmAlbums().isEmpty()){
            showEmptyView();
        }else{
            image_grid_rv.setVisibility(View.VISIBLE);
            empty_panel.setVisibility(View.GONE);
            //更新Grid
            ImagePickModel model = ImagePickModel.getInstance();
            mGridAdapter.refreshData(model.getmAlbums().get(model.getmCurSelectedAlbumIndex()).items);
        }
    }

    private void showEmptyView(){
        //grid 显示无照片
        image_grid_rv.setVisibility(View.GONE);
        empty_panel.setVisibility(View.VISIBLE);

    }

    private void showToast(String tips){
        Toast.makeText(this,tips,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG,"requestCode : " + requestCode + ", permissions[0] : " + permissions[0] + " grantResults[0] : " + grantResults[0]);
        switch (requestCode){
            case PERMISSION_STORAGE_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initData();
                }else{
                    showToast("权限被禁止，无法选择本地图片");
                }
                break;
            case PERMISSION_CAMERA_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ImagePickModel.getInstance().takePicture(GridActivity.this,REQUEST_CODE_TAKE_PICTURE);
                }else{
                    showToast("权限被禁止，无法进行拍照");
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_TAKE_PICTURE:
                if(resultCode == Activity.RESULT_OK){
                    //发送广播通知图片增加了
                    ImagePickModel.galleryAddPic(this, ImagePickModel.getInstance().getTakeImageFile());
                    String path = ImagePickModel.getInstance().getTakeImageFile().getAbsolutePath();
                    ImageItem imageItem = new ImageItem();
                    imageItem.path = path;

                    Intent intent = new Intent();
                    ArrayList<ImageItem> selectedImages = new ArrayList<>();
                    selectedImages.add(imageItem);
                    intent.putParcelableArrayListExtra("images",selectedImages);
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
                break;
            case REQUEST_CODE_PREVIEW:
                Log.i(TAG,"back from preview");
                if(resultCode == Activity.RESULT_OK){{
                    sendResult();
                }}
                break;
            case REQUEST_CODE_CROP:
                Log.i(TAG,"back from crop");
                if(resultCode == Activity.RESULT_OK){
                    Intent intent = new Intent();
                    ArrayList<ImageItem> selectedImages = new ArrayList<>();
                    selectedImages.add(data.getParcelableExtra("crop_image"));
                    Log.i(TAG, "path = " + selectedImages.get(0).path);
                    intent.putParcelableArrayListExtra("images", selectedImages);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_icon:
                finish();
                break;
            case R.id.folder_name:
            case R.id.text_indicator:
                showOrHidePopWindow();
                break;
            case R.id.send_text:
                sendResult();
                break;
            case R.id.preview_btn:
                goPreview(-1);
                break;
        }
    }

    private void showOrHidePopWindow(){
        createPopupFolderList();
        if(mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }else{
            mPopupWindow.showAsDropDown(bottom_panel);
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mAlbumAdapter = new ImageFolderListAdapter(this,ImagePickModel.getInstance().getmAlbums());
        mPopupWindow = new FolderPopupWindow(this, mAlbumAdapter);
        mPopupWindow.setOnItemClickListener(new FolderPopupWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ImagePickModel.getInstance().setmCurSelectedAlbumIndex(position);
                mGridAdapter.refreshData(ImagePickModel.getInstance().getmAlbums().get(position).items);
                folder_name_tv.setText(ImagePickModel.getInstance().getmAlbums().get(position).name);
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setMargin(bottom_panel.getHeight());
    }

    @Override
    protected void onDestroy() {
        ImagePickModel.getInstance().unregisterCheckChangeListener(this);
        ImagePickModel.getInstance().release();
        super.onDestroy();
    }

    private void sendResult(){
        ArrayList<ImageItem> selectedImages = ImagePickModel.getInstance().getSelectedImages();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("images",selectedImages);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }

    private void goPreview(int pos){
        Intent intent = new Intent(GridActivity.this,PreviewActivity.class);
        if(pos > -1){
            intent.putExtra(PreviewActivity.PIC_SELECTED_ALBUM_INDEX,ImagePickModel.getInstance().getmCurSelectedAlbumIndex());
            intent.putExtra(PreviewActivity.PIC_SELECTED_INDEX_PARAM,pos);
        }
        startActivityForResult(intent,REQUEST_CODE_PREVIEW);
    }

    private void goCrop(int pos){
        Intent intent = new Intent(GridActivity.this,CropActivity.class);
        int curAblumIndex = ImagePickModel.getInstance().getmCurSelectedAlbumIndex();
        ImageItem image = ImagePickModel.getInstance().getmAlbums().get(curAblumIndex).items.get(pos);
        intent.putExtra("crop_image",image);
        startActivityForResult(intent,REQUEST_CODE_CROP);
    }



    @Override
    public void onChange() {
        //图片recycler
        ImagePickModel model = ImagePickModel.getInstance();
        mGridAdapter.refreshData(model.getmAlbums().get(model.getmCurSelectedAlbumIndex()).items);

        int selectedSize = ImagePickModel.getInstance().getSelectedImages().size();
        int maxSelectSize = ImagePickModel.getInstance().getSelectLimit();
        //右上角 --> 完成
        if(selectedSize > 0){
            send_tv.setEnabled(true);
            send_tv.setText(String.format(getResources().getString(R.string.send_enable),selectedSize+"",maxSelectSize+""));
        }else{
            send_tv.setEnabled(false);
            send_tv.setText(R.string.send_disable);
        }

        //右下角 -- > 预览
        if(selectedSize > 0){
            preview_btn.setEnabled(true);
            preview_btn.setText(String.format(getResources().getString(R.string.preview_enable),selectedSize+""));
        }else{
            preview_btn.setEnabled(false);
            preview_btn.setText(R.string.preview_disable);
        }
    }
}
