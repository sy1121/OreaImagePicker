package com.example.yishe.oreaimagepicker.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author yishe
 * @date 2019/8/7.
 * email：yishe@tencent.com
 * description：
 */
public class ImageItem implements Parcelable {

    public String name;
    public String path;
    public int width;
    public int height;
    public long size;
    public String mediaType; //媒体类型
    public long createTime; //创建时间

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImageItem)) return false;
        ImageItem item = (ImageItem)obj;
        return name.equals(item.name) && path.equals(item.path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.size);
        dest.writeString(this.mediaType);
        dest.writeLong(this.createTime);
    }

    public ImageItem() {
    }

    protected ImageItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readLong();
        this.mediaType = in.readString();
        this.createTime = in.readLong();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
