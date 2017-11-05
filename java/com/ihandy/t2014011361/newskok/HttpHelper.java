package com.ihandy.t2014011361.newskok;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.Message;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** Use for send http get and post request
 *  Caution: block!
 *  Result send to handler by message
 *  When success, msg.what = success, msg.obj = responseEntity
 *  when failed, msg.what = failed, msg.obj = err code
 */
public class HttpHelper {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;

    private static final int CONNECTION_TIME_LIMIT = 1000;
    private static final int CONNECTION_SO_LIMIT = 2000;

    public static final String COMMENT_SERVER = "http://121.42.171.31/newskokServer";

    public static final String SUCCESS_FLAG = "success for server";

    Message globlMsg;

    public static Message sendHttpGetRequest(final String address) {
        HttpClient httpClient = new DefaultHttpClient();

        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIME_LIMIT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTION_SO_LIMIT);

        String trans_add = new String(address);
        trans_add = trans_add.replaceAll(" ", "%20");

        HttpGet httpGet = new HttpGet(trans_add);
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = FAILED;
            msg.obj = "1";
            return msg;
        }
        HttpEntity entity;
        try {
            if (response.getStatusLine().getStatusCode() != 200)
                throw new IOException("2");
            entity = response.getEntity();
            Message msg = new Message();
            msg.what = SUCCESS;
            msg.obj = EntityUtils.toString(entity);
            return msg;
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = FAILED;
            if ((e.getMessage() != null) && (e.getMessage().equals("2"))) {
                msg.obj = e.getMessage();
                msg.arg1 = response.getStatusLine().getStatusCode();
            } else
                msg.obj = "3";
            return msg;
        } catch (ParseException e) {
            Message msg = new Message();
            msg.what = FAILED;
            msg.obj = "4";
            return msg;
        }
    }

    public static Message sendHttpPostRequest(final String address, final Map<String, String> params) {
        HttpClient httpClient = new DefaultHttpClient();

        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIME_LIMIT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTION_SO_LIMIT);

        String trans_add = new String(address);
        trans_add = trans_add.replaceAll(" ", "%20");

        HttpPost httpPost = new HttpPost(trans_add);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (String i : params.keySet()) {
            pairs.add(new BasicNameValuePair(i, params.get(i)));
        }
        UrlEncodedFormEntity fromEntity = null;
        try {
            fromEntity = new UrlEncodedFormEntity(pairs, HTTP.UTF_8);
            fromEntity.setContentEncoding(HTTP.UTF_8);
            httpPost.setEntity(fromEntity);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            httpPost.getParams().setParameter(HTTP.CHARSET_PARAM, HTTP.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = FAILED;
            msg.obj = "1";
            return msg;
        }
        HttpEntity entity;
        try {
            if (response.getStatusLine().getStatusCode() != 200)
                throw new IOException("2");
            entity = response.getEntity();
            Message msg = new Message();
            msg.what = SUCCESS;
            msg.obj = EntityUtils.toString(entity, "utf-8");
            return msg;
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = FAILED;
            if (e.getMessage().equals("2")) {
                msg.obj = e.getMessage();
                msg.arg1 = response.getStatusLine().getStatusCode();
            } else
                msg.obj = "3";
            return msg;
        } catch (ParseException e) {
            Message msg = new Message();
            msg.what = FAILED;
            msg.obj = "4";
            return msg;
        }
    }

    // return message: failed: what = failed; success: what = success & obj = store absolute path
    public Message sendPhotoRequest(final String url, final String pathPrefix) {
        globlMsg = new Message();
        if (url.lastIndexOf(".") == -1) {
            globlMsg.what = HttpHelper.FAILED;
            globlMsg.obj = "5"; // format error;
            return globlMsg;
        }
        String suffix = url.substring(url.lastIndexOf("."));
        if (suffix.equals(".jpg") || suffix.equals(".jpeg") || suffix.equals(".bmp") || suffix.equals(".png")) {
        } else {
            globlMsg.what = HttpHelper.FAILED;
            globlMsg.obj = "5"; // format error;
            return globlMsg;
        }

        String format = url.substring(url.lastIndexOf("."));
        String fullPath = pathPrefix + format;

        String trans_url = new String(url);
        trans_url = trans_url.replaceAll(" ", "%20");

        BufferedInputStream in = null;
        FileOutputStream out = null;
        try {
            out = TApplication.context.openFileOutput(fullPath, TApplication.context.MODE_PRIVATE);
            URL webURL = new URL(trans_url);
            HttpURLConnection conn = (HttpURLConnection)webURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECTION_TIME_LIMIT);
            in = new BufferedInputStream(conn.getInputStream());
            byte buf[] = new byte[1024];
            int readBytes;
            while ((readBytes = in.read(buf)) != -1) {
                out.write(buf, 0, readBytes);
            }
            globlMsg.what = HttpHelper.SUCCESS;
            globlMsg.obj = fullPath;
        } catch(IOException e) {
            globlMsg.what = HttpHelper.FAILED;
            globlMsg.obj = "6";
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {}
        }
        return globlMsg;
    }

    public static boolean checkInternet() {
        Message conn = HttpHelper.sendHttpGetRequest(COMMENT_SERVER);
        if (conn.what == HttpHelper.SUCCESS) {
            if (conn.obj.equals(SUCCESS_FLAG))
                return true;
            else
                return false;
        }
        if (conn.what == HttpHelper.FAILED) {
            return false;
        }
        return false;
    }

}
