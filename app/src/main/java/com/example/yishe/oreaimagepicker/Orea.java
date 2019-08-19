package com.example.yishe.oreaimagepicker;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * @author yishe
 * @date 2019/8/19.
 * email：yishe@tencent.com
 * description：
 */
public class Orea {

    private final WeakReference<Activity> mContext;

    private Orea(Activity activity){
        mContext = new WeakReference<>(activity);
    }

    public static Orea from(Activity activity){
        return new Orea(activity);
    }

    public SelectionCreator select(){
        return new SelectionCreator(new Orea(mContext.get()));
    }

    public Activity getActivity(){
        return mContext.get();
    }
}
