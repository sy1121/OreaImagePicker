package com.example.yishe.oreaimagepicker.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;



import com.example.yishe.oreaimagepicker.entity.Album;
import com.example.yishe.oreaimagepicker.entity.ImageItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yishe
 * @date 2019/8/7.
 * email：yishe@tencent.com
 * description：
 */
public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ImageDataSource";

    private final Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final String[] PROJECTIONS = new String[]{
            MediaStore.Images.Media.DISPLAY_NAME,//名称
            MediaStore.Images.Media.DATA,//路径
            MediaStore.Images.Media.WIDTH,//宽
            MediaStore.Images.Media.HEIGHT,//高
            MediaStore.Images.Media.SIZE,//大小
            MediaStore.Images.Media.MIME_TYPE,//类型
            MediaStore.Images.Media.DATE_ADDED//创建时间
    };

    private WeakReference<FragmentActivity> mContext;
    private OnImageLoadListener mLoadListener;

    public ImageDataSource(FragmentActivity context, OnImageLoadListener listener){
        mContext = new WeakReference<>(context);
        mLoadListener = listener;
        LoaderManager loaderManager = context.getSupportLoaderManager();
        loaderManager.initLoader(0,null,this);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        Context context = mContext.get();
        if(context == null) return null;
        CursorLoader cursorLoader = new CursorLoader(mContext.get(),URI,PROJECTIONS,null,null,MediaStore.Images.Media.DATE_ADDED + " DESC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        List<Album> albums = new ArrayList<>();
        if(cursor != null){
            List<ImageItem> items = new ArrayList<>();
            while(cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndexOrThrow(PROJECTIONS[0]));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(PROJECTIONS[1]));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(PROJECTIONS[2]));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(PROJECTIONS[3]));
                long size = cursor.getInt(cursor.getColumnIndexOrThrow(PROJECTIONS[4]));
                String mediaType = cursor.getString(cursor.getColumnIndexOrThrow(PROJECTIONS[5]));
                long createTime = cursor.getLong(cursor.getColumnIndexOrThrow(PROJECTIONS[6]));
                File file = new File(path);
                if(!file.exists() || file.length() == 0) continue;
                ImageItem image = new ImageItem();
                image.name = name;
                image.path = path;
                image.height = height;
                image.width = width;
                image.size = size;
                image.mediaType = mediaType;
                image.createTime = createTime;
                items.add(image);

                File newFile = new File(path);
                File parentFile = newFile.getParentFile();
                Album newAlbum = new Album();
                newAlbum.name = parentFile.getName();
                newAlbum.path = parentFile.getAbsolutePath();
                if(albums.contains(newAlbum)){
                    albums.get(albums.indexOf(newAlbum)).items.add(image);
                }else{
                    List<ImageItem> imageItmes = new ArrayList<>();
                    imageItmes.add(image);
                    newAlbum.items = imageItmes;
                    newAlbum.thumbnail = image;
                    albums.add(newAlbum);
                }
            }
            if(!items.isEmpty()) {
                //添加全部图片相册
                Album allImagesAlbum = new Album();
                allImagesAlbum.path = "/";
                allImagesAlbum.name = "全部图片";
                allImagesAlbum.items = items;
                allImagesAlbum.thumbnail = items.get(0);
                albums.add(0,allImagesAlbum);
            }
            Log.i(TAG,"image size = " + items.size());
            Log.i(TAG,"alnum size = " + albums.size());
        }

        if(mLoadListener != null){
            mLoadListener.onLoadFinished(albums);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.i(TAG,"onLoaderReset");
    }

    public interface OnImageLoadListener{
        void onLoadFinished(List<Album>albums);
    }
}
