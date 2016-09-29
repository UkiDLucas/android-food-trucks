package com.uki.twitter.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Profile
{

	private static final String TAG = Profile.class.getSimpleName();

	private int id;
	private boolean geoEnabled;
	private String name;
	private String location;
	private String profileImageUrl;
	private String screen_name;
	private Status status;

	public boolean hasGeo()
	{
		return status.getLatitude() != 0.0 && status.getLongitude() != 0.0;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getProfileImageUrl()
	{
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl)
	{
		this.profileImageUrl = profileImageUrl;
	}

	public boolean isGeoEnabled()
	{
		return geoEnabled;
	}

	public void setGeoEnabled(boolean geoEnabled)
	{
		this.geoEnabled = geoEnabled;
	}

	public String getScreen_name()
	{
		return screen_name;
	}

	public void setScreen_name(String screen_name)
	{
		this.screen_name = screen_name;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public static Profile valueOf(JSONObject profile)
	{
		try
		{
			Profile p = new Profile();
			p.setScreen_name(profile.getString("screen_name"));
			p.setProfileImageUrl(profile.getString("profile_image_url"));
			p.setId(profile.getInt("id"));
			p.setLocation(profile.getString("location"));

			if (profile.has("status"))
			{
				Status s = new Status();
				JSONObject status = profile.getJSONObject("status");
				s.setText(status.getString("text"));
				s.setCreated_at(status.getString("created_at"));
				s.setFrom_user(p.getScreen_name());
				if (!status.getString("geo").equals("null"))
				{
					s.setLatitude(status.getJSONObject("geo").getJSONArray("coordinates").getDouble(0));
					s.setLongitude(status.getJSONObject("geo").getJSONArray("coordinates").getDouble(1));
				}
				p.setStatus(s);

			}
			return p;
		} catch (JSONException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}
}
