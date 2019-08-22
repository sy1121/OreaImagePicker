package com.example.yishe.oreaimagepicker;

import android.app.Activity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.loader.ImageLoader;

public class GlideImageLoader implements ImageLoader {
    @Override
    public void load(Activity activity, String path, ImageView imageView, int targetWidth, int targetHeight) {
        Glide.with(activity)
                .load(path)
                .placeholder(activity.getDrawable(R.color.color_pic_back))
                .into(imageView);

    }
}
