package com.ihandy.t2014011361.newskok;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.maxwin.view.XListView;

public class NewsListView extends FrameLayout implements XListView.IXListViewListener {

    final public static int REFRESH_FINISH = 1;
    final public static int LOADMORE_FINISH = 2;
    final public static int REFRESH_NOUSE = 3;
    final public static int LOADMORE_NOUSE = 4;
    final public static int NOTIFY_DATA_CHANGE = 5;

    public String categoryName;
    private MainActivity context;

    private String refreshTimeStr;
    private XListView listView;
    private NewsListAdapter newsListAdapter;

    private ArrayList<News> newsList;

    public NewsListView(String categoryName, MainActivity context) {
        super(context);
        this.categoryName = categoryName;
        this.context = context;
        viewInit();
    }

    public NewsListView(String categoryName, MainActivity context, AttributeSet attrs) {
        super(context, attrs);
        this.categoryName = categoryName;
        this.context = context;
        viewInit();
    }

    private void viewInit() {
        refreshTimeStr = context.getResources().getString(R.string.not_refresh_yet);

        LayoutInflater.from(context).inflate(R.layout.list_page, this);
        listView = (XListView)findViewById(R.id.listView);
        listView.setRefreshTime(refreshTimeStr);
        listView.setPullLoadEnable(true);

        newsList = NewsHelper.getNewsList(categoryName);
        if (newsList.size() == 0) {
            newsList = new ArrayList<>();
            newsList.add(new EmptyNews());
        }
        newsListAdapter = new NewsListAdapter(context, R.layout.list_item, newsList);
        listView.setAdapter(newsListAdapter);
        listView.setXListViewListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                i--;
                News news = newsList.get(i);
                if (!(news instanceof EmptyNews))
                    ((MainActivity)context).startWeb(news, categoryName);
            }
        });
    }

    public Handler listHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_NOUSE:
                    Toast toast0 = Toast.makeText(context, TApplication.context.getResources().getString(R.string.refresh_failed), Toast.LENGTH_SHORT);
                    toast0.show();
                    break;
                case LOADMORE_NOUSE:
                    Toast toast1 = Toast.makeText(context, TApplication.context.getResources().getString(R.string.loadmore_failed), Toast.LENGTH_SHORT);
                    toast1.show();
                    break;
                case REFRESH_FINISH:
                    newsListAdapter.notifyDataSetInvalidated();
                    newsListAdapter.clear();
                    if (newsList.size() == 0) {
                        newsList = new ArrayList<>();
                        newsList.add(new EmptyNews());
                    }
                    newsListAdapter.newsList = newsList;
                    newsListAdapter.addAll(newsListAdapter.newsList);
                    newsListAdapter.notifyDataSetChanged();

                    listView.stopRefresh();
                    refreshTimeStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    listView.setRefreshTime(refreshTimeStr);
                    break;
                case LOADMORE_FINISH:
                    newsListAdapter.notifyDataSetInvalidated();
                    newsListAdapter.clear();
                    if (newsList.size() == 0) {
                        newsList = new ArrayList<>();
                        newsList.add(new EmptyNews());
                    }
                    newsListAdapter.newsList = newsList;
                    newsListAdapter.addAll(newsListAdapter.newsList);
                    newsListAdapter.notifyDataSetChanged();

                    listView.stopLoadMore();
                    break;
                case NOTIFY_DATA_CHANGE:
                    Log.e("NewsListView", "newsNotifyActivity");
                    newsListAdapter.notifyDataSetInvalidated();
                    newsListAdapter.clear();
                    newsList = NewsHelper.getNewsList(categoryName);
                    if (newsList.size() == 0) {
                        newsList = new ArrayList<>();
                        newsList.add(new EmptyNews());
                    }
                    newsListAdapter.newsList = newsList;
                    newsListAdapter.addAll(newsListAdapter.newsList);
                    newsListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String preId = "";
                if (!(newsList.get(0) instanceof EmptyNews))
                    preId = newsList.get(0).news_id;
                NewsHelper.refresh(categoryName);
                newsList = NewsHelper.getNewsList(categoryName);
                if ((newsList.size() == 0) || (preId.equals(newsList.get(0).news_id))) {
                    listHandler.sendMessage(makeMsg(REFRESH_NOUSE));
                }
                listHandler.sendMessage(makeMsg(REFRESH_FINISH));
            }
        }).start();
    }

    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int preSize = newsList.size();
                NewsHelper.getMore(categoryName);
                newsList = NewsHelper.getNewsList(categoryName);
                if (newsList.size() == 0) {
                    newsList = new ArrayList<>();
                    newsList.add(new EmptyNews());
                }
                if (newsList.size() <= preSize) {
                    listHandler.sendMessage(makeMsg(LOADMORE_NOUSE));
                }
                listHandler.sendMessage(makeMsg(LOADMORE_FINISH));
            }
        }).start();
    }

    public static Message makeMsg(int code) {
        Message msg = new Message();
        msg.what = code;
        return msg;
    }

}
