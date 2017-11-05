package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.apache.http.protocol.HTTP;

public class AppInit {

    final public static int INTERNET_CHECK = 1;
    final public static int NO_INTERNET_CONFIRM = 2;
    final public static int CONFIG_START = 3;
    final public static int CONFIG_FINISH = 4;
    final public static int NEWS_START = 5;
    final public static int NEWS_FINISH = 6;
    final public static int FINISH = 7;

    public static boolean inited = false;
    private static Context context;
    private static TSQLHelper sqlHelper;

    public static void appInit(final Handler activityHandler) {
        if (inited) return; else inited = true;
        context = TApplication.context;
        new Thread(new Runnable() {

            @Override
            public void run() {

                activityHandler.sendMessage(makeMsg(INTERNET_CHECK));
                TApplication.config.isInternet = HttpHelper.checkInternet();
                if (!TApplication.config.isInternet)
                    activityHandler.sendMessage(makeMsg(NO_INTERNET_CONFIRM));

                activityHandler.sendMessage(makeMsg(CONFIG_START));

                sqlHelper = TApplication.sqlHelper;
                TApplication.config.init();

                //init xunfei
                SpeechUtility.createUtility(TApplication.context, SpeechConstant.APPID + TApplication.context.getString(R.string.xunfei_appid));

                activityHandler.sendMessage(makeMsg(CONFIG_FINISH));

                if (TApplication.config.isInternet)
                    activityHandler.sendMessage(makeMsg(NEWS_START));

                NewsHelper.initFetch();
                activityHandler.sendMessage(makeMsg(NEWS_FINISH));

                activityHandler.sendMessage(makeMsg(FINISH));

            }
        }).start();
    }

    private static Message makeMsg(int x) {
        Message msg = new Message();
        msg.what = x;
        return msg;
    }

}
