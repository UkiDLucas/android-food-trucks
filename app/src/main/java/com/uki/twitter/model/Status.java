package com.uki.twitter.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Status implements Serializable {

	private static final String TAG = Status.class.getSimpleName();

	private long id;
	private double latitude;
	private double longitude;
	private String created_at;
	private String text;
	private String from_user;

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public static Status valueOf(JSONObject status) {
		try {
			Status s = new Status();
			if (!status.getString("geo").equals("null")) {
				s.setLatitude(status.getJSONObject("geo")
						.getJSONArray("coordinates").getDouble(0));
				s.setLongitude(status.getJSONObject("geo")
						.getJSONArray("coordinates").getDouble(1));
			}
			s.setFrom_user(status.getJSONObject("user").getString("screen_name"));
			s.setCreated_at(status.getString("created_at"));
			s.setText(status.getString("text"));
			s.setId(status.getLong("id"));
			return s;
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}

	public void setFrom_user(String from_user) {
		this.from_user = from_user;
	}

	public String getFrom_user() {
		return from_user;
	}

}
