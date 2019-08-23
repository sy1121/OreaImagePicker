package com.example.yishe.oreaimagepicker.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.adapter.HorizontalListAdapter;
import com.example.yishe.oreaimagepicker.adapter.ImagePreviewAdapter;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.listener.OnCheckChangeListener;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends BaseActivity implements View.OnClickListener, OnCheckChangeListener {
    private static final String TAG = "PreviewActivity";

    @BindView(R.id.back_icon)
    ImageView back_btn;
    @BindView(R.id.title)
    TextView title_tv;
    @BindView(R.id.send_text)
    TextView send_tv;
    @BindView(R.id.preview_pic_big)
    ViewPager mViewPager;
    @BindView(R.id.preview_pic_small)
    RecyclerView mRecyclerView;
    @BindView(R.id.edit_btn)
    TextView mEditBtn;
    @BindView(R.id.choose_check)
    ImageView mChooseCheck;
    @BindView(R.id.preview_up_panel)
    RelativeLayout mHorizontalPanel;
    @BindView(R.id.preview_header)
    RelativeLayout mHeader;
    @BindView(R.id.preview_footer)
    RelativeLayout mFooter;

    public static final String PIC_SELECTED_INDEX_PARAM = "pic_selected_index_param";
    public static final String PIC_SELECTED_ALBUM_INDEX = "pic_selected_album_index";

    private List<ImageItem> mCurAlbumPicSet;
    private int mShowIndex = -1; //正在预览的照片位置索引
    private int mSelectedAlbumIndex = -1;
    private ImagePickModel mImagePickModel;

    private ImagePreviewAdapter mPreviewAdapter;
    private HorizontalListAdapter mHorizontalListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        mImagePickModel = ImagePickModel.getInstance();
        mSelectedAlbumIndex = getIntent().getIntExtra(PIC_SELECTED_ALBUM_INDEX,-1);
        mShowIndex = getIntent().getIntExtra(PIC_SELECTED_INDEX_PARAM,0);
        if(mSelectedAlbumIndex == -1){
            mCurAlbumPicSet = mImagePickModel.getSelectedImages();
        }else{
            mCurAlbumPicSet = mImagePickModel.getmAlbums().get(mSelectedAlbumIndex).items;
        }

        initView();
        initData();
        mImagePickModel.registerCheckChangeListener(this);
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        send_tv.setEnabled(true);
        send_tv.setOnClickListener(this);
        mChooseCheck.setOnClickListener(this);
        mEditBtn.setOnClickListener(this);


        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHeader.getLayoutParams();
            params.topMargin = Utils.getStatusHeight(this);
            mHeader.setLayoutParams(params);
        }
    }

    private void initData(){
        refreshTitle();
        refreshSendBtn();
        refreshCheckbox();
        initRecyclerview();
        initViewPager();
    }

    private void refreshSendBtn(){
        if(ImagePickModel.getInstance().getSelectedImages().size() > 0){
            send_tv.setText(String.format(getResources().getString(R.string.send_enable),ImagePickModel.getInstance().getSelectedImages().size()+"",ImagePickModel.getInstance().getSelectLimit()+"")); //"发送("+mSelectedCount+"/9)"
        }else{
            send_tv.setText(getResources().getString(R.string.send_disable));
        }
    }

    private void refreshTitle(){
        title_tv.setText((mShowIndex+1)+"/"+mCurAlbumPicSet.size());
    }

    private void refreshCheckbox(){
        ImageItem curImage = mCurAlbumPicSet.get(mShowIndex);
        if(ImagePickModel.getInstance().getSelectedImages().contains(curImage)){
            mChooseCheck.setImageResource(R.mipmap.checkbox_checked);
        }else{
            mChooseCheck.setImageResource(R.mipmap.checkbox_normal);
        }
    }

    private void refreshBottomPanel(){
        if(ImagePickModel.getInstance().getSelectedImages().size() == 0){
            mHorizontalPanel.setVisibility(View.GONE);
        }else{
            mHorizontalPanel.setVisibility(View.VISIBLE);
        }
    }

    private void initRecyclerview(){
        mHorizontalListAdapter = new HorizontalListAdapter(this,ImagePickModel.getInstance().getSelectedImages(),mCurAlbumPicSet.get(mShowIndex));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mHorizontalListAdapter);
        mHorizontalListAdapter.setOnItemClickListener(new HorizontalListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                mShowIndex = mCurAlbumPicSet.indexOf(ImagePickModel.getInstance().getSelectedImages().get(pos));
                refreshTitle();
                mViewPager.setCurrentItem(mShowIndex);
                mHorizontalListAdapter.notifyDataSetChanged();
                refreshCheckbox();

            }
        });
        refreshBottomPanel();

    }

    private void initViewPager(){
        mPreviewAdapter = new ImagePreviewAdapter(this,mCurAlbumPicSet);
        mPreviewAdapter.setOnImageClickListener(new ImagePreviewAdapter.OnImageClickListener() {
            @Override
            public void onClick() {
                onImageTip();
            }
        });
        mViewPager.setAdapter(mPreviewAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //切换图片的预览状态
                mShowIndex = position;
                //修改title
                refreshTitle();
                //修改选择框
                refreshCheckbox();
                //修改横向预览列表
                mHorizontalListAdapter.notifyPreviewImageChange(mCurAlbumPicSet.get(mShowIndex));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(mShowIndex);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mImagePickModel.unregisterCheckChangeListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_icon:
                finish();
                break;
            case R.id.send_text:
                setResult(android.app.Activity.RESULT_OK);
                finish();
                break;
            case R.id.edit_btn:
                break;
            case R.id.choose_check:
                boolean isChecked = ImagePickModel.getInstance().getSelectedImages().contains(mCurAlbumPicSet.get(mShowIndex));
                if(!isChecked && mImagePickModel.hasReceivedMaxCount()){
                    Toast.makeText(this,"最多选中"+ ImagePickModel.getInstance().getSelectLimit() +"张图片",Toast.LENGTH_SHORT).show();
                    return ;
                }
                ImagePickModel.getInstance().notifyCheckChanged(mCurAlbumPicSet.get(mShowIndex),!isChecked);
                break;

        }
    }

    @Override
    public void onChange() {
        //修改checkbox
        refreshCheckbox();
        //修改发送按钮
        refreshSendBtn();
        //修改水平预览状态
        mHorizontalListAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mHorizontalListAdapter.getItemCount());
        //bottomPanel可见
        refreshBottomPanel();
        //点击预览按钮进来的做一下特殊处理
        if(mSelectedAlbumIndex == -1){
            mPreviewAdapter.notifyDataSetChanged();
            refreshTitle();
        }
    }


    private void onImageTip(){
        if(mHeader.getVisibility()==View.VISIBLE){
            mHeader.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_out));
            mFooter.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_out));
            tintManager.setStatusBarTintResource(Color.TRANSPARENT);
            mHeader.setVisibility(View.GONE);
            mFooter.setVisibility(View.GONE);
        }else{
            mHeader.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_in));
            mFooter.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_in));
            tintManager.setStatusBarTintResource(R.color.ip_color_primary);
            mHeader.setVisibility(View.VISIBLE);
            mFooter.setVisibility(View.VISIBLE);
        }
    }
}
