
package com.github.bstopp.kcjug.sling.twitter.dao;

import java.util.List;

import com.github.bstopp.kcjug.sling.twitter.Tweet;

public interface TweetDao {

    void save(Tweet tweet);

    Tweet getMostRecent();

    List<Tweet> getTweets(int count);

    List<Tweet> getTweets(int count, int page);

}
