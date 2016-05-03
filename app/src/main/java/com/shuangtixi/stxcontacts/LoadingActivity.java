package com.shuangtixi.stxcontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        TimerTask task = new TimerTask() {
            public void run() {

                //execute the task
                Intent i=new Intent();
                i.putExtra("LoadingState",MainActivity.LOADING_SUCCESS);
                LoadingActivity.this.setResult(MainActivity.LOADING_STATE,i);
                LoadingActivity.this.finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 3000);
    }

    @Override
    public void onBackPressed() {

    }
}
