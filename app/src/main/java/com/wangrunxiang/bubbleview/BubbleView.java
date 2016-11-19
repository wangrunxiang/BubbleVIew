package com.wangrunxiang.bubbleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nono.android.common.helper.device.yearclass.DeviceClassUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 点亮爱心动画
 * Created by kimmy on 2016/11/4.
 */
public class BubbleView extends SurfaceView {

    private static final String TAG = "BubbleView";

    private static final long SLEEP_TIME = 50L;
    private static final long SLEEP_TIME_LOW_LEVEL_DEVICE = 100L;

    private SurfaceHolder surfaceHolder;

    /**
     * 气泡的个数
     */
    private ArrayList<Bubble> mBubbles = new ArrayList<>();
    private final Object mBubblesLock = new Object();
    private Paint mPaint;
    /**
     * 负责绘制的工作线程
     */
    private DrawThread mDrawThread;
    private final Object mDrawThreadLock = new Object();

    public BubbleView(Context context) {
        this(context, null);
    }

    public BubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setZOrderOnTop(true);
        //设置画布  背景透明
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mDrawThread == null) {
                    mDrawThread = new DrawThread();
                    mDrawThread.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mDrawThread != null) {
                    clear();
                    mDrawThread.isRun = false;
                    mDrawThread = null;
                }
                synchronized (mDrawThreadLock) {
                    mDrawThreadLock.notifyAll();
                }
            }
        });
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * 添加气泡的函数
     */
    public void addBubble(Bubble bubble) {
        synchronized (mBubblesLock) {
            mBubbles.add(bubble);
        }
        synchronized (mDrawThreadLock) {
            mDrawThreadLock.notifyAll();
        }
    }

    class DrawThread extends Thread {
        boolean isRun = true;

        @Override
        public void run() {
            super.run();
            //绘制的线程 死循环 不断的跑动
            while (isRun) {
                long frameStart = System.currentTimeMillis();

                Canvas canvas = null;
                boolean isEnd = true;
                try {
                    synchronized (mBubblesLock) {
                        canvas = surfaceHolder.lockCanvas();
                        if (canvas!=null) {
                            //清除画面
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            //对所有气泡进行遍历绘制
                            for (int i = 0; i < mBubbles.size(); i++) {
                                isEnd = mBubbles.get(i).isEnd;
                                mBubbles.get(i).draw(canvas, mPaint);
                            }
                            Iterator<Bubble> it = mBubbles.iterator();
                            while (it.hasNext()) {
                                Bubble bubble = it.next();
                                if (bubble != null && bubble.isEnd) {
                                    it.remove();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                if (isEnd) {
                    synchronized (mDrawThreadLock) {
                        try {
                            mDrawThreadLock.wait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //控制帧率
                if(isRun) {
                    try {
                        long frameDrawTime = System.currentTimeMillis() - frameStart;
                        long sleepTime = 0;
                        if(DeviceClassUtil.isHightLevelDevice()) {
                            if (frameDrawTime < SLEEP_TIME) {
                                sleepTime = SLEEP_TIME - frameDrawTime;
                            }
                        } else {
                            if (frameDrawTime < SLEEP_TIME_LOW_LEVEL_DEVICE) {
                                sleepTime = SLEEP_TIME_LOW_LEVEL_DEVICE - frameDrawTime;
                            }
                        }
                        if (sleepTime < 10) {
                            sleepTime = 10;
                        }
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            mDrawThread = null;
        }
    }

    public void clear() {
        synchronized (mBubblesLock) {
            mBubbles.clear();
        }
    }
}
