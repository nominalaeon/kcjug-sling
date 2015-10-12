
package com.github.bstopp.kcjug.sling.twitter.service;

import java.util.List;

import com.github.bstopp.kcjug.sling.twitter.Account;
import com.github.bstopp.kcjug.sling.twitter.Tweet;

public interface TwitterApiService {

    String TWITTER_URL = "https://twitter.com/";

    Account getAccount();

    List<Tweet> getTweets();

    List<Tweet> getTweets(Tweet start);
}
