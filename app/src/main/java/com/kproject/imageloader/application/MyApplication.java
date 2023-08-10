package com.kproject.imageloader.application;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static MyApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static MyApplication getContext() {
        return context;
    }

}
