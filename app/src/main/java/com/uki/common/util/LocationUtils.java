package com.uki.common.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

public class LocationUtils
{
	public static boolean isNetworkLocationProviderEnabled(Context ctx)
	{
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	public static boolean isGPSLocationprovderEnabled(Context ctx)
	{
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static boolean ifAnyLocationProviderEnabled(Context ctx)
	{
		final LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static void createLocationProviderDisabledAlert(final Context ctx)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage("This option requires location provider be enabled. Would you like to go to the settings screen?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						ctx.startActivity(gpsOptionsIntent);
					}
				});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static Location getLatestKnownLocation(Context context)
	{
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = lm.getBestProvider(crit, true);
		if (provider != null)
		{
			Location loc = lm.getLastKnownLocation(provider);
			return loc;
		} else
		{
			return null;
		}

	}

	public static void getDirections(Context context, double startLatitude, double startLongitude, double destLatitude, double destLongitude)
	{
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + startLatitude + "," + startLongitude + "&daddr=" + destLatitude + ","
				+ destLongitude));
		context.startActivity(intent);
	}
}
