package com.example.yishe.oreaimagepicker.ui;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.adapter.HorizontalListAdapter;
import com.example.yishe.oreaimagepicker.adapter.ImagePreviewAdapter;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener{

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

    private static final String TAG = "PreviewPage";
    public static final String PIC_SET_PARAM = "pic_urls_param";
    public static final String PIC_SELECTED_INDEX_PARAM = "pic_selected_index_param";
    public static final String PIC_SELECTED_ALBUM_INDEX = "pic_selected_album_index";
    private static final int INDEX_SELECTED_PICS = 1;
    private static final int INDEX_CUR_ALBUM_PICS = 2;


    private List<ImageItem> mCurAlbumPicSet;
    private int mShowIndex = -1; //正在预览的照片位置索引
    private int mSelectedCount = 0;
    private int mSelectedAlbumIndex = -1;
    private ImagePickModel mImagePickModel;

    private ImagePreviewAdapter mPreviewAdapter;
    private HorizontalListAdapter mHorizontalListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
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
    }

    private void initView(){
        back_btn.setOnClickListener(this);
        send_tv.setEnabled(true);
        send_tv.setOnClickListener(this);
        mChooseCheck.setOnClickListener(this);
        mEditBtn.setOnClickListener(this);
    }

    private void initData(){
        refreshTitle();
        refreshSendBtn();
        refreshCheckbox();
        initRecyclerview();
        initViewPager();
    }


    private void doPageSelected(){
        refreshTitle();
        refreshCheckbox();
    }

    private void doCheckChanged(){
        /*Image cur = mCurAlbumPicSet.get(mShowIndex);
        int indexInSelectedSet=findPosInPicList(cur.getUrl(),INDEX_SELECTED_PICS);
        if(cur.isSelected()){
            mChooseCheck.setImageResource(R.drawable.checkbox_normal);
            cur.setSelected(false);
            mSelectedCount--;
            mSelectedPicSet.remove(cur);
        }else{
            mChooseCheck.setImageResource(R.drawable.checkbox_checked);
            cur.setSelected(true);
            mSelectedCount++;
            mSelectedPicSet.add(cur);
        }*/

        refreshSendBtn();
        Log.i("hahaha","doCheckChange");
        if(mSelectedCount==0){
            mHorizontalPanel.setVisibility(View.GONE);
            //mRecyclerView.setVisibility(View.GONE);
        }else {
            mHorizontalPanel.setVisibility(View.VISIBLE);
            //mRecyclerView.setVisibility(View.VISIBLE);
        }
        mHorizontalListAdapter.notifyDataSetChanged();
       /* if(indexInSelectedSet ==-1){ //之前没有选择，添加，只需要更新最后一个
            //mHorizontalListAdapter.notifyItemInserted(mSelectedPicSet.size()-1);
            mHorizontalListAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mSelectedPicSet.size()-1);
        }else{//删除
           *//* mHorizontalListAdapter.notifyItemRemoved(indexInSelectedSet);
            mHorizontalListAdapter.notifyItemRangeChanged(0,mSelectedPicSet.size());*//*
            mHorizontalListAdapter.notifyDataSetChanged();
        }*/

    }

    private void refreshSendBtn(){
        if(mSelectedCount >0){
            send_tv.setText(String.format(getResources().getString(R.string.send_enable),mSelectedCount+"",ImagePickModel.getInstance().getSelectLimit()+"")); //"发送("+mSelectedCount+"/9)"
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

    private void refreshRecyclerView(int lastIndex,int curIndex){
        if(mSelectedCount==0){
            mHorizontalPanel.setVisibility(View.GONE);
        }else{
            mHorizontalPanel.setVisibility(View.VISIBLE);
            if(lastIndex!=-1){
                mHorizontalListAdapter.notifyItemChanged(lastIndex);
            }
            if(curIndex!=-1){
                mHorizontalListAdapter.notifyItemChanged(curIndex);
                mRecyclerView.smoothScrollToPosition(curIndex);
            }
        }


    }

    private void initRecyclerview(){
       /* mHorizontalListAdapter = new HorizontalListAdapter(this,mSelectedPicSet);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mHorizontalListAdapter);
        mHorizontalListAdapter.setOnItemClickListener(new HorizontalListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                if(!mCurAlbumPicSet.get(mShowIndex).path.equals(mSelectedPicSet.get(pos).path)){
                    int lastIndexInSelectedSet = findPosInPicList(mCurAlbumPicSet.get(mShowIndex).path,INDEX_SELECTED_PICS);
                    ImageItem cur = mCurAlbumPicSet.get(mShowIndex);
                    int indexInAlbum =findPosInPicList(mSelectedPicSet.get(pos).path,INDEX_CUR_ALBUM_PICS);
                    if(indexInAlbum<0) return ;
                    mShowIndex  = indexInAlbum;
                    int curIndexInSelectedSet = findPosInPicList(mCurAlbumPicSet.get(mShowIndex).path,INDEX_SELECTED_PICS);
                    cur = mCurAlbumPicSet.get(mShowIndex);

                    mViewPager.setCurrentItem(mShowIndex);
                    refreshTitle();
                    refreshCheckbox();
                    refreshRecyclerView(lastIndexInSelectedSet,curIndexInSelectedSet);
                }
            }
        });
*/
        if(mSelectedCount==0){
            mHorizontalPanel.setVisibility(View.GONE);
        }else{
            mHorizontalPanel.setVisibility(View.VISIBLE);
        }

    }

    private void initViewPager(){
        //生成viewpager展示的ImageView
        List<ImageView> imageViews  = new ArrayList<>();
        for(int i=0;i<mCurAlbumPicSet.size();i++){
            ImageView imageView= new ImageView(this);
            imageView.setTag(mCurAlbumPicSet.get(i));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mHeader.getVisibility()==View.VISIBLE){
                        mHeader.setVisibility(View.GONE);
                        mFooter.setVisibility(View.GONE);
                    }else{
                        mHeader.setVisibility(View.VISIBLE);
                        mFooter.setVisibility(View.VISIBLE);
                    }
                }
            });
            imageViews.add(imageView);
        }
        mPreviewAdapter = new ImagePreviewAdapter(this,imageViews);
        mViewPager.setAdapter(mPreviewAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //切换图片的预览状态
                /*int lastIndexInSelectedSet = findPosInPicList(mCurAlbumPicSet.get(mShowIndex).getUrl(),INDEX_SELECTED_PICS);
                ImageItem curImage = mCurAlbumPicSet.get(mShowIndex);
                //curImage.setPreViewing(false);
                mShowIndex = position; //更新预览图片的index
                int curIndexInSelectedSet = findPosInPicList(mCurAlbumPicSet.get(mShowIndex).getUrl(),INDEX_SELECTED_PICS);
                ImageItem newSelectedImage = mCurAlbumPicSet.get(mShowIndex);
                //newSelectedImage.setPreViewing(true);
                refreshRecyclerView(lastIndexInSelectedSet,curIndexInSelectedSet);
                doPageSelected();*/ //更新其他控件的状态显示
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(mShowIndex);
    }


    private List<Image> pathToImage(List<String> paths){
        List<Image> images = new ArrayList<>();
        Set<String> sets = new HashSet<>();
        for(int i=0;i<paths.size();i++){
            sets.add(paths.get(i));
        }
      /*  for(Image image:mImagePickModel.getmFolders().get(0).getImages()){
            if(sets.contains(image.getUrl())){
                images.add(image);
            }
        }*/
        return images;
    }

    private int findPosInPicList(String picPath,int setIndex){
        /*List<Image> searchSet= mCurAlbumPicSet;
        switch (setIndex){
            case INDEX_CUR_ALBUM_PICS:
                searchSet=mCurAlbumPicSet;
                break;
            case INDEX_SELECTED_PICS:
                searchSet = mSelectedPicSet;
                break;
        }
        if(TextUtils.isEmpty(picPath)||searchSet==null||searchSet.isEmpty()) return -1;
        for(int i=0;i<searchSet.size();i++){
            if(searchSet.get(i).getUrl().equals(picPath)) return i;
        }*/
        return -1;
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
     //   mCurAlbumPicSet.get(mShowIndex).setPreViewing(false);
        mCurAlbumPicSet.clear();
        mCurAlbumPicSet = null;
        mShowIndex=-1;
        mSelectedCount =0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_icon:
                finish();
                break;
            case R.id.send_text:
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.edit_btn:
                break;
            case R.id.choose_check:
                doCheckChanged();
                break;

        }
    }
}
