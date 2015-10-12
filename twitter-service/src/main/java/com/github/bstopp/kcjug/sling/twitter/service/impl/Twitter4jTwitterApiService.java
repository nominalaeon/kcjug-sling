
package com.github.bstopp.kcjug.sling.twitter.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bstopp.kcjug.sling.twitter.Account;
import com.github.bstopp.kcjug.sling.twitter.Tweet;
import com.github.bstopp.kcjug.sling.twitter.service.TweetFormatter;
import com.github.bstopp.kcjug.sling.twitter.service.TwitterApiService;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

//@formatter:off
@Service
@Component(metatype = true, enabled= true, immediate = true,
    configurationFactory = true,
    policy = ConfigurationPolicy.REQUIRE,
    label= "KCJUG Twitter4j Twitter Service")
@Properties({
@Property(  name = Constants.SERVICE_DESCRIPTION,
            value = "Gets Twitter Account and Tweets from remote system."),
})
//@formatter:on
public class Twitter4jTwitterApiService implements TwitterApiService {

    private static final Logger LOG                  = LoggerFactory.getLogger(Twitter4jTwitterApiService.class);

    @Property(label = "Twitter Screen Name")
    private static final String SCREEN_NAME_PROP     = "screen.name";

    @Property(label = "Twitter API Consumer Key")
    private static final String CONSUMER_KEY_PROP    = "consumer.key";

    @Property(label = "Twitter API Consumer Secret")
    private static final String CONSUMER_SECRET_PROP = "consumer.secret";

    private static final int    PAGE_SIZE            = 20;

    private String              screenName;
    private String              consumerKey;
    private String              consumerSecret;

    private TwitterFactory      twitterFactory;

    private TweetFormatter      tweetFormatter       = new TweetFormatter();

    public Twitter4jTwitterApiService() {
        twitterFactory = new TwitterFactory(buildConfiguration());
    }

    // TODO change this to accept screen name so it's not a configuratoin value but a parameter
    @Override
    public Account getAccount() {

        Twitter twitter = null;
        try {
            twitter = getTwitter();
            User user = twitter.showUser(screenName);

            Account account = new Account();
            account.setScreenName(user.getScreenName());
            account.setUserId(user.getId());
            account.setProfileUrl(TwitterApiService.TWITTER_URL + user.getScreenName());
            account.setProfileImage(user.getBiggerProfileImageURLHttps());
            account.setDescription(user.getDescription());
            account.setFollowers(user.getFollowersCount());
            account.setFriends(user.getFriendsCount());

            return account;
        } catch (TwitterException e) {
            LOG.error("An error occured while trying to retrieve the statuses.", e);
            throw new ServiceException("An error occured while trying to load the account", e);
        }
    }

    @Override
    public List<Tweet> getTweets() {
        return getTweets(null);
    }

    @Override
    public List<Tweet> getTweets(Tweet start) {

        LOG.debug("Looking up Tweets for User ({}) using API Key {}", screenName, consumerKey);
        List<Tweet> tweets = new ArrayList<Tweet>();
        Twitter twitter = null;
        try {
            twitter = getTwitter();

            Paging paging = start != null ? new Paging(start.getStatusId()) : new Paging();
            ResponseList<Status> statuses = twitter.getUserTimeline(screenName, paging);
            if (start == null) {
                while (statuses.isEmpty()) {
                    paging.setCount(paging.getCount() + PAGE_SIZE);
                }
            }
            for (Status status : statuses) {
                if ((status.getInReplyToStatusId() > 0) || (status.getInReplyToUserId() > 0)
                        || (status.getInReplyToScreenName() != null)) {
                    continue;
                }
                LOG.trace("Found Status ({}), converting to Tweet.", status.getId());
                Tweet tweet = convert(status);
                LOG.trace("Converted Tweet ({})", tweet);
                tweets.add(tweet);
            }
        } catch (TwitterException ex) {
            LOG.error("An error occured while trying to retrieve the statuses.", ex);
        }

        return tweets;

    }

    @Activate
    protected void activate(ComponentContext context) throws Exception {

        LOG.debug("Starting Twitter4j API Client Service.");

        Dictionary<String, Object> props = context.getProperties();

        screenName = PropertiesUtil.toString(props.get(SCREEN_NAME_PROP), null);
        if (StringUtils.isBlank(screenName)) {
            throw new ConfigurationException(SCREEN_NAME_PROP, "Screen name is required.");
        }

        consumerKey = PropertiesUtil.toString(props.get(CONSUMER_KEY_PROP), null);
        if (StringUtils.isBlank(consumerKey)) {
            throw new ConfigurationException(CONSUMER_KEY_PROP, "Consumer Key is required.");
        }

        consumerSecret = PropertiesUtil.toString(props.get(CONSUMER_SECRET_PROP), null);
        if (StringUtils.isBlank(consumerSecret)) {
            throw new ConfigurationException(CONSUMER_SECRET_PROP, "Consumer Secret is required.");
        }

        try {
            // Test connection to Twitter.
            getTwitter();
        } catch (TwitterException e) {
            throw new Exception("Unable to connect to Twitter, Key/Secret invalid.", e);
        }
    }

    public Tweet convert(Status status) {
        LOG.debug("Twitter4jTwitterApiService - convert " + status.getText());
        Tweet tweet = new Tweet();
        tweet.setStatusId(status.getId());
        tweet.setScreenName(status.getUser().getScreenName());
        tweet.setText(status.getText());
        tweet.setFormattedText(tweetFormatter.formatText(status.getText(), status));

        Calendar created = Calendar.getInstance();
        created.setTime(status.getCreatedAt());
        tweet.setCreated(created);

        tweet.setRetweet(status.isRetweet());
        if (tweet.isRetweet()) {
            tweet.setOriginal(convert(status.getRetweetedStatus()));
        }
        return tweet;
    }

    private Twitter getTwitter() throws TwitterException {
        Twitter twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.getOAuth2Token();
        return twitter;
    }

    private Configuration buildConfiguration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setGZIPEnabled(true);
        cb.setApplicationOnlyAuthEnabled(true);
        return cb.build();
    }

}
