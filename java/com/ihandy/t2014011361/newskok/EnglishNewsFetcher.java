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

public class EnglishNewsFetcher extends NewsFetcher {

    public EnglishNewsFetcher(Category category) {
        super(category);
        source_id = Config.ENGSOURCEID;
        Cursor cursor = TApplication.sqlHelper.getReadableDatabase().query("sources", null, "id = ?", new String[] {String.valueOf(source_id)}, null, null, null);
        cursor.moveToFirst();
        baseURL = cursor.getString(cursor.getColumnIndex("url"));
    }

    @Override
    /* request in block way */
    public Message refresh() {
        Message msg = new Message();

        String generalURL = baseURL + "query?locale=en&" + "category=" + category.category;
        String additional = "";
        if (category.category.equals("top_stories"))
            Log.w(EnglishNewsFetcher.class.getName(), category.category);

        int itemsRead = 0;
        newsList.clear();

        do {
            Message resMsg = HttpHelper.sendHttpGetRequest(generalURL + additional);
            if (resMsg.what == HttpHelper.FAILED) {
                break;
            } else {
                try {
                    JSONObject rootObj = new JSONObject((String)resMsg.obj);
                    int code = rootObj.getJSONObject("meta").getInt("code");
                    if (!rootObj.has("data"))
                        break;
                    if (code != 200) {
                        break;
                    } else {
                        JSONArray newsArr = rootObj.getJSONObject("data").getJSONArray("news");
                        if (newsArr.length() == 0) break;
                        for (int i = 0; i < newsArr.length(); i++) {
                            JSONObject obj = newsArr.getJSONObject(i);
                            News nowNews = new News();
                            nowNews.category = category.category;
                            nowNews.title = obj.getString("title");
                            if (obj.getJSONArray("imgs").length() > 0)
                                nowNews.img = nowNews.imgurl = obj.getJSONArray("imgs").getJSONObject(0).getString("url");
                            else
                                nowNews.img = nowNews.imgurl = "";
                            nowNews.intro = "";
                            nowNews.origin = obj.getString("origin");
                            nowNews.region = obj.getString("country");
                            nowNews.source_id = Config.ENGSOURCEID;
                            try {
                                nowNews.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("updated_time")).getTime();
                            } catch (ParseException e) {
                                try {
                                    nowNews.time = obj.getLong("fetched_time");
                                } catch (Exception ee) {
                                    nowNews.time = 0;
                                }
                            }
                            nowNews.news_id = String.valueOf(obj.getLong("news_id"));
                            if (obj.isNull("source"))
                                nowNews.source = "";
                            else
                                nowNews.source = obj.getJSONObject("source").getString("url");
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
                    }
                    String lastId;
                    if (rootObj.getJSONObject("data").has("next_id")) {
                        lastId = String.valueOf(rootObj.getJSONObject("data").getLong("next_id"));
                        additional = "&max_news_id=" + lastId;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } while (itemsRead < ITEMS_FIRST);

        if (itemsRead > 0) {
            msg.what = NewsFetcher.SUCCESS;
        } else {
            msg.what = NewsFetcher.FAILED;
        }

        // read from storage to compensate lack
        if (itemsRead < ITEMS_FIRST) {
            Cursor cursor = TApplication.sqlHelper.getReadableDatabase().query
                    ("NEWS", null, "category=? and source_id=?", new String[] {category.category, String.valueOf(Config.ENGSOURCEID)},
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
            msg.what = FAILED;
            return msg;
        }

        long minTime = new Date().getTime();
        String startNewsId = "";
        for (News n : newsList)
            if (n.time < minTime) {
                minTime = n.time;
                startNewsId = n.news_id;
            }

        String generalURL = baseURL + "query?locale=en&" + "category=" + category.category;
        String additional = "&max_news_id=" + startNewsId;

        int itemsRead = 0;

        do {
            Message resMsg = HttpHelper.sendHttpGetRequest(generalURL + additional);
            if (resMsg.what == HttpHelper.FAILED) {
                break;
            } else {
                try {
                    JSONObject rootObj = new JSONObject((String)resMsg.obj);
                    int code = rootObj.getJSONObject("meta").getInt("code");
                    if (!rootObj.has("data"))
                        break;
                    if (code != 200) {
                        break;
                    } else {
                        JSONArray newsArr = rootObj.getJSONObject("data").getJSONArray("news");
                        if (newsArr.length() == 0) break;
                        for (int i = 0; i < newsArr.length(); i++) {
                            JSONObject obj = newsArr.getJSONObject(i);
                            News nowNews = new News();
                            nowNews.category = category.category;
                            nowNews.title = obj.getString("title");
                            if (obj.getJSONArray("imgs").length() > 0)
                                nowNews.img = obj.getJSONArray("imgs").getJSONObject(0).getString("url");
                            else
                                nowNews.img = "";
                            nowNews.intro = "";
                            nowNews.origin = obj.getString("origin");
                            nowNews.region = obj.getString("country");
                            nowNews.source_id = Config.ENGSOURCEID;
                            try {
                                nowNews.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(obj.getString("updated_time")).getTime();
                            } catch (ParseException e) {
                                try {
                                    nowNews.time = obj.getLong("fetched_time");
                                } catch (Exception ee) {
                                    nowNews.time = 0;
                                }
                            }
                            nowNews.news_id = String.valueOf(obj.getLong("news_id"));
                            if (obj.isNull("source"))
                                nowNews.source = "";
                            else
                                nowNews.source = obj.getJSONObject("source").getString("url");
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
                    }
                    String lastId;
                    if (rootObj.getJSONObject("data").has("next_id")) {
                        lastId = String.valueOf(rootObj.getJSONObject("data").getLong("next_id"));
                        additional = "&max_news_id=" + lastId;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } while (itemsRead < cnt);

        msg.what = NewsFetcher.SUCCESS;
        msg.obj = itemsRead;
        return msg;
    }
}
