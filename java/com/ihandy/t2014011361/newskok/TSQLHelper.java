package com.ihandy.t2014011361.newskok;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.util.List;

public class TSQLHelper extends SQLiteOpenHelper {

    public static final int LATEST_VERSION = 34;

    static final String CREATE_SOURCES = "create table SOURCES ("
            + "id integer primary key autoincrement, "
            + "name text, "
            + "url text"
            + ")";

    static final String CREATE_CATEGORY = "create table CATEGORY ("
            + "id integer primary key autoincrement, "
            + "category text, "
            + "category_name text, "
            + "source_id integer"
            + ")";

    static final String CREATE_NEWS = "create table NEWS ("
            + "id integer primary key autoincrement, "
            + "news_id text, "
            + "title text, "
            + "category text, "
            + "source_id integer, "
            + "path text, "
            + "fetch_time bigint, "
            + "liked tinyint, "
            + "imgurl text"
            + ")";

    static final String CREATE_LIKES = "create table LIKES ("
            + "id integer primary key autoincrement, "
            + "news_id text"
            + ")";

    static final String CREATE_CONFIG = "create table CONFIG ("
            + "id integer primary key autoincrement, "
            + "key text"
            + "value text"
            + ")";

    static final String CREATE_CATEGORYNAME = "create table CATEGORYNAME ("
            + "id integer primary key autoincrement, "
            + "priority integer, "
            + "name text"
            + ")";

    static final String[] SOURCE_NAMES = new String[]
            {
                    "English News", "中文新闻"
            };

    static final String[] SOURCE_URLS = new String[]
            {
                    "http://assignment.crazz.cn/news/", "http://166.111.68.66:2042/news/"
            };

    static final String[] CHINESE_NEWS_CATEGORIES = new String[]
            {
                    "科技", "教育", "军事", "国内", "社会", "文化", "汽车", "国际", "体育", "财经", "健康", "娱乐"
            };

    static final String[] TABLE_NAMELIST = new String[]
            {
                    "SOURCES", "CATEGORY", "NEWS", "LIKES", "CONFIG", "CATEGORYNAME"
            };


    private Context context;
    public TSQLHelper(Context _context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(_context, name, cursorFactory, version);
        context = _context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SOURCES);
        db.execSQL(CREATE_CATEGORY);
        db.execSQL(CREATE_NEWS);
        db.execSQL(CREATE_LIKES);
        db.execSQL(CREATE_CONFIG);
        db.execSQL(CREATE_CATEGORYNAME);

        // loading sources
        for (int i = 0; i < SOURCE_NAMES.length; ++i) {
            ContentValues values = new ContentValues();
            values.put("name", SOURCE_NAMES[i]);
            values.put("url", SOURCE_URLS[i]);
            db.insert("SOURCES", null, values);
        }

        // loading chinese news source's categories
        for (int i = 0; i < CHINESE_NEWS_CATEGORIES.length; ++i) {
            ContentValues values = new ContentValues();
            values.put("category", String.valueOf(i+1));
            values.put("category_name", CHINESE_NEWS_CATEGORIES[i]);
            values.put("source_id", Config.CHNSOURCEID);
            db.insert("CATEGORY", null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.w("DATABASE", "upgrade from " + i + " to " + i1);
        for (int j = 0; j < TABLE_NAMELIST.length; j++)
            db.execSQL("drop table if exists " + TABLE_NAMELIST[j]);
        onCreate(db);
    }
}
