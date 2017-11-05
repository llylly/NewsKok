package com.ihandy.t2014011361.newskok;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NewsHelper {

    static public int REFRESH_ITEMS_CNT = 20;
    static public int MORE_ITEMS_CNT = 20;

    static public List<NewsFetcher> fetcherList;

    static public Map<String, ArrayList<News>> grossNewsList = new HashMap<>();

    public static void initFetch() {
        fetcherList = new ArrayList<>();
        for (String now : TApplication.config.categoryNameListShowed) {
            for (Integer nowCateItem : TApplication.config.categories.keySet())
                if (TApplication.config.categories.get(nowCateItem).category_name.equals(now)) {
                    if (TApplication.config.categories.get(nowCateItem).source_id == Config.ENGSOURCEID)
                        fetcherList.add(new EnglishNewsFetcher(TApplication.config.categories.get(nowCateItem)));
                    if (TApplication.config.categories.get(nowCateItem).source_id == Config.CHNSOURCEID)
                        fetcherList.add(new ChineseNewsFetcher(TApplication.config.categories.get(nowCateItem)));
                }
        }
        List<Thread> fetcherPool = new ArrayList<>();
        for (final NewsFetcher nowFetcher : fetcherList) {
            fetcherPool.add(new Thread(new Runnable() {
                @Override
                public void run() {nowFetcher.refresh();
                }
            }));
        }
        for (Thread t : fetcherPool) t.start();
        for (Thread t : fetcherPool)
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        fetcherPool.clear();
    }

    public static ArrayList<News> getNewsList(String categoryName) {
        if (grossNewsList.containsKey(categoryName) && grossNewsList.get(categoryName).size() > 0)
            return grossNewsList.get(categoryName);

        List<Category> realCategoryList = new ArrayList<>();
        for (Integer i : TApplication.config.categories.keySet()) {
            Category now = TApplication.config.categories.get(i);
            if (now.category_name.equals(categoryName) &&
                    (TApplication.config.categoryNameListShowed.contains(categoryName)) &&
                    (!TApplication.config.unabledSources.contains(TApplication.config.sources.get(now.source_id).name)))
                realCategoryList.add(now);
        }
        if (realCategoryList.size() == 0)
            return new ArrayList<>();
        String selectString = "";
        for (int i = 0; i < realCategoryList.size(); i++) {
            if (i > 0) selectString += " OR ";
            selectString += "(category=? AND source_id=?)";
        }
        String[] selectParams = new String[realCategoryList.size() << 1];
        for (int i = 0; i < realCategoryList.size(); i++) {
            selectParams[i << 1] = realCategoryList.get(i).category;
            selectParams[(i << 1) + 1] = String.valueOf(realCategoryList.get(i).source_id);
        }
        Cursor cursor = TApplication.sqlHelper.getReadableDatabase().query
                ("NEWS", null, selectString, selectParams, null, null, "fetch_time DESC");


        grossNewsList.put(categoryName, new ArrayList<News>());
        ArrayList<News> nowList = grossNewsList.get(categoryName);
        nowList.clear();
        if (cursor.moveToFirst()) {
            do {
                nowList.add(News.fromCursor(cursor));
            } while ((nowList.size() < REFRESH_ITEMS_CNT) && (cursor.moveToNext()));
        }
        cursor.close();
        return nowList;
    }

    public static void refreshSingle(String news_id) {
        for (ArrayList<News> list : grossNewsList.values()) {
            if ((list != null) && (list.size() > 0))
                for (News nowNews : list)
                    if (nowNews.news_id.equals(news_id)) {
                        Cursor cur = TApplication.sqlHelper.getReadableDatabase().query
                                ("NEWS", null, "news_id=?", new String[] {news_id}, null, null, null, null);
                        if (cur.moveToFirst())
                            nowNews = News.fromCursor(cur);
                        cur.close();
                    }
        }
    }

    /* in block way & need time! */
    public static void refresh(String categoryName) {
        List<Category> realCategoryList = new ArrayList<>();
        for (Integer i : TApplication.config.categories.keySet()) {
            Category now = TApplication.config.categories.get(i);
            if (now.category_name.equals(categoryName) &&
                    (TApplication.config.categoryNameListShowed.contains(categoryName)) &&
                    (!TApplication.config.unabledSources.contains(TApplication.config.sources.get(now.source_id).name)))
                realCategoryList.add(now);
        }
        if (realCategoryList.size() == 0)
            return;

        for (Category now : realCategoryList) {
            boolean exists = false;
            for (NewsFetcher fetcher : fetcherList)
                if (fetcher.category.equals(now)) {
                    exists = true;
                    break;
                }
            if (!exists) {
                if (now.source_id == Config.ENGSOURCEID)
                    fetcherList.add(new EnglishNewsFetcher(now));
                if (now.source_id == Config.CHNSOURCEID)
                    fetcherList.add(new ChineseNewsFetcher(now));
            }
        }

        List<Thread> fetcherPool = new ArrayList<>();
        for (final NewsFetcher now : fetcherList)
            if (realCategoryList.contains(now.category)) {
                fetcherPool.add(new Thread(new Runnable() {
                    @Override
                    public void run() {now.refresh();
                    }
                }));
            }

        for (Thread t : fetcherPool) t.start();
        for (Thread t : fetcherPool)
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        grossNewsList.get(categoryName).clear();
    }

    /* in block way & need time! */
    public static void getMore(String categoryName) {
        List<Category> realCategoryList = new ArrayList<>();
        for (Integer i : TApplication.config.categories.keySet()) {
            Category now = TApplication.config.categories.get(i);
            if (now.category_name.equals(categoryName) &&
                    (TApplication.config.categoryNameListShowed.contains(categoryName)) &&
                    (!TApplication.config.unabledSources.contains(TApplication.config.sources.get(now.source_id).name)))
                realCategoryList.add(now);
        }
        if (realCategoryList.size() == 0)
            return;

        List<Thread> fetcherPool = new ArrayList<>();
        for (final NewsFetcher now : fetcherList)
            if (realCategoryList.contains(now.category)) {
                fetcherPool.add(new Thread(new Runnable() {
                    @Override
                    public void run() {now.getMore(NewsHelper.MORE_ITEMS_CNT);
                    }
                }));
            }
        for (Thread t : fetcherPool) t.start();
        for (Thread t : fetcherPool)
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        ArrayList<News> nowList = new ArrayList<>();
        for (News news : getNewsList(categoryName)) {
            nowList.add(news);
        }
        grossNewsList.put(categoryName, nowList);
        if (nowList.size() == 0) return;
        int aimSize = nowList.size() + NewsHelper.MORE_ITEMS_CNT;
        long lastTime = nowList.get(nowList.size() - 1).time;

        String selectString = "";
        for (int i = 0; i < realCategoryList.size(); i++) {
            if (i > 0) selectString += " OR ";
            selectString += "(category=? AND source_id=?)";
        }
        String[] selectParams = new String[1 + (realCategoryList.size() << 1)];
        selectParams[0] = String.valueOf(new Date().getTime());
        //String.valueOf(lastTime);
        for (int i = 0; i < realCategoryList.size(); i++) {
            selectParams[(i << 1) + 1] = realCategoryList.get(i).category;
            selectParams[(i << 1) + 2] = String.valueOf(realCategoryList.get(i).source_id);
        }
        Cursor cur = TApplication.sqlHelper.getReadableDatabase().query
                ("NEWS", null, "(fetch_time <= ?) AND (" + selectString + ")", selectParams, null, null, "fetch_time DESC", null);

        if (cur.moveToFirst()) {
            do {
                long thisTime = cur.getLong(cur.getColumnIndex("fetch_time"));
                boolean repeat = false;
                //if (thisTime == lastTime) {
                    String thisId = cur.getString(cur.getColumnIndex("news_id"));
                    for (News n : nowList)
                        if (n.news_id.equals(thisId)) {
                            repeat = true;
                            break;
                        }
                //}
                if (!repeat)
                    nowList.add(News.fromCursor(cur));
            } while ((nowList.size() < aimSize) && (cur.moveToNext()));
            Log.e("nowList", nowList.size() + " ");
            Log.e("gross", getNewsList(categoryName).size() + " ");
            cur.close();
        }
    }

}
