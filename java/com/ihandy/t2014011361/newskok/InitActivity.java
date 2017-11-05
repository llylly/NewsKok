package com.ihandy.t2014011361.newskok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class InitActivity extends Activity {

    private TextView statusView;
    public static boolean noInternetShowed = false;
    public static boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.init_layout);
        statusView = (TextView)findViewById(R.id.status);
        AppInit.appInit(handler);
        if (finished)
            findViewById(R.id.enterMain).setVisibility(View.VISIBLE);
        (findViewById(R.id.enterMain)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityStart();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (finished)
            mainActivityStart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (finished)
            mainActivityStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        finish();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppInit.INTERNET_CHECK:
                    statusView.setText(R.string.internet_check);
                    break;
                case AppInit.NO_INTERNET_CONFIRM:
                    noInternetShowed = true;
                    break;
                case AppInit.CONFIG_START:
                    statusView.setText(R.string.loading_config);
                    break;
                case AppInit.CONFIG_FINISH:
                    statusView.setText(R.string.finish_config);
                    break;
                case AppInit.NEWS_START:
                    statusView.setText(R.string.fetch_news);
                    break;
                case AppInit.NEWS_FINISH:
                    statusView.setText(R.string.finish_news);
                    break;
                case AppInit.FINISH:
                    statusView.setText(R.string.finish_loading);
                    finished = true;
                    mainActivityStart();
                    break;
            }
        }

    };

    private void mainActivityStart() {
        Intent intent = new Intent(TApplication.context, MainActivity.class);
        intent.putExtra("noInternet", noInternetShowed);
        startActivity(intent);
        finish();
    }

}
