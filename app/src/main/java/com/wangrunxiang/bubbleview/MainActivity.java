package com.wangrunxiang.bubbleview;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BubbleView mBv;
    private Button mBtAddBubble;
    private String[] mBubbleUrls = {"http://ofl87y8j7.bkt.clouddn.com/icon_love_banana.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_cake.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_candy.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_lemon.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_lollipop.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_pineapple.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_popsicle.png",
            "http://ofl87y8j7.bkt.clouddn.com/icon_love_watermelon.png"};

    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBv = (BubbleView) findViewById(R.id.bv);
        mBtAddBubble = (Button) findViewById(R.id.bt_add_bubble);
        mBtAddBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBubble();
            }
        });
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addBubble();
                    }
                });
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 1000, 100);
    }

    private void addBubble() {
        Picasso.with(MainActivity.this).load(mBubbleUrls[new Random().nextInt(7)]).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mBv.addBubble(new Bubble(bitmap, mBv));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
    }
}
