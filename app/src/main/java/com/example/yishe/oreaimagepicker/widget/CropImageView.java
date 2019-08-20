package com.example.yishe.oreaimagepicker.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.example.yishe.oreaimagepicker.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author yishe
 * @date 2019/8/20.
 * email：yishe@tencent.com
 * description：
 */
public class CropImageView extends PhotoView {
    private static final String TAG = "CropImageView";

    public enum Style {
        RECTANGLE,CIRCLE
    }

    private Style[]styles = {Style.RECTANGLE, Style.CIRCLE};

    private int mMaskColor = 0xAF000000; //暗色
    private int mBorderColor = 0xAA808080;//焦点边框颜色
    private int mFocusedWidth = 200;//焦点框的宽度
    private int mFocusedHeight = 200; //焦点框的高度
    private int mBorderWidth = 1;         //焦点边框的宽度（画笔宽度）
    private int mDefaultStyleIndex = 0;   // 默认焦点框的形状

    private Style mStyle = styles[mDefaultStyleIndex];
    private Paint mBorderPaint = new Paint();
    private RectF mFocusedRect = new RectF(); //焦点框矩形
    private Path mFocusedPath = new Path(); //焦点框路径

    private static final float MAX_SCALE = 4.0f; // 最大缩放比，图片缩放后大小和中间选中区域的比值
    private static final int NONE = 0; //初始化
    private static final int DRAG = 1; //拖拽
    private static final int ZOOM = 2; //缩放
    private static final int ROTATE = 3; //旋转
    private static final int ZOOM_OR_ROTATE = 4; //缩放或旋转

    private static final int SAVE_SUCCESS = 1001; //
    private static final int SAVE_ERROR = 1002; //

    private int mImageWidth;
    private int mImageHeight;
    private int mRotatedImageWidth;
    private int mRotatedImageHeight;
    private Matrix matrix = new Matrix();  //图片变换的matirx
    private Matrix savedMatrix = new Matrix(); //开始变换的时候，图片的matrix
    private PointF pA = new PointF();    //第一个手指按下的坐标
    private PointF PB = new PointF();    //第二个手指按下的坐标
    private PointF midPoint = new PointF(); //两个手指的中间点
    private PointF doubleClickPos = new PointF(); //双击图片的时候，双击点的坐标
    private PointF mFocusMidPoint = new PointF();  //焦点框的中间点
    private int mode = NONE;                //初始状态
    private long  doubleClickTime = 0;      //第二次双击的时间
    private double rotation = 0;            //手指旋转的角度，不是90的整数倍，可能为任意值，
    private float oldDist = 1;              // 双指第一次的距离
    private int sumRotateLevel = 0;         // 旋转的角度，90的整数倍
    private float mMaxScale = MAX_SCALE;    // 根据不同图片的大小，动态得到的最大缩放比
    private boolean isInited = false;       // 是否经过了onSizeChanged 初始化
    private boolean mSaving = false;        // 是否正在保存


    private int mWidth,mHeight; //控件宽高
    private float mCenterX,mCenterY;//控件中心点坐标

    public CropImageView(Context context){
        this(context,null);
    }

    public CropImageView(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }

    public CropImageView(Context context,AttributeSet attrs,int defStyle){
        super(context, attrs,defStyle);

        mFocusedWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mFocusedWidth, getResources().getDisplayMetrics());
        mFocusedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mFocusedHeight, getResources().getDisplayMetrics());

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    private void init(){
        mWidth = getWidth();
        mHeight = getHeight();
        mCenterX = (mWidth) / 2;
        mCenterY = (mHeight) / 2;

        mFocusedRect = new RectF(mCenterX - mFocusedWidth / 2,mCenterY - mFocusedHeight / 2,
                mCenterX + mFocusedWidth / 2,mCenterY + mFocusedHeight / 2);
        mFocusedPath = new Path();
        int radius = Math.min(mFocusedHeight,mFocusedWidth) / 2;
        mFocusedPath.addCircle(mCenterX,mCenterY,radius,Path.Direction.CCW);
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(new RectF(0,0,mWidth,mHeight));
        canvas.clipPath(mFocusedPath, Region.Op.DIFFERENCE);
        canvas.drawColor(getResources().getColor(R.color.colorMask));
        canvas.restore();
    }


    public void saveCropViewToBitmap(){
        Bitmap bitmap = Bitmap.createBitmap( ((BitmapDrawable) getDrawable()).getBitmap(),(int)mFocusedRect.left,(int)mFocusedRect.top,mFocusedWidth,mFocusedHeight);
        int length = Math.min(mFocusedHeight, mFocusedWidth);
        int radius = length / 2;
        Bitmap circleBitmap = Bitmap.createBitmap(length,length, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawCircle(mFocusedWidth / 2,mFocusedHeight / 2,radius,paint);
        saveBitmapToFile(circleBitmap);
    }


    public void saveBitmapToFile(File folder,int expectedWidth,int expectedHeight,boolean isSaveRectangle){

    }

    @SuppressLint("WrongThread")
    private void saveBitmapToFile(Bitmap bitmap){
        File root = Environment.getExternalStorageDirectory();
        File storeFile = new File(root,"test2.png");
        if(storeFile.exists()){
            storeFile.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(storeFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCropImagePath(){
        return Environment.getExternalStorageDirectory() + File.separator + "test2.png";
    }
}
