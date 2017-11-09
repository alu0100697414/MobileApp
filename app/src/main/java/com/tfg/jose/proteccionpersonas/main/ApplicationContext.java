package com.tfg.jose.proteccionpersonas.main;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jose on 09/11/2017.
 */

public class ApplicationContext extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
