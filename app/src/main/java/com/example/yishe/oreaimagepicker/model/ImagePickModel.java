package com.example.yishe.oreaimagepicker.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.example.yishe.oreaimagepicker.data.ImageDataSource;
import com.example.yishe.oreaimagepicker.entity.Album;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.listener.OnCheckChangeListener;
import com.example.yishe.oreaimagepicker.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImagePickModel {
    private static final String TAG = "ImagePickModel";

    private ArrayList<Album> mAlbums;
    private int mCurSelectedAlbumIndex;
    private ArrayList<ImageItem> mSelectedImages;
    private List<OnCheckChangeListener> mCheckChangeListeners;
    private static volatile ImagePickModel mInstance;

    private File mTakeImageFile;

    //配置设置
    private boolean mMultiMode = false;    //图片选择模式
    private int mSelectLimit = 9;         //最大选择图片数量
    private boolean mCrop = false;         //裁剪
    private boolean mPreview = false;     //是否预览
    private boolean mShowCamera = true;   //显示相机


    private ImagePickModel(){
        mAlbums = new ArrayList<>();
        mSelectedImages = new ArrayList<>();
        mCheckChangeListeners = new ArrayList<>();
    }

    public static ImagePickModel getInstance(){
        if(mInstance==null){
            synchronized (ImagePickModel.class){
                if(mInstance == null){
                    mInstance = new ImagePickModel();
                }
            }
        }
        return mInstance;
    }


    public void scanImage(FragmentActivity context,DataLoadCallback callback){
        new ImageDataSource(context, new ImageDataSource.OnImageLoadListener() {
            @Override
            public void onLoadFinished(List<Album> albums) {
                Log.i(TAG,"onLoadFinished");
                mAlbums.clear();
                mAlbums.addAll(new ArrayList<>(albums));
                if(!mAlbums.isEmpty()){
                    mCurSelectedAlbumIndex = 0; //默认选中第一相册
                }
                if(callback != null){
                    callback.onLoadFinished();
                }
            }
        });
    }


    /**
     * 拍照的方法
     */
    public void takePicture(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            if (Utils.existSDCard()) mTakeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            else mTakeImageFile = Environment.getDataDirectory();
            mTakeImageFile = createFile(mTakeImageFile, "IMG_", ".jpg");
            if (mTakeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！

                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    uri = Uri.fromFile(mTakeImageFile);
                } else {

                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                     */
                    uri = FileProvider.getUriForFile(activity, Utils.getFileProviderName(activity), mTakeImageFile);
                    //加入uri权限 要不三星手机不能拍照
                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
    }


    /**
     * 扫描图片
     */
    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }


    public void notifyCheckChanged(int pos,boolean isChecked){
        int curAlbumIndex = ImagePickModel.getInstance().getmCurSelectedAlbumIndex();
        ImageItem curImage = ImagePickModel.getInstance().getmAlbums().get(curAlbumIndex).items.get(pos);
        if(isChecked){
            ImagePickModel.getInstance().getSelectedImages().add(curImage);
        }else{
            ImagePickModel.getInstance().getSelectedImages().remove(curImage);
        }

        for(OnCheckChangeListener listener : mCheckChangeListeners){
            listener.onChange();
        }

    }

    public void release(){
        mCurSelectedAlbumIndex = -1;
        mInstance = null;
        if(mSelectedImages != null){
            mSelectedImages.clear();
            mSelectedImages = null;
        }

        if(mAlbums != null){
            mAlbums.clear();
            mAlbums = null;
        }

        if(mCheckChangeListeners != null){
            mCheckChangeListeners.clear();
            mCheckChangeListeners = null;
        }
        mTakeImageFile= null;

    }

    public void registerCheckChangeListener(OnCheckChangeListener listener){
        if(mCheckChangeListeners == null) return ;
        mCheckChangeListeners.add(listener);
    }

    public void removeCheckChangeListener(OnCheckChangeListener listener){
        if(mCheckChangeListeners == null) return ;
        mCheckChangeListeners.remove(listener);
    }


    public interface  DataLoadCallback{
        void onLoadFinished();
    }

    public boolean hasReceivedMaxCount(){
        return getSelectedImages().size() >= 9;
    }


    public boolean isMultiMode() {
        return mMultiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.mMultiMode = multiMode;
    }

    public int getSelectLimit() {
        return mSelectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.mSelectLimit = selectLimit;
    }

    public boolean isCrop() {
        return mCrop;
    }

    public void setCrop(boolean crop) {
        this.mCrop = crop;
    }

    public boolean isPreview() {
        return mPreview;
    }

    public void setPreview(boolean preview) {
        this.mPreview = preview;
    }

    public boolean isShowCamera() {
        return mShowCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.mShowCamera = showCamera;
    }

    public File getTakeImageFile() {
        return mTakeImageFile;
    }


    public List<Album> getmAlbums() {
        return mAlbums;
    }

    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }

    public int getmCurSelectedAlbumIndex() {
        return mCurSelectedAlbumIndex;
    }

    public void setmCurSelectedAlbumIndex(int mCurSelectedAlbumIndex) {
        this.mCurSelectedAlbumIndex = mCurSelectedAlbumIndex;
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList("mAlbums",mAlbums);
        outState.putParcelableArrayList("mSelectedImages",mSelectedImages);
        outState.putInt("mCurSelectedAlbumIndex",mCurSelectedAlbumIndex);
        outState.putBoolean("mMultiMode",mMultiMode);
        outState.putInt("mSelectLimit",mSelectLimit);
        outState.putBoolean("mCrop",mCrop);
        outState.putBoolean("mPreview",mPreview);
        outState.putBoolean("mShowCamera",mShowCamera);
        outState.putSerializable("mTakeImageFile",mTakeImageFile);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        mAlbums = savedInstanceState.getParcelableArrayList("mAlbums");
        mSelectedImages = savedInstanceState.getParcelableArrayList("mSelectedImages");
        mCurSelectedAlbumIndex = savedInstanceState.getInt("mCurSelectedAlbumIndex",0);
        mMultiMode = savedInstanceState.getBoolean("mMultiMode",false);
        mSelectLimit = savedInstanceState.getInt("mSelectLimit",9);
        mCrop = savedInstanceState.getBoolean("mCrop",false);
        mPreview = savedInstanceState.getBoolean("mPreview",false);
        mShowCamera = savedInstanceState.getBoolean("mShowCamera",false);
        mTakeImageFile = (File)savedInstanceState.getSerializable("mTakeImageFile");
    }
}
