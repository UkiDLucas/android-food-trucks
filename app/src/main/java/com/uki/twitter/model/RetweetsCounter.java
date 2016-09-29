package com.uki.twitter.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class RetweetsCounter implements Serializable{

	public RetweetsCounter(String hashTag, String userName, int count) {
		this.hashTag = hashTag;
		this.userName = userName;
		this.count = count;
	}
	public RetweetsCounter(){}
	
	public static final String EVENT_HASHTAG = "hashtag";
	public static final String USER_NAME = "username";
	public static final String COUNT = "count";
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(columnName = EVENT_HASHTAG)
	private String hashTag;
	@DatabaseField(columnName = USER_NAME)
	private String userName;
	@DatabaseField(columnName = COUNT)
	private int count;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHashTag() {
		return hashTag;
	}
	public void setHashTag(String hashTag) {
		this.hashTag = hashTag;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
}
