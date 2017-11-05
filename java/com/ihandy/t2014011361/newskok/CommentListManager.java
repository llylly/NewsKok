package com.ihandy.t2014011361.newskok;

import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentListManager {

    public static final int pageSize = 20;

    private Handler handler;
    public ArrayList<Comment> arr;
    private String news_id;

    CommentListManager(Handler _handler, ArrayList<Comment> _arr, String _news_id) {
        handler = _handler;
        arr = _arr;
        news_id = _news_id;
    }

    public void refresh() {
        arr = new ArrayList<>();
        basicGet(1, pageSize);
    }

    public void getMore() {
        ArrayList newOne = new ArrayList<>();
        newOne.addAll(arr);
        arr = newOne;
        basicGet(arr.size() / pageSize + 1, pageSize);
    }

    public void addComment(final String nickName, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String address = HttpHelper.COMMENT_SERVER + "/sendComment";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("newsId", news_id);
                params.put("nickName", nickName);
                params.put("comment", text);
                Message responseMsg = HttpHelper.sendHttpPostRequest(address, params);
                try {
                    if (responseMsg.what == HttpHelper.FAILED)
                        throw new Exception();
                    String text = (String) responseMsg.obj;
                    JSONObject obj = new JSONObject(text);
                    if (obj.getInt("code") != 200)
                        throw new Exception();
                } catch (Exception e) {
                    handler.sendMessage(makeMsg(CommentFragment.FAIL_ADD));
                }
                handler.sendMessage(makeMsg(CommentFragment.FINISH_ADD));
            }
        }).start();
    }

    private void basicGet(final int pageNo, final int num) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String address = HttpHelper.COMMENT_SERVER + "/getComment";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("newsId", news_id);
                params.put("page", String.valueOf(pageNo));
                params.put("num", String.valueOf(num));
                Message responseMsg = HttpHelper.sendHttpPostRequest(address, params);
                try {
                    if (responseMsg.what == HttpHelper.FAILED)
                        throw new Exception();
                    String text = (String) responseMsg.obj;
                    JSONObject obj = new JSONObject(text);
                    if (obj.getInt("code") != 200)
                        throw new Exception();
                    JSONArray jarr = obj.getJSONArray("comments");
                    for (int i = 0; i < jarr.length(); ++i) {
                        JSONObject now = jarr.getJSONObject(i);
                        Comment nowC = new Comment(now.getString("floor"), now.getString("nickname"),
                                now.getString("comment"));
                        boolean repeat = false;
                        for (Comment j : arr)
                            if (j.floor.equals(nowC.floor)) {
                                repeat = true;
                                break;
                            }
                        if (!repeat)
                            arr.add(nowC);
                    }
                } catch (Exception e) {
                    handler.sendMessage(makeMsg(CommentFragment.FAIL_LOADING));
                }
                handler.sendMessage(makeMsg(CommentFragment.FINISH_LOADING));
            }
        }).start();
    }

    public static Message makeMsg(int code) {
        Message msg = new Message();
        msg.what = code;
        return msg;
    }

}
