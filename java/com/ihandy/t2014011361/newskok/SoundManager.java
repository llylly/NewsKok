package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;

public class SoundManager {

    public static final String MENU0 = "xiaoyan";
    public static final String MENU1 = "henry";
    public static final String MENU2 = "vixm";
    public static final String MENU3 = "vixqa";
    public static final String MENU4 = "vixr";
    public static final String MENU5 = "vixk";

    static void makeSound(Context context, String categoryName, String voiceName) {
        SpeechSynthesizer synthesizer = SpeechSynthesizer.createSynthesizer(context, null);
        synthesizer.setParameter(SpeechConstant.VOICE_NAME, voiceName);
        synthesizer.setParameter(SpeechConstant.SPEED, "50");
        synthesizer.setParameter(SpeechConstant.VOLUME, "80");
        synthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        int code = synthesizer.startSpeaking(textGen(categoryName), synListener);
    }

    static private String textGen(String categoryName) {
        String s;
        Context context = TApplication.context;
        ArrayList<News> newsList = NewsHelper.getNewsList(categoryName);
        if (newsList.size() == 0) {
            s = context.getString(R.string.sync_e);
        } else {
            s = context.getString(R.string.sync0) + categoryName + context.getString(R.string.sync1);
            for (int i = 0; i < 5; ++i) {
                if (i >= newsList.size()) break;
                s += String.valueOf(i+1);
                s += context.getString(R.string.sync2);
                s += newsList.get(i).title;
                s += context.getString(R.string.sync3);
                if (newsList.get(i).origin.equals("")) {
                    // origin is null
                    s += context.getString(R.string.sync4);
                    s += context.getString(R.string.sync6);
                } else {
                    String origin = newsList.get(i).origin;
                    if ((origin.length() >= 2) &&
                            (origin.substring(0, 2).equals(context.getString(R.string.sync4)))) {
                        Log.e("", "Allo!");
                    } else
                        s += context.getString(R.string.sync4) + context.getString(R.string.sync5);
                    s += newsList.get(i).origin;
                }
                s += context.getString(R.string.sync3);
            }
        }
        return s;
    }

    static private SynthesizerListener synListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
        }
        @Override
        public void onSpeakPaused() {
        }
        @Override
        public void onSpeakResumed() {
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }
        @Override
        public void onCompleted(SpeechError error) {
            Toast.makeText(TApplication.context, TApplication.context.getString(R.string.audio_finish), Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };


}
