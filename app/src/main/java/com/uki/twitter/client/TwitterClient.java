package com.uki.twitter.client;

import java.util.List;

import android.content.Context;
import com.uki.twitter.model.Tweet;

public interface TwitterClient {
    List<Tweet> fetchTweets(Context ctx, String hashTag);
}
