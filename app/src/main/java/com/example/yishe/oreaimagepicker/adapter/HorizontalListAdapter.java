package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;

import java.util.List;

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.ViewHolder> {

    private List<ImageItem> mPicList;
    private Context mContext;
    private ImageItem mPreviewingImage;

    public HorizontalListAdapter(Context context,List<ImageItem> datas,ImageItem imageItem){
        mContext = context;
        mPicList = datas;
        mPreviewingImage = imageItem;
    }

    public void notifyPreviewImageChange(ImageItem newPreviewingImage){
        mPreviewingImage = newPreviewingImage;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.preview_horizontal_list_item,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ImageItem image = mPicList.get(position);
        Glide.with(mContext).load(image.path).placeholder(mContext.getDrawable(R.color.color_pic_back)).into(holder.mPic);
        if(image.equals(mPreviewingImage)){
            holder.mMask.setVisibility(View.VISIBLE);
        }else{
            holder.mMask.setVisibility(View.GONE);
        }
      /*  if(image.isSelected()){
            holder.mUnSelectedMask.setVisibility(View.GONE);
        }else{
            holder.mUnSelectedMask.setVisibility(View.VISIBLE);
        }*/

        if(onItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return mPicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView mPic;
        public View mUnSelectedMask;
        public ImageView mMask;
        public ViewHolder(View itemView) {
            super(itemView);
            mPic = (ImageView)itemView.findViewById(R.id.pic_thumbnail);
            mMask = (ImageView)itemView.findViewById(R.id.pic_selected_sign);
            mUnSelectedMask = itemView.findViewById(R.id.unselect_mask);
        }
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnItemClickListener onItemClickListener;

    public interface  OnItemClickListener{
        void onItemClick(int pos);
    }

}

