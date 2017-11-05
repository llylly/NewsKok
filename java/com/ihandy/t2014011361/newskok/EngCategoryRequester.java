package com.ihandy.t2014011361.newskok;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class EngCategoryRequester extends CategoryRequester {

    final public static int CONNECTION_ERROR = 1;
    final public static int NOT_200_ERROR = 2;
    final public static int TRANSFORM_ERROR = 3;
    final public static int PARSE_ERROR = 4;
    final public static int FORMAT_ERROR = 5;

    @Override
    public Message refreshCategories() {
        String baseUrl = TApplication.config.sources.get(Config.ENGSOURCEID).url;
        String paramUrl = "en/category?timestamp=" + String.valueOf(new Date().getTime());
        String finalUrl = baseUrl + paramUrl;

        Message msg = HttpHelper.sendHttpGetRequest(finalUrl);
        Message returnMsg = new Message();
        msg.arg2 = Config.ENGSOURCEID; // arg2 specifies the type of requester for message
        switch (msg.what) {
            case HttpHelper.SUCCESS:
                try {
                    parseReturn((String) msg.obj);
                    returnMsg.what = CATEGORY_FINISH;
                } catch (Exception e) {
                    returnMsg.what = CATEGORY_FAILED;
                    returnMsg.obj = FORMAT_ERROR;
                } finally {
                    return returnMsg;
                }
            case HttpHelper.FAILED:
                returnMsg.what = CATEGORY_FAILED;
                switch (Integer.valueOf((String) msg.obj)) {
                    case 1:
                        returnMsg.obj = CONNECTION_ERROR;
                        break;
                    case 2:
                        returnMsg.obj = NOT_200_ERROR;
                        returnMsg.arg1 = msg.arg1; // arg1 saved the received status code in int type
                        break;
                    case 3:
                        returnMsg.obj = TRANSFORM_ERROR;
                        break;
                    case 4:
                        returnMsg.obj = PARSE_ERROR;
                        break;
                }
                return returnMsg;
        }
        return new Message();
    }

    private void parseReturn(String ret) throws Exception {

        Log.i(EngCategoryRequester.class.getName(), "Category Response: " + ret);

        Map<String, String> receivedMap = new TreeMap<String, String>();
        try {
            JSONObject rootObj = new JSONObject(ret);
            JSONObject categoryArr = rootObj.getJSONObject("data").getJSONObject("categories");
            Iterator<String> ite = categoryArr.keys();
            while (ite.hasNext()) {
                String nowKey = ite.next();
                String nowValue = categoryArr.getString(nowKey);
                receivedMap.put(nowKey, nowValue);
            }

        } catch (JSONException e) {
            Log.e(EngCategoryRequester.class.getName(), "JSON Exception!");
            throw e;
        }
        SQLiteDatabase db = TApplication.sqlHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete("CATEGORY", "source_id = ?", new String[] {String.valueOf(Config.ENGSOURCEID)});
        for (String nowCategory : receivedMap.keySet()) {
            String nowCategoryName = receivedMap.get(nowCategory);
            ContentValues cv = new ContentValues();
            cv.put("category", nowCategory);
            cv.put("category_name", nowCategoryName);
            cv.put("source_id", Config.ENGSOURCEID);
            db.insert("CATEGORY", null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
