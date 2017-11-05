package com.ihandy.t2014011361.newskok;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ImageButton settingBtn;
    private ImageButton audioBtn;
    private SlidingMenu menu;

    public ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private MainPagerAdaptor mainPagerAdaptor;

    private ArrayList<NewsListView> viewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TApplication.config.savedUser == null) {
            Log.e("MainActivityy", "Restart!!!");
            Intent intent = new Intent(TApplication.context, InitActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.main);

        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.blue_shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slidingmenumain);

        settingBtn = (ImageButton)findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.showMenu();
            }
        });

        audioBtn = (ImageButton)findViewById(R.id.audioBtn);
        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionsMenu();
            }
        });

        viewPagerInit();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.broadcast.mainActivity");
        LocalBroadcastManager.getInstance(this).registerReceiver(new LocalReceiver(), intentFilter);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("noInternet", false) == true) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getResources().getString(R.string.internet_check));
            dialog.setMessage(getResources().getString(R.string.no_internet_warning));
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        }

        findViewById(R.id.tabBtn).setOnClickListener(settingListener);
        findViewById(R.id.sourceBtn).setOnClickListener(settingListener);
        findViewById(R.id.likeListBtn).setOnClickListener(settingListener);
        findViewById(R.id.aboutBtn).setOnClickListener(settingListener);

        findViewById(R.id.imgFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.imgFrame).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imgCloseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.imgFrame).setVisibility(View.GONE);
            }
        });
    }

    private void viewPagerInit() {
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        pagerTabStrip = (PagerTabStrip)viewPager.findViewById(R.id.tabStrip);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.colorTab));
        pagerTabStrip.setTextColor(getResources().getColor(R.color.colorTab));

        viewList = new ArrayList<>();

        for (String nowCate : TApplication.config.categoryNameListShowed) {
            viewList.add(new NewsListView(nowCate, this));
        }

        mainPagerAdaptor = new MainPagerAdaptor(viewList);

        viewPager.setAdapter(mainPagerAdaptor);
    }

    public void startWeb(News news, String categoryName) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("URL", news.source);
        intent.putExtra("news", news);
        intent.putExtra("categoryName", categoryName);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("newsNotify")) {
                String categoryName = intent.getStringExtra("categoryName");

                if (categoryName != null) {
                    for (NewsListView n : mainPagerAdaptor.viewList) {
                        if (n.categoryName.equals(categoryName))
                            n.listHandler.sendMessage(makeMsg(NewsListView.NOTIFY_DATA_CHANGE));
                    }
                }
            }
        }
    }

    public static Message makeMsg(int code) {
        Message msg = new Message();
        msg.what = code;
        return msg;
    }

    public View.OnClickListener settingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int type = 0;
            switch (view.getId()) {
                case R.id.tabBtn:
                    type = SettingFragment.TAB_SETTING;
                    break;
                case R.id.sourceBtn:
                    type = SettingFragment.SOURCE_SETTING;
                    break;
                case R.id.likeListBtn:
                    type = SettingFragment.LIKE_SETTING;
                    break;
                case R.id.aboutBtn:
                    type = SettingFragment.ABOUT_SETTING;
                    break;
            }
            if (type > 0) {
                menu.toggle();
                SettingFragment frag = new SettingFragment();
                final android.app.FragmentManager fragmentManager = getFragmentManager();
                Bundle args = new Bundle();
                args.putInt("type", type);
                frag.setArguments(args);
                viewPager.setVisibility(View.INVISIBLE);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.rootLayout, frag);
                transaction.setCustomAnimations(R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit);
                transaction.commit();
            }
        }
    };

    public void reloadCategory() {
        viewList.clear();
        for (String nowCate : TApplication.config.categoryNameListShowed) {
            viewList.add(new NewsListView(nowCate, this));
        }
        mainPagerAdaptor.notifyDataSetChanged();
    }

    public void showImageView(News nowNews) {
        if (!nowNews.img.equals("")) {
            try {
                Bitmap bp = BitmapFactory.decodeStream(openFileInput(nowNews.img));
                ((ImageView) findViewById(R.id.imageMainView)).setImageBitmap(bp);
                findViewById(R.id.imgFrame).setVisibility(View.VISIBLE);
            } catch(FileNotFoundException e) {}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String categoryName = viewList.get(viewPager.getCurrentItem()).categoryName;
        switch (item.getItemId()) {
            case R.id.menu0:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU0);
                break;
            case R.id.menu1:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU1);
                break;
            case R.id.menu2:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU2);
                break;
            case R.id.menu3:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU3);
                break;
            case R.id.menu4:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU4);
                break;
            case R.id.menu5:
                SoundManager.makeSound(this, categoryName, SoundManager.MENU5);
                break;
        }
        return true;
    }

}
