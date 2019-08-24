package com.example.app.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yishe
 * @date 2019/8/19.
 * email：yishe@tencent.com
 * description：
 */
public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ViewHolder> {
    private static final String TAG = "SelectedImageAdapter";

    private Context mContext;
    private List<ImageItem> mImages;

    public SelectedImageAdapter(Context context, List<ImageItem> images){
        mContext = context;
        if(images == null){
            mImages = new ArrayList<>();
        }else{
            mImages = images;
        }
    }

    public void refreshData(List<ImageItem> images){
        if(images == null) return;
        mImages.clear();
        mImages.addAll(new ArrayList<>(images));
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_selected_image_layout,null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageItem imageItem = mImages.get(position);
        Glide.with(mContext).load(imageItem.path).placeholder(mContext.getDrawable(R.color.color_pic_back)).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
        }
    }


}
