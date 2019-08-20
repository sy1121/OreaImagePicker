package com.example.yishe.oreaimagepicker.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.example.yishe.oreaimagepicker.R;

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
public class CropImageView extends AppCompatImageView {
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
        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources().getDisplayMetrics());

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.CropImageView);
        mMaskColor = a.getColor(R.styleable.CropImageView_cropMaskColor,mMaskColor);
        mBorderColor = a.getColor(R.styleable.CropImageView_cropBorderColor,mBorderColor);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.CropImageView_cropBorderWidth,mBorderWidth);
        mFocusedWidth  = a.getDimensionPixelSize(R.styleable.CropImageView_cropFocusWidth,mFocusedWidth);
        mFocusedHeight = a.getDimensionPixelSize(R.styleable.CropImageView_cropFocusHeight,mFocusedHeight);
        mDefaultStyleIndex = a.getInteger(R.styleable.CropImageView_cropStyle,mDefaultStyleIndex);
        mStyle = styles[mDefaultStyleIndex];
        a.recycle();

        //只允许图片为当前的缩放模式
        setScaleType(ScaleType.MATRIX);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        init();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        init();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        init();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        init();
    }

    /** 初始化图片和焦点框**/
    private void init(){
        Drawable d = getDrawable();
        if(!isInited || d == null) return ;
        mode = NONE;
        matrix = getImageMatrix();
        mImageWidth = mRotatedImageWidth = d.getIntrinsicWidth();
        mImageHeight = mRotatedImageHeight = d.getIntrinsicHeight();
        //计算出焦点框的中点的坐标和上下左右的xy值
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float midPointX = viewHeight / 2;
        float midPointY = viewHeight / 2;
        mFocusMidPoint = new PointF(midPointX,midPointY);

        if(mStyle == Style.CIRCLE){
            int focusSize = Math.min(mFocusedWidth,mFocusedHeight);
            mFocusedWidth = focusSize;
            mFocusedHeight = focusSize;
        }
        mFocusedRect.left = mFocusMidPoint.x - mFocusedWidth / 2;
        mFocusedRect.right = mFocusMidPoint.x + mFocusedWidth / 2;
        mFocusedRect.top = mFocusMidPoint.y - mFocusedHeight / 2;
        mFocusedRect.bottom = mFocusMidPoint.y + mFocusedHeight / 2;

        //适配焦点框的缩放比例（图片的最小边不小于焦点框的最小边）
        float fitFocusScale = getScale(mImageWidth, mImageHeight, mFocusedWidth, mFocusedHeight,true);
        mMaxScale = fitFocusScale * MAX_SCALE;
        //适配显示图片的ImageView 的缩放比例（图片至少有一边时铺满屏幕的显示的情形）
        float fitViewScale = getScale(mImageWidth, mImageHeight, viewWidth, viewHeight,false);
        //确定最终的缩放比例，在适配焦点框的前提下适配显示图片的ImageView
        //方案: 首先满足适配焦点框，如果还能适配显示图片的ImageView，则适配它，即去缩放比例的最大值
        //采取这种方案的原因：有可能图片很长或者很高，适配了ImageView的时候可能会宽/高已经小于焦点框的宽/高
        float scale = fitViewScale > fitFocusScale ? fitViewScale : fitFocusScale;
        //图像中心为中心进行缩放
        matrix.setScale(scale,scale,mImageWidth / 2, mImageHeight / 2);
        float[] mImageMatrixValues = new float[9];
        matrix.getValues(mImageMatrixValues); //获取缩放后的mImageMatrix的值
        float transX = mFocusMidPoint.x - (mImageMatrixValues[2] + mImageWidth * mImageMatrixValues[0] / 2);
        float transY = mFocusMidPoint.y - (mImageMatrixValues[5] + mImageHeight * mImageMatrixValues[4] /2);
        matrix.postTranslate(transX,transY);
        setImageMatrix(matrix);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(Style.RECTANGLE == mStyle){
            mFocusedPath.addRect(mFocusedRect, Path.Direction.CCW);
            canvas.save();
            canvas.clipRect(0,0, getWidth(), getHeight());
            canvas.clipPath(mFocusedPath, Region.Op.DIFFERENCE);
            canvas.drawColor(mMaskColor);
            canvas.restore();
        }else if(Style.CIRCLE == mStyle){
            float radius = Math.min((mFocusedRect.right - mFocusedRect.left) / 2, (mFocusedRect.bottom - mFocusedRect.top) / 2);
            mFocusedPath.addCircle(mFocusMidPoint.x, mFocusMidPoint.y, radius,Path.Direction.CCW);
            canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.clipPath(mFocusedPath, Region.Op.DIFFERENCE);
            canvas.drawColor(mMaskColor);
            canvas.restore();
        }
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setAntiAlias(true);
        canvas.drawPath(mFocusedPath, mBorderPaint);
        mFocusedPath.reset();
    }
    /**  计算边界缩放比例 isMinScale 是否最小缩放比例， true 最小缩比例  false 最大缩放比例 ***/
    private float getScale(int bitmapWidth, int bitmapHeight, int minWidth, int minHeight, boolean isMinScale){
        float scale;
        float scaleX = (float) minWidth / bitmapWidth;
        float scaleY = (float) minHeight / bitmapHeight;
        if(isMinScale){
            scale = scaleX > scaleY ? scaleX : scaleY;
        }else{
            scale = scaleX < scaleY ? scaleX : scaleY;
        }
        return scale;
    }

    /** 修正图片的缩放比**/
    private void fixScale(){
        float imageMatrixValues[] = new float[9];
        matrix.getValues(imageMatrixValues);
        float currentScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        float minScale = getScale(mRotatedImageWidth, mRotatedImageHeight, mFocusedWidth, mFocusedHeight, true);
        mMaxScale = minScale * MAX_SCALE;

        //保证图片最小时占满中间的焦点空间
        if(currentScale < minScale){
            float scale = minScale / currentScale;
            matrix.postScale(scale, scale);
        }else{
            float scale = mMaxScale / currentScale;
            matrix.postScale(scale, scale);
        }
    }

    /** 修正图片的位移**/
    private void fixTranslation(){
        RectF imageRect = new RectF(0, 0, mImageWidth, mImageHeight);
        matrix.mapRect(imageRect);
        float deltaX = 0, deltaY = 0;
        if(imageRect.left > mFocusedRect.left) {
            deltaX = -imageRect.left + mFocusedRect.left;
        }else if(imageRect.right < mFocusedRect.right){
            deltaX = -imageRect.right + mFocusedRect.right;
        }
        if(imageRect.top > mFocusedRect.top){
            deltaY = -imageRect.top + mFocusedRect.top;
        }else if(imageRect.bottom < mFocusedRect.bottom){
            deltaY = -imageRect.bottom + mFocusedRect.bottom;
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /** 获取当前图片允许的最大缩放比**/
    private float maxPostScale(){
        float imageMatixValues[] = new float[9];
        matrix.getValues(imageMatixValues);
        float curScale = Math.abs(imageMatixValues[0] + Math.abs(imageMatixValues[1]));
        return mMaxScale / curScale;
    }

    /** 计算两点之间的距离**/
    private float spacing(float x1, float y1, float x2, float y2){
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /***计算两点之间的距离**/
    private float spacing(PointF pA,PointF pB){
        return spacing(pA.x, pA.y, pB.x, pB,y);
    }

    /*** 双击触发的方法**/
    private void doubleClick(float x, float y){
        float p[] = new float[9];
        matrix.getValues(p);
        float curScale = Math.abs(p[0]) + Math.abs(p[1]);
        float minScale = getScale(mRotatedImageWidth,mRotatedImageHeight,mFocusedWidth,mFocusedHeight,true);
        if(curScale < mMaxScale){
            //每次双击的时候， 缩放加 minScale
            float toScale = Math.min(curScale + minScale, mMaxScale) / curScale;
            matrix.postScale(toScale, toScale, x, y);
        }else {
            float toScale = minScale / curScale;
            matrix.postScale(toScale, toScale, x, y);
            fixTranslation();
        }
        setImageMatrix(matrix);
    }

    /**
     *
     * @param expectWidth    期望的宽度
     * @param expectHeight   期望的高度
     * @param isSaveRectangle  是否按矩形区域保存图片
     * @return  裁剪后的Bitmap
     */
    public Bitmap getCropBitmap(int expectWidth, int expectHeight, boolean isSaveRectangle){
        if(expectWidth <= 0 || expectHeight < 0) return null;
        Bitmap srcBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        srcBitmap = rotate(srcBitmap, sumRotateLevel * 90); //最好用level,因为角度可能不是90的整数
        return makeCropBitmap(srcBitmap, mFocusedRect, getImageMatrixRect(), expectWidth, expectHeight, isSaveRectangle);
    }

    /**
     *
     * @param bitmap 要旋转的图形
     * @param degrees 旋转的角度
     * @return  旋转后的Bitmap
     */
    public Bitmap rotate(Bitmap bitmap, int degrees){
        if(degrees != 0 && bitmap != null){
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try{
                Bitmap rotateBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);;
                if(bitmap != rotateBitmap){
                    return rotateBitmap;
                }
            } catch (OutOfMemoryError ex){
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * @return  获取当前图片显示的矩形区域
     */
    private RectF getImageMatrixRect(){
        RectF rectF = new RectF();
        rectF.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }

    /**
     *
     * @param bitmap                需要裁剪的图片
     * @param focusRect             中间需要裁剪的矩形区域
     * @param imageMatrixRect       当前图片在屏幕上的显示矩形区域
     * @param expectWidth           希望获得的图片宽度，如果图片宽度不足时，拉伸图片
     * @param expectHeight          希望获得的图片高度，如果图片高度不足时，拉伸图片
     * @param isSaveRectangle       是否希望按区域保存图片
     * @return  裁剪后的图片的Bitmap
     */
    private Bitmap makeCropBitmap(Bitmap bitmap, RectF focusRect, RectF imageMatrixRect, int expectWidth, int expectHeight, boolean isSaveRectangle){
        if(imageMatrixRect == null || bitmap == null){
            return null;
        }
        float scale = imageMatrixRect.width() / bitmap.getWidth();
        int left = (int) ((focusRect.left - imageMatrixRect.left) / scale);
        int top = (int) ((focusRect.top - imageMatrixRect.top) / scale);
        int width = (int) (focusRect.width() / scale);
        int height = (int) (focusRect.height() / scale);

        if(left < 0) left = 0;
        if(top < 0) top = 0;
        if(left + width > bitmap.getWidth()) width = bitmap.getWidth() - left;
        if(top + height > bitmap.getHeight()) height = bitmap.getHeight() - top;

        try{
            bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            if(expectWidth != width || expectHeight != height){
                bitmap = Bitmap.createScaledBitmap(bitmap, expectWidth, expectHeight, true);
                if(mStyle == Style.CIRCLE && !isSaveRectangle){
                   //如果时圆形，就将土拍你裁剪成圆的
                   int length = Math.min(expectWidth, expectHeight);
                   int radius  = length / 2;
                   Bitmap circleBitmap = Bitmap.createBitmap(length,left,Bitmap.Config.ARGB_8888);
                   Canvas canvas = new Canvas(circleBitmap);
                   BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                   Paint paint = new Paint();
                   paint.setShader(bitmapShader);
                   canvas.drawCircle(expectWidth / 2f, expectHeight / 2f, radius, paint);
                   bitmap = circleBitmap;
                }
            }
        }catch(OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
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
