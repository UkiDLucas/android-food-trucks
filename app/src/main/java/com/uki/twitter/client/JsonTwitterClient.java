package com.uki.twitter.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import com.uki.twitter.R;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.uki.twitter.model.Tweet;
import com.uki.common.http.HttpUtils;

public class JsonTwitterClient implements TwitterClient{

    private static final String TAG = JsonTwitterClient.class.getSimpleName();
    private static final String SEARCH_URI = "https://api.twitter.com/1.1/search/tweets.json";
    private static final int TWEETS_PER_PAGE = 100;
    private HttpClient httpClient;

    public JsonTwitterClient() {
        this(HttpUtils.createHttpClient());
    }

    public JsonTwitterClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public List<Tweet> fetchTweets(Context ctx, String hashTag) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try{

            StringBuilder sb = new StringBuilder(SEARCH_URI + "?q=" + URLEncoder.encode(hashTag.replaceFirst("#", "%23")) + "&count="
                    + TWEETS_PER_PAGE);
            sb.append("result_type=recent");
            HttpGet httpGet = new HttpGet(sb.toString());
            String bearerToken = getBearerToken(ctx);
            if (TextUtils.isEmpty(bearerToken)) {
                bearerToken = retrieveBearerToken(ctx);
                saveBearerToken(ctx, bearerToken);
            }
            httpGet.addHeader("Authorization", "Bearer "+ bearerToken);
            HttpResponse httpResponse = httpClient.execute(httpGet);

            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            JSONObject twitterFeed = new JSONObject(responseStr);
            if (twitterFeed.has("statuses")) {
                JSONArray entries = twitterFeed.getJSONArray("statuses");
                for (int i = 0; i < entries.length(); i++) {
                    tweets.add(Tweet.valueOf(entries.getJSONObject(i)));
                }
            }
        }catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return tweets;
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClientProtocolException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
