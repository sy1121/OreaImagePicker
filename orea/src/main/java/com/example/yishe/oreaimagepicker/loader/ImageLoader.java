package com.example.yishe.oreaimagepicker.loader;

import android.content.Context;
import android.widget.ImageView;

public interface ImageLoader {
    void load(Context context, String path, ImageView imageView, int targetWidth, int targetHeight);
}
