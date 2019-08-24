package com.example.yishe.oreaimagepicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yishe.oreaimagepicker.R;
import com.example.yishe.oreaimagepicker.entity.Album;
import com.example.yishe.oreaimagepicker.model.ImagePickModel;

import java.util.ArrayList;
import java.util.List;

public class ImageFolderListAdapter extends BaseAdapter {

    private static final String TAG = "PicFolderListAdapter";
    private Context mContext;
    private List<Album> mDatas;
    private int lastSelected = 0;

    public ImageFolderListAdapter(Context context,List<Album> folders){
        mContext = context;
        if(folders == null){
            mDatas = new ArrayList<>();
        }else {
            mDatas = folders;
        }
    }

    public void refreshData(List<Album> albums) {
        if (albums == null || albums.size() == 0) this.mDatas = new ArrayList<>();
        else this.mDatas = albums;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_folder_list,null);
            viewHolder.thumbnail = (ImageView)convertView.findViewById(R.id.folder_thumbnail);
            viewHolder.folderName = (TextView)convertView.findViewById(R.id.folder_name);
            viewHolder.fileCount = (TextView)convertView.findViewById(R.id.file_count);
            viewHolder.selectIcon = (ImageView)convertView.findViewById(R.id.select_sign);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Album cur = mDatas.get(position);
        //Glide.with(mContext).load(cur.thumbnail.path).centerCrop().placeholder(mContext.getDrawable(R.color.color_pic_back)).into(viewHolder.thumbnail);
        ImagePickModel.getInstance().getmImageLoader().load(mContext,cur.thumbnail.path,viewHolder.thumbnail,300,300);
        viewHolder.folderName.setText(cur.name);
        viewHolder.fileCount.setText("共"+cur.items.size()+"张");
        int curSelectedAlbumIndex = ImagePickModel.getInstance().getmCurSelectedAlbumIndex();
        if(ImagePickModel.getInstance().getmAlbums().indexOf(cur) == curSelectedAlbumIndex){
            viewHolder.selectIcon.setVisibility(View.VISIBLE);
            lastSelected = position;
        }else{
            viewHolder.selectIcon.setVisibility(View.GONE);
        }
        return convertView;
    }

    public int getLastSelected() {
        return lastSelected;
    }


    public class ViewHolder{
        public ImageView thumbnail;
        public TextView folderName;
        public TextView fileCount;
        public ImageView selectIcon;
    }
}
