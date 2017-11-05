package com.ihandy.t2014011361.newskok;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;

public class News implements Serializable {

    public String category;

    public String title;
    public String img;
    public String intro;

    public String origin;
    public String region;

    public int source_id;

    public long time;
    public String news_id;
    public String source;

    public int liked;

    public String imgurl;

    public static News fromCursor(final Cursor cursor) {
        News ans = new News();
        String path = cursor.getString(cursor.getColumnIndex("path"));
        FileInputStream in = null;
        BufferedReader reader = null;
        try {
            in = TApplication.context.openFileInput(path);
            reader = new BufferedReader(new InputStreamReader(in));
            ans.category = reader.readLine();
            ans.title = reader.readLine();
            ans.img = reader.readLine();
            ans.intro = reader.readLine();
            ans.origin = reader.readLine();
            ans.region = reader.readLine();
            ans.source_id = Integer.valueOf(reader.readLine());
            ans.time = Long.valueOf(reader.readLine());
            ans.news_id = reader.readLine();
            ans.source = reader.readLine();
            ans.liked = Integer.valueOf(reader.readLine());
            ans.imgurl = reader.readLine();
        } catch (IOException e) {
            Log.e("News", "from Cursor exception at " + path);
            return null;
        }
        return ans;
    }

    public void syncWithDbWithLike() {
        title.replace('\n', ' ');
        img.replace('\n', ' ');
        intro.replace('\n', ' ');
        origin.replace('\n', ' ');
        region.replace('\n', ' ');

        SQLiteDatabase db = TApplication.sqlHelper.getReadableDatabase();
        Cursor cur = db.query("NEWS", null, "news_id=?", new String[]{news_id}, null, null, null, null);
        boolean create = false;
        String path;
        if (cur.moveToFirst()) {
            // already has one
            News fromFile = News.fromCursor(cur);
            img = fromFile.img;
            imgurl = fromFile.imgurl;

            path = news_id + ".conf";

            ContentValues values = new ContentValues();
            values.put("news_id", news_id);
            values.put("title", title);
            values.put("category", category);
            values.put("source_id", source_id);
            values.put("path", path);
            values.put("fetch_time", time);
            values.put("liked", liked);
            values.put("imgurl", imgurl);
            db.update("NEWS", values, "news_id=?", new String[]{news_id});
        } else {
            // create new record and new file
            create = true;

            if (!img.equals("")) {
                Message msg = new HttpHelper().sendPhotoRequest(img, news_id + "-image");
                if (msg.what == HttpHelper.SUCCESS) img = (String) msg.obj;
                else img = imgurl = "";
            }

            path = news_id + ".conf";

            ContentValues values = new ContentValues();
            values.put("news_id", news_id);
            values.put("title", title);
            values.put("category", category);
            values.put("source_id", source_id);
            values.put("path", path);
            values.put("fetch_time", time);
            values.put("liked", liked);
            values.put("imgurl", imgurl);
            db.insert("NEWS", null, values);
        }

        FileOutputStream out = null;
        PrintStream print = null;
        try {
            out = TApplication.context.openFileOutput(path, TApplication.context.MODE_PRIVATE);
            print = new PrintStream(out);
            print.println(category);
            print.println(title);
            if (create) {
                System.out.println(10 + 10);
            }
            print.println(img);
            print.println(intro);
            print.println(origin);
            print.println(region);
            print.println(source_id);
            print.println(time);
            print.println(news_id);
            print.println(source);
            print.println(liked);
            print.println(imgurl);
        } catch (IOException e) {
            Log.e("News", "sync with Db exception at " + path);
        } finally {
            if (print != null) print.close();
        }
        cur.close();
    }

    public void syncWithDb() {
        SQLiteDatabase db = TApplication.sqlHelper.getReadableDatabase();
        Cursor cur = db.query("NEWS", null, "news_id=?", new String[] {news_id}, null, null, null, null);
        boolean create = false;
        String path = "";
        if (cur.moveToFirst()) {
            // already has one
            try {
                create = true;
                News fromFile = News.fromCursor(cur);
                img = fromFile.img;
                imgurl = fromFile.imgurl;

                path = news_id + ".conf";

                ContentValues values = new ContentValues();
                values.put("news_id", news_id);
                values.put("title", title);
                values.put("category", category);
                values.put("source_id", source_id);
                values.put("path", path);
                values.put("fetch_time", time);
                values.put("imgurl", imgurl);

                liked = fromFile.liked;

                db.update("NEWS", values, "news_id=?", new String[] {news_id});
            } catch(Exception e) {
                create = false;
            }
        }
        // when facing exception, renew one

        if (!create) {
            // create new record and new file

            if (!img.equals("")) {
                Message msg = new HttpHelper().sendPhotoRequest(img, news_id + "-image");
                if (msg.what == HttpHelper.SUCCESS) img = (String)msg.obj; else img = "";
            }

            path = news_id + ".conf";

            ContentValues values = new ContentValues();
            values.put("news_id", news_id);
            values.put("title", title);
            values.put("category", category);
            values.put("source_id", source_id);
            values.put("path", path);
            values.put("fetch_time", time);
            values.put("liked", liked);
            values.put("imgurl", imgurl);
            db.insert("NEWS", null, values);
        }

        FileOutputStream out = null;
        PrintStream print = null;
        try {
            out = TApplication.context.openFileOutput(path, TApplication.context.MODE_PRIVATE);
            print = new PrintStream(out);
            print.println(category);
            print.println(title);
            print.println(img);
            print.println(intro);
            print.println(origin);
            print.println(region);
            print.println(source_id);
            print.println(time);
            print.println(news_id);
            print.println(source);
            print.println(liked);
            print.println(imgurl);
        } catch (IOException e) {
            Log.e("News", "sync with Db exception at " + path);
        } finally {
            if (print != null) print.close();
        }
        cur.close();
    }

    public void changeLiked() {
        if (liked == 0) {
            liked = 1;
            SQLiteDatabase db = TApplication.sqlHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("news_id", news_id);
            db.insert("LIKES", null, cv);
        } else {
            liked = 0;
            SQLiteDatabase db = TApplication.sqlHelper.getWritableDatabase();
            db.delete("LIKES", "news_id=?", new String[] {news_id});
        }
        syncWithDbWithLike();
        NewsHelper.refreshSingle(news_id);
    }

}
