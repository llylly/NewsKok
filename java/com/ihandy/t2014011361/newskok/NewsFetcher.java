package com.ihandy.t2014011361.newskok;

import android.os.Message;

import java.util.ArrayList;

abstract public class NewsFetcher {

    final public static int SUCCESS = 1;
    final public static int FAILED = 2;

    final protected static int ITEMS_FIRST = 20; //items read in refresh

    Category category;
    public  ArrayList<News> newsList = new ArrayList<>();
    int source_id;
    String baseURL;

    public NewsFetcher(Category category) {
        this.category = category;
    }
    abstract public Message refresh();
    abstract public Message getMore(int cnt);

}
