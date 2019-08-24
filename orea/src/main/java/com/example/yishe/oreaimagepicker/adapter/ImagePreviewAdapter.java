package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;


public class ImagePreviewAdapter extends PagerAdapter {

    private List<ImageItem> mImages;
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

    public ImagePreviewAdapter(Context context,List<ImageItem> images){
        mContext = context;
        mImages = images;
    }


    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView imageView = new PhotoView(mContext);
        container.addView(imageView);
        ImageItem image = mImages.get(position);
        //Glide.with(mContext).load(image.path).placeholder(mContext.getDrawable(R.color.color_pic_back)).into(imageView);
        ImagePickModel.getInstance().getmImageLoader().load(mContext,image.path,imageView,300,300);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onImageClickListener!=null){
                    onImageClickListener.onClick();
                }
            }
        });
        return imageView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
