package com.example.listview.Login_Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.listview.R;

/*SplashScreen: Activity. Si prevede:
* 1) Metodo che imposti la durata dello SplashScreen e lanci la pagina inizale "MainActivity"*/

public class SplashScreen extends Activity {

    private static final String TAG_LOG= SplashScreen.class.getName();
    private static final long MIN_WAIT_INTERVAL=1500L;
    private static final long MAX_WAIT_INTERVAL=3000L;
    private static final int GO_AHEAD_WHAT = 1;
    private long mStartTime;
    private boolean mIsDone;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case GO_AHEAD_WHAT:
                    long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                    if(elapsedTime>=MIN_WAIT_INTERVAL && !mIsDone){
                        mIsDone=true;
                        goAhead();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStartTime=SystemClock.uptimeMillis();
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage, mStartTime+MAX_WAIT_INTERVAL);
        Log.d(TAG_LOG, "Handerl message sent!");
    }

    private void goAhead(){
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
