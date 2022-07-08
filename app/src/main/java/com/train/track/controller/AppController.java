package com.train.track.controller;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class AppController extends Application {

    @SuppressLint("StaticFieldLeak")
    private static AppController mInstance;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = AppController.this.getApplicationContext();
    }

    public Context getContext() {
        return context;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }
}
