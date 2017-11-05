package com.ihandy.t2014011361.newskok;


import android.database.Cursor;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChineseNewsFetcher extends NewsFetcher {

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd',' yyyy HH:mm:ss aaa", Locale.ENGLISH);

    public ChineseNewsFetcher(Category category) {
        super(category);
        source_id = Config.CHNSOURCEID;
        Cursor cursor = TApplication.sqlHelper.getReadableDatabase().query("sources", null, "id = ?", new String[] {String.valueOf(source_id)}, null, null, null);
        cursor.moveToFirst();
        baseURL = cursor.getString(cursor.getColumnIndex("url"));
    }

    private String getFullURL(int pageNo, int pageSize) {
        return baseURL + "action/query/latest?pageNo=" + String.valueOf(pageNo) +
                "&pageSize=" + String.valueOf(pageSize) + "&category=" + category.category;
    }

    @Override
    /* request in block way */
    public Message refresh() {
        Message msg = new Message();

        int itemsRead = 0;
        Message resMsg = HttpHelper.sendHttpGetRequest(getFullURL(1, ITEMS_FIRST));
        if (resMsg.what == HttpHelper.SUCCESS) {
            try {
                JSONArray arr = new JSONObject((String)resMsg.obj).getJSONArray("list");
                for (int i = 0; i < arr.length(); ++i) {
                    JSONObject obj = arr.getJSONObject(i);
                    News nowNews = new News();
                    nowNews.category = category.category;
                    nowNews.title = obj.getString("news_Title");
                    String imgArr[];
                    if ((imgArr = obj.getString("news_Pictures").split(";")).length > 0)
                        nowNews.img = nowNews.imgurl = imgArr[0];
                    else
                        nowNews.img = nowNews.imgurl =  "";
                    nowNews.intro = obj.getString("news_Intro");
                    nowNews.origin = obj.getString("news_Author");
                    nowNews.region = "中国";
                    nowNews.source_id = Config.CHNSOURCEID;
                    String rawTime = obj.getString("news_Time");
                    try {
                        Date date = dateFormat.parse(rawTime);
                        nowNews.time = date.getTime();
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                    nowNews.news_id = obj.getString("news_ID");
                    nowNews.source = obj.getString("news_URL");

                    nowNews.liked = 0;

                    nowNews.syncWithDb();
                    boolean find = false;
                    for (News now : newsList)
                        if (now.news_id.equals(nowNews)) {
                            find = true;
                            break;
                        }
                    // guarantee no repeat
                    if (!find) {
                        ++itemsRead;
                        newsList.add(nowNews);
                    }
                }
            } catch (JSONException e) {
                msg.what = NewsFetcher.FAILED;
            }
        }

        if (itemsRead > 0) {
            msg.what = NewsFetcher.SUCCESS;
        } else {
            msg.what = NewsFetcher.FAILED;
        }

        // read from storage to compensate lack
        if (itemsRead < ITEMS_FIRST) {
            Cursor cursor = TApplication.sqlHelper.getReadableDatabase().query
                    ("NEWS", null, "category=? and source_id=?", new String[] {category.category, String.valueOf(Config.CHNSOURCEID)},
                            null, null, "fetch_time DESC", null);
            if (cursor.moveToFirst()) {
                do {
                    News nowNews = News.fromCursor(cursor);
                    boolean find = false;
                    for (News now : newsList)
                        if (now.news_id.equals(nowNews)) {
                            find = true;
                            break;
                        }
                    // guarantee no repeat
                    if (!find) {
                        ++itemsRead;
                        newsList.add(nowNews);
                    }
                } while ((itemsRead <= ITEMS_FIRST) && (cursor.moveToNext()));
            }
        }

        msg.obj = itemsRead;

        return msg;
    }

    @Override
    public Message getMore(int cnt) {
        Message msg = new Message();

        if (newsList.size() == 0) {
            msg.what = NewsFetcher.FAILED;
            return msg;
        }

        int itemsRead = 0;
        Message resMsg = HttpHelper.sendHttpGetRequest(getFullURL((newsList.size()) / cnt + 1, cnt));
        if (resMsg.what == HttpHelper.SUCCESS) {
            try {
                JSONArray arr = new JSONObject((String)resMsg.obj).getJSONArray("list");
                for (int i = 0; i < arr.length(); ++i) {
                    JSONObject obj = arr.getJSONObject(i);
                    News nowNews = new News();
                    nowNews.category = category.category;
                    nowNews.title = obj.getString("news_Title");
                    String imgArr[];
                    if ((imgArr = obj.getString("news_Pictures").split(";")).length > 0)
                        nowNews.img = imgArr[0];
                    else
                        nowNews.img = "";
                    nowNews.intro = obj.getString("news_Intro");
                    nowNews.origin = obj.getString("news_Author");
                    nowNews.region = "中国";
                    nowNews.source_id = Config.CHNSOURCEID;
                    String rawTime = obj.getString("news_Time");
                    try {
                        Date date = dateFormat.parse(rawTime);
                        nowNews.time = date.getTime();
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                    nowNews.news_id = obj.getString("news_ID");
                    nowNews.source = obj.getString("news_URL");

                    nowNews.liked = 0;

                    nowNews.syncWithDb();
                    boolean find = false;
                    for (News now : newsList)
                        if (now.news_id.equals(nowNews)) {
                            find = true;
                            break;
                        }
                    // guarantee no repeat
                    if (!find) {
                        ++itemsRead;
                        newsList.add(nowNews);
                    }
                }
            } catch (JSONException e) {
                msg.what = NewsFetcher.FAILED;
            }
        }

        msg.what = NewsFetcher.SUCCESS;
        msg.obj = itemsRead;
        return msg;
    }
}
