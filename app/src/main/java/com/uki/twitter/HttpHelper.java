
package com.uki.twitter;

import java.net.SocketException;
import java.net.UnknownHostException;

import oauth.signpost.http.HttpResponse;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class HttpHelper {

    private HttpClient client;

  //  private HttpGet httpget;

    private BasicResponseHandler responseHandler;

    public HttpHelper() {

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Create and initialize scheme registry
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);

        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);

        client = new DefaultHttpClient(cm, params);

        // client = new DefaultHttpClient();
        //httpget = new HttpGet();
        responseHandler = new BasicResponseHandler();
    }

    public String getPage(HttpGet get, Activity activity) {
        try {
            return client.execute(get, responseHandler);
		} catch (UnknownHostException e) {
			HttpHelper.showInternetConnectionError(activity);
		} catch (SocketException e) {
			HttpHelper.showInternetConnectionError(activity);
		} catch (Exception e) {
			e.printStackTrace();
			//activity.finish();
        }
        return "";
    }

    public String doPost(HttpPost httpost, Activity activity) {

        String response = "";

        try {
        	org.apache.http.HttpResponse httpResponse = client.execute(httpost);
        	String s = EntityUtils.toString(httpResponse.getEntity());
        	
           // response = client.execute(httpost, responseHandler);
        	Log.i("response", s);
		} catch (UnknownHostException e) {
			HttpHelper.showInternetConnectionError(activity);
		} catch (SocketException e) {
			HttpHelper.showInternetConnectionError(activity);
		} catch (Exception e) {
			e.printStackTrace();
			//activity.finish();
        }

        // String response = new DefaultHttpClient().execute(httpget,
        // responseHandler);

        return response;
    }
    
    public static void showInternetConnectionError(Activity activity) {
		Looper.prepare();
		Toast.makeText(activity, activity.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
		Looper.loop();
		Looper.myLooper().quit();
    }

}
