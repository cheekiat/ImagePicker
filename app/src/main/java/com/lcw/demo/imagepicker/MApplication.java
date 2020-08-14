package com.lcw.demo.imagepicker;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Create by: chenWei.li
 * Date: 2019/1/24
 * Time: 11:31 AM
 * Email: lichenwei.me@foxmail.com
 */
public class MApplication extends Application {

    private static MApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
//        Fresco.initialize(this);
    }

    public static MApplication getContext() {
        return mApplication;
    }
}
