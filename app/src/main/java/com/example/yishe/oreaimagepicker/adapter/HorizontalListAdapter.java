package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yishe.oreaimagepicker.entity.ImageItem;

import java.util.List;

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.ViewHolder> {

    private List<ImageItem> mPicList;
    private Context mContext;

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
    public HorizontalListAdapter(Context context,List<ImageItem> datas){
        mContext = context;
        mPicList = datas;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    //    View view = LayoutInflater.from(mContext).inflate(R.layout.preview_horizontal_list_item,null);
    //    return new ViewHolder(view);
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ImageItem image = mPicList.get(position);
       /* PicassoWrapper.getInstance().build(mContext).load(Uri.parse("file://"+image.getUrl())).resize(300,300).into(holder.mPic);
        if(image.isPreViewing()){
            holder.mMask.setVisibility(View.VISIBLE);
        }else{
            holder.mMask.setVisibility(View.GONE);
        }
        if(image.isSelected()){
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
        /*    mPic = (ImageView)itemView.findViewById(R.id.pic_thumbnail);
            mMask = (ImageView)itemView.findViewById(R.id.pic_selected_sign);
            mUnSelectedMask = itemView.findViewById(R.id.unselect_mask);*/
        }
    }
}

