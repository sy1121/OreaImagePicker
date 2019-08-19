package com.example.yishe.oreaimagepicker;

import android.app.Activity;
import android.content.Intent;

import com.example.yishe.oreaimagepicker.model.ImagePickModel;
import com.example.yishe.oreaimagepicker.ui.GridActivity;

/**
 * @author yishe
 * @date 2019/8/19.
 * email：yishe@tencent.com
 * description：
 */
public class SelectionCreator {
    private final Orea mOrea;
    private final ImagePickModel mModel;

    public SelectionCreator(Orea orea){
        mOrea = orea;
        mModel = ImagePickModel.getInstance();
    }

    public SelectionCreator setMultiMode(boolean multiMode){
        mModel.setMultiMode(multiMode);
        return this;
    }

    public SelectionCreator setSelectLimit(int selectLimit){
        mModel.setSelectLimit( selectLimit);
        return this;
    }

    public SelectionCreator setCrop(boolean isCrop){
        mModel.setCrop(isCrop);
        return this;
    }

    public SelectionCreator setPreview(boolean isPreview){
        mModel.setPreview(isPreview);
        return this;
    }

    public SelectionCreator setShowCamera(boolean showCamera){
        mModel.setShowCamera(showCamera);
        return this;
    }

    public void forResult(int requestCode){
        Activity activity = mOrea.getActivity();
        if(activity == null) return;
        Intent intent = new Intent(activity, GridActivity.class);
        activity.startActivityForResult(intent,requestCode);
    }



}
