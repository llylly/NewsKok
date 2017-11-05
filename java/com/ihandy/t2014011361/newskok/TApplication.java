package com.ihandy.t2014011361.newskok;

import android.app.Application;
import android.content.Context;
import android.util.Log;


public class TApplication extends Application {

    public static Context context;
    public static Config config;
    public static TSQLHelper sqlHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
    }

    public void initApp() {
        context = getApplicationContext();
        config = new Config();
        sqlHelper = new TSQLHelper(context,
                context.getResources().getString(R.string.database_name),
                null,
                TSQLHelper.LATEST_VERSION);
    }


}
