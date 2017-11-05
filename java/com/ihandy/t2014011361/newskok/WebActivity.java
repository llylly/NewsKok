package com.ihandy.t2014011361.newskok;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class WebActivity extends Activity {

    public static final String APP_CACHE_DIRNAME = "web_cache/";

    private WebView webView;
    private ImageButton backBtn;
    private ImageButton shareBtn;
    private ImageButton commentBtn;
    private ProgressBar progressBar;

    private String url;
    private News news;
    private String categoryName;

    private ImageButton star;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("WebActivity", "WebActivity create");

        Intent intent = getIntent();
        boolean legalCreate = true;
        if ((url = intent.getStringExtra("URL")) == null)
            legalCreate = false;
        if ((news = (News)intent.getSerializableExtra("news")) == null)
            legalCreate = false;
        if ((categoryName = intent.getStringExtra("categoryName")) == null)
            legalCreate = false;
        news.syncWithDb();

        if (!legalCreate) finish();

        setContentView(R.layout.web_activity);
        backBtn = (ImageButton)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        setWebView();

        star = (ImageButton)findViewById(R.id.starBtn);
        initStarStyle();
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                news.changeLiked();
                initStarStyle();
                if (news.liked != 0)
                    Toast.makeText(WebActivity.this, WebActivity.this.getString(R.string.liked_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(WebActivity.this, WebActivity.this.getString(R.string.unliked_toast), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent("com.broadcast.mainActivity");
                intent.putExtra("newsNotify", true);
                intent.putExtra("categoryName", categoryName);
                LocalBroadcastManager.getInstance(WebActivity.this).sendBroadcast(intent);
            }
        });

        shareBtn = (ImageButton)findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShare();
            }
        });

        commentBtn = (ImageButton)findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentToggle();
            }
        });
    }

    private void setWebView() {
        webView = (WebView)findViewById(R.id.webView);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        // By default, there is no javascript support or zoom controls, so
        // turn both of those on.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDisplayZoomControls(false);

        // We want links to continue opening inside the app.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("WEBVIEW", "loadUrl="+url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                Log.d("WEBVIEW", "onPageStarted="+url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("WEBVIEW", "onPageFinished="+url);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String url) {
                Log.d("WEBVIEW", "onPageError="+url+"::descr="+description);
            }
        });

        // Try to get YouTube videos working.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.e("WebActivity", "Progress " + newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100)
                    progressBar.setVisibility(View.GONE);
                else
                    progressBar.setVisibility(View.VISIBLE);
                super.onProgressChanged(view, newProgress);
            }
        });

        String cacheDirPath = getFilesDir().getAbsolutePath()+APP_CACHE_DIRNAME;
        //      String cacheDirPath = getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;
        Log.e(WebActivity.class.getName(), "cacheDirPath="+cacheDirPath);
        //设置数据库缓存路径
        webView.getSettings().setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webView.getSettings().setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        webView.getSettings().setAppCacheEnabled(true);

        webView.loadUrl(url);
    }

    private void initStarStyle() {
        if (news.liked == 0)
            star.setImageDrawable(getResources().getDrawable(R.drawable.news_star_uncheck_btn));
        else
            star.setImageDrawable(getResources().getDrawable(R.drawable.news_star_check_btn));
    }

    private void commentToggle() {
        CommentFragment frag = new CommentFragment();
        final android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.rootLayout, frag);
        Bundle bundle = new Bundle();
        bundle.putSerializable("news", news);
        frag.setArguments(bundle);
        transaction.setCustomAnimations(R.animator.fragment_slide_buttom_enter,
                R.animator.fragment_slide_buttom_exit);
        transaction.commit();
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        //oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        //oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(news.title + "\n" + news.source + "\n" + news.intro);
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        if (!news.imgurl.equals(""))
            oks.setImageUrl(news.imgurl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        if (!news.img.equals(""))
            oks.setImagePath(getFileStreamPath(news.img).getAbsolutePath());//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(news.source);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(news.origin);
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(news.source);

        // 启动分享GUI
        oks.show(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK();
    }

}
