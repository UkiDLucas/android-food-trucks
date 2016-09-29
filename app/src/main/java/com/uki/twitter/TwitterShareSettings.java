package com.uki.twitter;

import java.io.Serializable;

public class TwitterShareSettings implements Serializable {
	
	public TwitterShareSettings(String twitterCallbackUrl, String twitterConsumerKey, String twitterConsumerSecret) {
		this.twitterCallbackUrl = twitterCallbackUrl;
		this.twitterConsumerKey = twitterConsumerKey;
		this.twitterConsumerSecret = twitterConsumerSecret;
	}
	
	private String applicationName = "";
	
	private String twitterCallbackUrl;
	private String twitterConsumerKey;
	private String twitterConsumerSecret;
	

	public String getTwitterCallbackUrl() {
		return twitterCallbackUrl;
	}
	public void setTwitterCallbackUrl(String twitterCallbackUrl) {
		this.twitterCallbackUrl = twitterCallbackUrl;
	}
	public String getTwitterConsumerKey() {
		return twitterConsumerKey;
	}
	public void setTwitterConsumerKey(String twitterConsumerKey) {
		this.twitterConsumerKey = twitterConsumerKey;
	}
	public String getTwitterConsumerSecret() {
		return twitterConsumerSecret;
	}
	public void setTwitterConsumerSecret(String twitterConsumerSecret) {
		this.twitterConsumerSecret = twitterConsumerSecret;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	
	public TwitterShareSettings updateAppName(String applicationName) {
		this.applicationName = applicationName;
		return this;
	}
}
