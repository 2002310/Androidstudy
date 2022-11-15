package com.example.android_study.application;

import android.app.Application;

import com.example.android_study.common.config.Global;

public class SmartApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initGlobal();
    }

    public void initGlobal(){
            try {
                Global.LOCAL_VERSION = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
                Global.SERVICE_VERSION = 1;
            }catch (Exception ex){
                ex.printStackTrace();
            }
    }
}
