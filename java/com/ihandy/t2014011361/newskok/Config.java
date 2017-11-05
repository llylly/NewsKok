package com.ihandy.t2014011361.newskok;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import java.util.*;

public class Config {

    final public static int ENGSOURCEID = 1;
    final public static int CHNSOURCEID = 2;

    final public static int UPORDER = -1;
    final public static int DOWNORDER = -2;
    final public static int CATEGORY_ENABLE = -3;
    final public static int CATEGORY_UNABLE = -4;
    final public static int SOURCE_ENABLE = -5;
    final public static int SOURCE_UNABLE = -6;

    public SharedPreferences preferences;

    public Boolean savedUser;
    public String userName, password;
    public TreeMap<Integer, Source> sources; //first is id, second is name, third is url
    public ArrayList<String> unabledSources; // by show name!
    public TreeMap<Integer, Category> categories; //first is category id, second is category inner name, third is category show name
    public ArrayList<String> unabledCategories; // by show name!
    public ArrayList<String> categoryNameList; // tabs shows on the screen except that forbids
    public ArrayList<String> categoryNameListShowed;

    public boolean isInternet;

    private TSQLHelper helper;

    public void init() {
        //collections init
        sources = new TreeMap<>();
        unabledSources = new ArrayList<>();
        categories = new TreeMap<>();
        unabledCategories = new ArrayList<>();
        categoryNameList = new ArrayList<>();
        categoryNameListShowed = new ArrayList<>();

        //open config file
        preferences = TApplication.context.getSharedPreferences(TApplication.context.getResources().getString(R.string.config_shared_name), Context.MODE_PRIVATE);

        //open config database
        helper = TApplication.sqlHelper;

        // read from preferences
        if (!preferences.getBoolean("created", false)) {
            create(preferences);
        }

        savedUser = preferences.getBoolean("savedUser", false);
        if (savedUser) {
            userName = preferences.getString("userName", "");
            password = preferences.getString("password", "");
        }

        readAndCalSource();

        // refresh categories using http
        for (Integer nowSource_id : sources.keySet()) {
            if (unabledSources.indexOf(nowSource_id) != -1) continue; // if unabled, skip it
            switch (nowSource_id) {
                case ENGSOURCEID:
                    Message msg = new EngCategoryRequester().refreshCategories();
                    switch (msg.arg2) {
                        case ENGSOURCEID:
                            if (msg.what == CategoryRequester.CATEGORY_FINISH) {
                                Log.i(Config.class.getName(), "English News categories received successful");
                            } else {
                                Log.w(Config.class.getName(), "English News categories failed!");
                                Log.w(Config.class.getName(), "Error code: " + String.valueOf((Integer)msg.obj));
                            }
                            break;
                    }
                    break;
                case CHNSOURCEID:
                    break;
            }
        }

        readAndCalCategory();
    }

    public static void create(SharedPreferences preferences) {
        Log.e(Config.class.getName(), "Shared Preferences creating");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("created", true);
        editor.putBoolean("savedUser", false);

        editor.putStringSet("unabledSources", new TreeSet<String>());
        editor.putStringSet("unabledCategories", new TreeSet<String>());
        editor.apply();
    }

    public void syncSharedPreferences(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("savedUser", savedUser);
        if (savedUser) {
            editor.putString("userName", userName);
            editor.putString("password", password);
        }

        TreeSet<String> unabledS = new TreeSet<>();
        TreeSet<String> unabledC = new TreeSet<>();
        for (String s : unabledSources)
            unabledS.add(s);
        for (String s : unabledCategories)
            unabledC.add(s);
        editor.putStringSet("unabledSources", unabledS);
        editor.putStringSet("unabledCategories", unabledC);
        editor.apply();
    }

