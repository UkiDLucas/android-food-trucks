package com.uki.common.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

public class NetworkUtils
{
	public static boolean isNetworkAvailable(Context ctx)
	{
		ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

		if (activeNetworkInfo != null)
		{
			return true;
		}

		// mobile
		NetworkInfo.State mobile = connManager.getNetworkInfo(0) != null ? connManager.getNetworkInfo(0).getState() : null;
		// wifi
		NetworkInfo.State wifi = connManager.getNetworkInfo(1) != null ? connManager.getNetworkInfo(1).getState() : null;

		return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING || wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
	}

	public static boolean isWifiAvailable(Context ctx)
	{
		ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

		if (activeNetworkInfo != null)
		{
			return true;
		}

		// wifi
		NetworkInfo.State wifi = connManager.getNetworkInfo(1) != null ? connManager.getNetworkInfo(1).getState() : null;

		return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
	}

	public static boolean isAirplaneModeOn(Context ctx)
	{
		return Settings.System.getInt(ctx.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public static void createNetworkisabledAlert(final Activity activity)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Network Error - The Internet connection appears to be offline.").setCancelable(false).setPositiveButton("Close", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				((Activity) activity).finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
