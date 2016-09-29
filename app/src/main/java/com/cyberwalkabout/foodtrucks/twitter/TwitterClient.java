package com.cyberwalkabout.foodtrucks.twitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.uki.foodtrucks.R;
import com.uki.common.http.HttpClientConfig;
import com.uki.common.http.HttpUtils;

public class TwitterClient {

    public static final String TAG = TwitterClient.class.getSimpleName();

    private static final String PROFILES_URL = "https://api.twitter.com/1.1/users/lookup.json";

    public static List<TwitterStatus> getTwitterStatuses(Context ctx, List<String> twitterNames) {
        return getStatuses(doPost(ctx, PROFILES_URL, getParams(twitterNames)));
    }

    public static List<TwitterStatus> getTwitterMentions(Context ctx, String twitterName) {
        return getStatuses(doGet(ctx, getMentionsUrl(twitterName)));
    }

    public static List<TwitterStatus> getTwitterMentions(Context ctx, List<String> twitterNames) {
        return getStatuses(doGet(ctx, getMentionsUrl(twitterNames)));
    }

    private static String getResponse(HttpUriRequest httpUriRequest) {
        HttpResponse httpResponse = null;
        try {
            HttpClient httpClient = HttpUtils.createHttpClient(HttpClientConfig.createConfig(10000));
            httpResponse = httpClient.execute(httpUriRequest);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(out);
            out.close();
            String response = out.toString();
            Log.d(TAG, response);
            return response;
        } catch (IOException e) {
            String message = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            Log.e(TAG, message);
        } finally {
            try {
                httpResponse.getEntity().getContent().close();
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    private static String doGet(Context ctx, String url) {
        HttpGet get = new HttpGet(url);
        String bearerToken = getBearerToken(ctx);
        if (TextUtils.isEmpty(bearerToken)) {
            bearerToken = retrieveBearerToken(ctx);
            saveBearerToken(ctx, bearerToken);
        }
        get.addHeader("Authorization", "Bearer " + bearerToken);
        return getResponse(get);
    }

    private static String doPost(Context ctx, String url, List<BasicNameValuePair> params) {
        try {
            HttpPost post = new HttpPost(url);
            String bearerToken = getBearerToken(ctx);
            if (TextUtils.isEmpty(bearerToken)) {
                bearerToken = retrieveBearerToken(ctx);
                saveBearerToken(ctx, bearerToken);
            }
            post.addHeader("Authorization", "Bearer " + bearerToken);

            post.setEntity(new UrlEncodedFormEntity(params));
            return getResponse(post);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private static List<BasicNameValuePair> getParams(List<String> twitterNames) {
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        StringBuffer queryString = new StringBuffer();
        for (String name : twitterNames) {
            queryString.append(URLEncoder.encode(name.substring(1)));
            queryString.append(",");
        }
        queryString.deleteCharAt(queryString.length() - 1);
        params.add(new BasicNameValuePair("screen_name", queryString.toString()));
        return params;
    }

    private static String getMentionsUrl(String twitterName) {
        StringBuffer urlString = new StringBuffer();
        urlString.append("https://api.twitter.com/1.1/search/tweets.json?q=");
        urlString.append(URLEncoder.encode(twitterName));
        urlString.append("&result_type=recent");
        return urlString.toString();
    }

    private static String getMentionsUrl(List<String> twitterNames) {
        StringBuffer urlString = new StringBuffer();
        urlString.append("https://api.twitter.com/1.1/search/tweets.json?q=");
        for (int i = 0; i < twitterNames.size(); i++) {
            urlString.append(URLEncoder.encode(twitterNames.get(i)));
            if (i != twitterNames.size() - 1) {
                urlString.append(URLEncoder.encode(" OR "));
            }
        }
        urlString.append("&result_type=recent");
        return urlString.toString();
    }

    private static List<TwitterStatus> getStatuses(String response) {
        List<TwitterStatus> statuses = new ArrayList<TwitterStatus>();
        try {
            JSONObject data = new JSONObject(response);
            JSONArray results = data.getJSONArray("statuses");
            for (int i = 0; i < results.length(); i++) {
                JSONObject status = results.getJSONObject(i);
                statuses.add(TwitterStatus.fromJSON(status));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return statuses;
    }

    private static String retrieveBearerToken(Context ctx) {
        try {
            String consumerKey = ctx.getString(R.string.twitter_consumer_key);
            String consumerSecret = ctx.getString(R.string.twitter_consumer_secret);

            String credentialsStr = URLEncoder.encode(consumerKey) + ":" + URLEncoder.encode(consumerSecret);
            String base64encodedCredentials = toBase64fromString(credentialsStr);

            HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth2/token");
            httpPost.addHeader("Authorization", "Basic " + base64encodedCredentials);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            List<NameValuePair> params = new ArrayList<NameValuePair>(1);
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = HttpUtils.createHttpClient().execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String responseStr = EntityUtils.toString(httpResponse.getEntity());
                JSONObject response = new JSONObject(responseStr);
                return response.getString("access_token");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private static void saveBearerToken(Context ctx, String token) {
        SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences("bearer", Context.MODE_PRIVATE);
        prefs.edit().putString("token", token).commit();
    }

    private static String getBearerToken(Context ctx) {
        SharedPreferences prefs = ctx.getApplicationContext().getSharedPreferences("bearer", Context.MODE_PRIVATE);
        return prefs.getString("token", "");
    }

    private static String toBase64fromString(String text) throws UnsupportedEncodingException {
        return Base64.encodeToString(text.getBytes("UTF-8"), Base64.NO_WRAP);
    }
}
