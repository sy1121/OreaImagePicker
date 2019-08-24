package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.util.Utils;

import java.util.ArrayList;
import java.util.List;


public class ImageSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PicSelectAdapter";
    private static final int ITEM_TYPE_NARMAL =0x01;
    private static final int ITEM_TYPE_CAMERA =0x02;
    private List<ImageItem> mDatas;
    private Context mContext;
    private int mImageSize;               //每个条目的大小
    private ImagePickModel mModel;

    private OnPicCheckChangeListener picCheckChangeListener;

    private OnItemClickListener itemClickListener;

    private OnCameraClickListener cameraClickListener;

    public interface  OnPicCheckChangeListener{
        void onChange(int pos,boolean checked);
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }

    public interface OnCameraClickListener{
        void onCameraClick();
    }


    public ImageSelectAdapter(Context context,List<ImageItem> imageItems){
        mContext = context;
        mModel = ImagePickModel.getInstance();
        if(imageItems == null){
            mDatas = new ArrayList<>();
        }else {
            mDatas = imageItems;
        }
        mImageSize = Utils.getImageItemWidth(mContext);
    }

    public void refreshData(List<ImageItem> images) {
        if (images == null || images.size() == 0) this.mDatas = new ArrayList<>();
        else this.mDatas = images;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==ITEM_TYPE_CAMERA){
            return new CameraViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_pic_camera,parent,false));
        }else{
            return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_pic_list,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder) {
            ((CameraViewHolder) holder).bindCamera();
        } else if (holder instanceof NormalViewHolder) {
            ((NormalViewHolder) holder).bind(position);
        }
    }



    @Override
    public int getItemViewType(int position) {
        if(ImagePickModel.getInstance().isShowCamera()){
            if(position ==0)  return ITEM_TYPE_CAMERA;
            else return ITEM_TYPE_NARMAL;
        }else{
            return ITEM_TYPE_NARMAL;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        return ImagePickModel.getInstance().isShowCamera() ? mDatas.size() + 1 : mDatas.size();
    }

    public ImageItem getItem(int position) {
        if (ImagePickModel.getInstance().isShowCamera()) {
            if (position == 0) return null;
            return mDatas.get(position - 1);
        } else {
            return mDatas.get(position);
        }
    }


    public class NormalViewHolder extends RecyclerView.ViewHolder{
        public ImageView pic;
        public ImageView mask;
        public ImageView select;

        public NormalViewHolder(View itemView) {
            super(itemView);
            pic = (ImageView) itemView.findViewById(R.id.item_pic);
            mask = (ImageView) itemView.findViewById(R.id.item_mask);
            select = (ImageView) itemView.findViewById(R.id.item_select);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
        }

        void bind(final int position){
            ImageItem item = getItem(position);
            final int pos = ImagePickModel.getInstance().isShowCamera() ? position - 1 : position;
            ImagePickModel.getInstance().getmImageLoader().load(mContext,item.path,pic,300,300);
            //Glide.with(mContext).load(item.path).centerCrop().placeholder(mContext.getDrawable(R.color.color_pic_back)).into(pic);
            if(ImagePickModel.getInstance().isMultiMode()) {
                if (mModel.getSelectedImages().contains(item)) {
                    select.setImageResource(R.mipmap.checkbox_checked);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    select.setImageResource(R.mipmap.checkbox_normal);
                    mask.setVisibility(View.GONE);
                }

                select.setTag(R.id.tag_first,pos);
                select.setTag(R.id.tag_second,mask);


                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int)v.getTag(R.id.tag_first);
                        ImageView mask = (ImageView)v.getTag(R.id.tag_second);
                        ImageItem item = mDatas.get(pos);
                        boolean isChecked;
                        if(mModel.getSelectedImages().contains(item)){
                            ((ImageView)v).setImageResource(R.mipmap.checkbox_normal);
                            mask.setVisibility(View.INVISIBLE);
                            isChecked = false;
                        }else{
                            if(mModel.hasReceivedMaxCount()){
                                Toast.makeText(mContext,"最多选中"+ ImagePickModel.getInstance().getSelectLimit() +"张图片",Toast.LENGTH_SHORT).show();
                                return ;
                            }
                            ((ImageView)v).setImageResource(R.mipmap.checkbox_checked);
                            mask.setVisibility(View.VISIBLE);
                            isChecked = true;
                        }
                        if(picCheckChangeListener!=null){
                            picCheckChangeListener.onChange(pos,isChecked);
                        }
                    }
                });

            }else{
                select.setVisibility(View.GONE);
                mask.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener!=null){
                        itemClickListener.onItemClick(pos);
                    }
                }
            });
        }
    }


    public class CameraViewHolder extends RecyclerView.ViewHolder{
        View mItemView;
        CameraViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
        }

        void bindCamera(){
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if(cameraClickListener!=null){
                       cameraClickListener.onCameraClick();
                   }
                }
            });
        }
    }


    public OnPicCheckChangeListener getPicCheckChangeListener() {
        return picCheckChangeListener;
    }

    public void setPicCheckChangeListener(OnPicCheckChangeListener picCheckChangeListener) {
        this.picCheckChangeListener = picCheckChangeListener;
    }

    public OnItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OnCameraClickListener getCameraClickListener() {
        return cameraClickListener;
    }

    public void setCameraClickListener(OnCameraClickListener cameraClickListener) {
        this.cameraClickListener = cameraClickListener;
    }
}
