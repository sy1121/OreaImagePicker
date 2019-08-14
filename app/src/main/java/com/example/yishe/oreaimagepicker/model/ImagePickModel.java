package com.example.yishe.oreaimagepicker.model;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;


import com.example.yishe.oreaimagepicker.data.ImageDataSource;
import com.example.yishe.oreaimagepicker.entity.Album;
import com.example.yishe.oreaimagepicker.entity.ImageItem;
import com.example.yishe.oreaimagepicker.util.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImagePickModel {
    private List<Album> mAlbums;
    private int mCurSelectedAlbumIndex;
    private List<ImageItem> selectedImages;
    private static volatile ImagePickModel mInstance;

    private File takeImageFile;

    //配置设置
    private boolean multiMode = false;    //图片选择模式
    private int selectLimit = 9;         //最大选择图片数量
    private boolean crop = false;         //裁剪
    private boolean preview = false;     //是否预览
    private boolean showCamera = true;   //显示相机


    private ImagePickModel(){
        mAlbums = new ArrayList<>();
        selectedImages = new ArrayList<>();
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
                mAlbums.clear();
                mAlbums.addAll(albums);
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
            if (Utils.existSDCard()) takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            else takeImageFile = Environment.getDataDirectory();
            takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！

                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    uri = Uri.fromFile(takeImageFile);
                } else {

                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                     */
                    uri = FileProvider.getUriForFile(activity, Utils.getFileProviderName(activity), takeImageFile);
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
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }



    public interface  DataLoadCallback{
        void onLoadFinished();
    }

    public void release(){
        takeImageFile= null;
        mInstance = null;
    }

    public boolean hasReceivedMaxCount(){
        return getSelectedImages().size() >= 9;
    }


    public boolean isMultiMode() {
        return multiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public File getTakeImageFile() {
        return takeImageFile;
    }


    public List<Album> getmAlbums() {
        return mAlbums;
    }

    public List<ImageItem> getSelectedImages() {
        return selectedImages;
    }

    public int getmCurSelectedAlbumIndex() {
        return mCurSelectedAlbumIndex;
    }

    public void setmCurSelectedAlbumIndex(int mCurSelectedAlbumIndex) {
        this.mCurSelectedAlbumIndex = mCurSelectedAlbumIndex;
    }

}
