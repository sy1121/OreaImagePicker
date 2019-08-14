package com.example.yishe.oreaimagepicker.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.adapter.ImageFolderListAdapter;
import com.example.yishe.oreaimagepicker.adapter.ImageSelectAdapter;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.util.Utils;
import com.example.yishe.oreaimagepicker.widget.FolderPopupWindow;
import com.example.yishe.oreaimagepicker.widget.GridSpacingItemDecoration;

import java.security.Permission;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GridActivity extends AppCompatActivity implements View.OnClickListener{

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
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        send_tv.setOnClickListener(this);
        folder_name_tv.setOnClickListener(this);
        text_indicator_iv.setOnClickListener(this);
        preview_btn.setOnClickListener(this);

        mGridAdapter = new ImageSelectAdapter(this,null);
        image_grid_rv.setLayoutManager(new GridLayoutManager(this, 3));
        image_grid_rv.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dpToPx(2), true));
        image_grid_rv.setAdapter(mGridAdapter);

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
        switch (requestCode){
            case PERMISSION_STORAGE_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initData();
                }else{
                    showToast("权限被禁止，无法选择本地图片");
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
              /*  mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter.refreshData(imageFolder.images);
                    mtvDir.setText(imageFolder.name);
                }*/
            }
        });
        mPopupWindow.setMargin(bottom_panel.getHeight());
    }
}
