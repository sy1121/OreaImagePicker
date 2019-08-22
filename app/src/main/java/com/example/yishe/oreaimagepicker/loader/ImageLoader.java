package com.example.yishe.oreaimagepicker.loader;

import android.app.Activity;
import android.widget.ImageView;

public interface ImageLoader {
    void load(Activity activity, String path, ImageView imageView, int targetWidth, int targetHeight);
}
