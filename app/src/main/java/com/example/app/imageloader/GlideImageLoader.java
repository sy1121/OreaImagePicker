package com.example.app.imageloader;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.yishe.oreaimagepicker.loader.ImageLoader;

public class GlideImageLoader implements ImageLoader {
    @Override
    public void load(Context context, String path, ImageView imageView, int targetWidth, int targetHeight) {
        Glide.with(context)
                .load(path)
                .placeholder(context.getDrawable(com.example.yishe.oreaimagepicker.R.color.color_pic_back))
                .into(imageView);

    }
}
