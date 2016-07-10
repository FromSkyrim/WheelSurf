package com.fatty.surfaceviewwheelsurf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by 17255 on 2016/7/9.
 */
public class WheelSurf extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    //盘块的奖项名字
    private String[] mStrs = new String[]{"单反相机", "iPad", "恭喜发财", "iPhone", "妹子一只", "恭喜发财"};
    //奖项的图片
    private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad,
            R.drawable.f040, R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
    //奖项图片的Bitmap数组
    private Bitmap[] mImgsBitmap;
    //背景的Bitmap图片
    Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
    //盘块的颜色
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};
    //奖项的数目
    private int mItemCount = 6;
    //字体的大小
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
            getResources().getDisplayMetrics());


    //画盘块的画笔
    private Paint mArcPaint;
    //绘制文本的画笔
    private Paint mTextPaint;


    //整个盘块的范围
    private RectF mRange = new RectF();
    //整个盘块的直径
    private int mRadius;
    //转盘的中心位置
    private int mCenter;
    //padding
    private int mPadding;


    //盘块旋转的速度
    private double mSpeed;
    private volatile float mStartAngle = 0;
    //判断是否点击了停止按钮
    private boolean isEnding;


    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    //用于绘制线程
    private Thread t;

    //线程的控制开关
    private boolean isRunning;

    //盘块变慢的速度
    private double slowDownSpeed = 0.03;


    public WheelSurf(Context context) {
        this(context, null);
    }

    public WheelSurf(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();

        mHolder.addCallback(this);


        //可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);


        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredHeight(), getMeasuredWidth());

        mPadding = getPaddingLeft();
        mRadius = width - mPadding * 2;
        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //初始化绘制盘块的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        //初始化绘制字体的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        //初始化绘制盘块的范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);

        //初始化图片
        mImgsBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        isRunning = true;

        t = new Thread(this);
        t.start();
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        isRunning = false;
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public void run() {
        //不断进行绘制
        while (isRunning) {
//            long start = System.currentTimeMillis();
//            draw();
//            long end = System.currentTimeMillis();
//
//            if ((end - start) < 50) {
//                try {
//                    Thread.sleep(50 - (end - start));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            draw();
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();

            if (mCanvas != null) {
                //绘制背景
                drawBg();

                //绘制盘块
                drawWheel();

            }
        } catch (Exception e) {
        } finally {
            //释放资源
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    //分色块绘制转盘，绘制转盘上的文本
    private void drawWheel() {
        float tmpAngle = mStartAngle;
        float sweepAngle = 360 / mItemCount;

        for (int i = 0; i < mItemCount; i++) {
            //绘制色块
            mArcPaint.setColor(mColors[i]);
            mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);

            //绘制文本
            drawText(tmpAngle, sweepAngle, mStrs[i]);

            //绘制图标
            drawIcon(tmpAngle, mImgsBitmap[i]);

            tmpAngle += sweepAngle;
        }

        mStartAngle += mSpeed;
        //判断是否按下了停止按钮
        if (isEnding) {
            mSpeed -= slowDownSpeed;
        }
        if (mSpeed <= 0) {
            mSpeed = 0;
            isEnding = false;
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度为直径的1/7
        int imgWidth = mRadius / 7;

        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 3.5 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 3.5 * Math.sin(angle));

        //确定图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);

        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    private void drawText(float tmpAngle, float sweepAngle, String mStr) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);

        //水平偏移量
        float textWidth = mTextPaint.measureText(mStr);
        float hOffset = (float) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);

        //垂直偏移量
        float vOffset = mRadius / 2 / 6;

        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);
    }

    //绘制背景
    private void drawBg() {
        mCanvas.drawColor(0xFFFFFFFF);
        mCanvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2,
                getMeasuredHeight() - mPadding / 2), null);
    }

    //点击启动旋转,index控制最终获奖的奖品
    public void wheelSurfStart(int index) {
        //计算每一项的角度
        float angle = 360 / mItemCount;

        //计算每一项的中奖范围
        float from = 270 - (index + 1) * angle;
        float end = from + angle;

        //设置停下来需要旋转的距离
        float targetFrom = 10 * 360 + from;
        float targetEnd = 10 * 360 + end;

        /*
        * v1 -> 0
        *
        * v1 * (v1 / slowDownSpeed) / 2 = targetFrom
        * v1 * v1 = targetFrom * 2 * 0.03
        * v1 = Math.sqrt(targetFrom * 2 * 0.03)
        * */

        float v1 = (float) Math.sqrt(targetFrom * 2 * 0.03);
        float v2 = (float) Math.sqrt(targetEnd * 2 * 0.03);

        mSpeed = v1 + Math.random() * (v2 - v1);
        isEnding = false;
    }

    //点击开始停止旋转
    public void wheelSurfStop() {
        mStartAngle = 0;
        isEnding = true;

    }

    //转盘是否还在旋转
    public boolean stillTurning() {
        return mSpeed != 0;
    }

    public boolean isEnding() {
        return isEnding;
    }
}