    private void readAndCalSource() {
        SQLiteDatabase db = helper.getReadableDatabase();

        sources.clear();
        //read all sources in database
        Cursor cursor = db.query("SOURCES", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                sources.put(id, new Source(id, name, url));
            } while (cursor.moveToNext());
        }
        cursor.close();
        unabledSources.clear();
        Set<String> origin0 = preferences.getStringSet("unabledSources", new TreeSet<String>());
        for (String i : origin0)
            unabledSources.add(i);
    }

    private void readAndCalCategory() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Set<String> origin1 = preferences.getStringSet("unabledCategories", new TreeSet<String>());
        unabledCategories.clear();
        for (String i : origin1)
            unabledCategories.add(i);

        categories.clear();
        // read categories from database
        Cursor cursor = db.query("CATEGORY", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(cursor.getColumnIndex("id"));
                String innerName = cursor.getString(cursor.getColumnIndex("category"));
                String showName = cursor.getString(cursor.getColumnIndex("category_name"));
                Integer source_id = cursor.getInt(cursor.getColumnIndex("source_id"));
                categories.put(id, new Category(id, innerName, showName, source_id));
            } while (cursor.moveToNext());
            cursor.close();
        }

        // read from database
        categoryNameList.clear();
        cursor = db.query("CATEGORYNAME", null, null, null, null, null, "priority asc");
        int nowPriority = 0;
        if (cursor.moveToFirst()) {
            do {
                categoryNameList.add(cursor.getString(cursor.getColumnIndex("name")));
                if (cursor.getInt(cursor.getColumnIndex("priority")) > nowPriority)
                    nowPriority = cursor.getInt(cursor.getColumnIndex("priority"));
            } while (cursor.moveToNext());
            cursor.close();
        }

        // check and add new categories if exists
        cursor = db.query("CATEGORY", null, null, null, null, null, "id asc");
        if (cursor.moveToFirst()) {
            do {
                String nowName = cursor.getString(cursor.getColumnIndex("category_name"));
                if (categoryNameList.indexOf(nowName) == -1) {
                    ++nowPriority;
                    ContentValues values = new ContentValues();
                    values.put("priority", nowPriority);
                    values.put("name", nowName);
                    db.insert("CATEGORYNAME", null, values);
                    categoryNameList.add(nowName);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // add category name list showed
        categoryNameListShowed = new ArrayList<>();
        for (String s : categoryNameList) {
            boolean b = true;
            if (unabledCategories.contains(s)) b = false;
            boolean cross = false;
            for (Integer i : categories.keySet())
                if (categories.get(i).category_name.equals(s))
                    if (!unabledSources.contains(sources.get(categories.get(i).source_id).name)) {
                        cross = true;
                        break;
                    }
            if (b && cross) {
                categoryNameListShowed.add(s);
            }
        }
    }

    public void adjustCategoryOrder(String name, int type) {
        if (type == UPORDER) {
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.query("CATEGORYNAME", null, "name=?", new String[] {name}, null, null, null, null);
            if (cursor.moveToFirst()) {
                Cursor preCursor = db.query("CATEGORYNAME", null, "priority < ?", new String[] {String.valueOf(cursor.getInt(cursor.getColumnIndex("priority")))}, null, null, "priority DESC", null);
                if (preCursor.moveToFirst()) {
                    while (!categoryNameListShowed.contains(preCursor.getString(preCursor.getColumnIndex("name")))) {
                        if (!preCursor.moveToNext()) break;
                    }
                }
                if (!preCursor.isAfterLast()) {
                    int prePriority = preCursor.getInt(preCursor.getColumnIndex("priority"));
                    int nowPriority = cursor.getInt(cursor.getColumnIndex("priority"));

                    db.beginTransaction();

                    ContentValues values = new ContentValues();
                    values.put("priority", prePriority);
                    values.put("name", cursor.getString(cursor.getColumnIndex("name")));
                    db.update("CATEGORYNAME", values, "id=?", new String[] {String.valueOf(cursor.getInt(cursor.getColumnIndex("id")))});

                    values.clear();
                    values.put("priority", nowPriority);
                    values.put("name", preCursor.getString(preCursor.getColumnIndex("name")));
                    db.update("CATEGORYNAME", values, "id=?", new String[] {String.valueOf(preCursor.getInt(preCursor.getColumnIndex("id")))});

                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                preCursor.close();
                cursor.close();
            }
        }
        if (type == DOWNORDER) {
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.query("CATEGORYNAME", null, "name=?", new String[] {name}, null, null, null, null);
            if (cursor.moveToFirst()) {
                Cursor nexCursor = db.query("CATEGORYNAME", null, "priority > ?", new String[] {String.valueOf(cursor.getInt(cursor.getColumnIndex("priority")))}, null, null, "priority ASC", null);
                if (nexCursor.moveToFirst()) {
                    while (!categoryNameListShowed.contains(nexCursor.getString(nexCursor.getColumnIndex("name")))) {
                        if (!nexCursor.moveToNext()) break;
                    }
                }
                if (!nexCursor.isAfterLast()) {
                    int nexPriority = nexCursor.getInt(nexCursor.getColumnIndex("priority"));
                    int nowPriority = cursor.getInt(cursor.getColumnIndex("priority"));

                    db.beginTransaction();

                    ContentValues values = new ContentValues();
                    values.put("priority", nexPriority);
                    values.put("name", cursor.getString(cursor.getColumnIndex("name")));
                    db.update("CATEGORYNAME", values, "id=?", new String[] {String.valueOf(cursor.getInt(cursor.getColumnIndex("id")))});

                    values.clear();
                    values.put("priority", nowPriority);
                    values.put("name", nexCursor.getString(nexCursor.getColumnIndex("name")));
                    db.update("CATEGORYNAME", values, "id=?", new String[] {String.valueOf(nexCursor.getInt(nexCursor.getColumnIndex("id")))});

                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                nexCursor.close();
                cursor.close();
            }
        }
        readAndCalCategory();
    }

    public void setCategoryEnablity(String categoryName, int type) {
        if (type == CATEGORY_ENABLE) {
            if (unabledCategories.contains(categoryName)) {
                unabledCategories.remove(categoryName);
                syncSharedPreferences(preferences);
                readAndCalCategory();
            }
        }
        if (type == CATEGORY_UNABLE) {
            if (!unabledCategories.contains(categoryName)) {
                unabledCategories.add(categoryName);
                syncSharedPreferences(preferences);
                readAndCalCategory();
            }
        }
    }

    public void setSourceEnablity(String sourceName, int type) {
        if (type == SOURCE_ENABLE) {
            if (unabledSources.contains(sourceName)) {
                unabledSources.remove(sourceName);
                syncSharedPreferences(preferences);
                readAndCalSource();
                readAndCalCategory();
            }
        }
        if (type == SOURCE_UNABLE) {
            if (!unabledSources.contains(sourceName)) {
                unabledSources.add(sourceName);
                syncSharedPreferences(preferences);
                readAndCalSource();
                readAndCalCategory();
            }
        }
    }

}
