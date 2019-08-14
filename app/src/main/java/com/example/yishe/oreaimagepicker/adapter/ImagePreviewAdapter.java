package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.List;

public class ImagePreviewAdapter extends PagerAdapter {

    private List<ImageView> datas;
    private Context mContext;

    public OnImageClickListener getOnImageClickListener() {
        return onImageClickListener;
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    private OnImageClickListener onImageClickListener;

    public interface  OnImageClickListener{
        void onClick();
    }

    public ImagePreviewAdapter(Context context,List<ImageView> views){
        mContext = context;
        datas =views;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(datas.get(position));
     /*   Image image = (Image)datas.get(position).getTag();
        PicassoWrapper.getInstance().build(mContext).load(Uri.parse("file://"+image.getUrl())).resize(image.getWidth(),image.getHeight()).into(datas.get(position));
      */  if(onImageClickListener!=null){
            onImageClickListener.onClick();
        }
        return datas.get(position);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(datas.get(position));
    }
}
