
package com.github.bstopp.kcjug.sling.twitter.job;

import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bstopp.kcjug.sling.twitter.Tweet;
import com.github.bstopp.kcjug.sling.twitter.dao.AccountDao;
import com.github.bstopp.kcjug.sling.twitter.dao.PersistenceException;
import com.github.bstopp.kcjug.sling.twitter.dao.TweetDao;
import com.github.bstopp.kcjug.sling.twitter.service.TwitterApiService;

//@formatter:off
@Component(immediate = true, metatype = true, enabled = true,
        label = "KCJUG witter Sync Job",
        description = "Schedule job which refreshes Twitter Feed components on a recurring basis",
        policy = ConfigurationPolicy.REQUIRE)
@Service(serviceFactory = false)
@Properties(value = {
        @Property(name = "scheduler.expression", value = "" , label = "Refresh Interval",
                description = "Twitter Feed Refresh interval (Quartz Cron Expression)"),
        @Property(name = "scheduler.concurrent", boolValue = false, propertyPrivate = true) })
//@formatter:on
public class TwitterSyncJob implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterSyncJob.class);

    // TODO add screen name as a config value and then turn this into a factory.

    @Reference
    private TwitterApiService   twitterApiService;

    @Reference
    private AccountDao          accountDao;

    @Reference(target = "(screen.name=StoppThinking)")
    private TweetDao            tweetDao;

    @Override
    public final void run() {
        LOG.debug("Starting Twitter Sync Job");

        try {
            accountDao.save(twitterApiService.getAccount());
            Tweet latest = tweetDao.getMostRecent();
            List<Tweet> tweets = twitterApiService.getTweets(latest);
            for (Tweet t : tweets) {
                tweetDao.save(t);
            }

        } catch (ServiceException e) {
            LOG.error("An error occured while trying to retrieve Twitter data..", e);
        } catch (PersistenceException e) {
            LOG.error("An error occured while trying to save Twitter data.", e);
        } catch (Exception e) {
            LOG.error("An error occured while running the Synch process.", e);
        }

    }

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        LOG.debug("TwitterSyncJob - Activate");
    }

    protected void bindAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    protected void unbindAccountDao(AccountDao accountDao) {
        if (this.accountDao == accountDao) {
            this.accountDao = null;
        }
    }

    protected void bindTweetDao(TweetDao tweetDao) {
        this.tweetDao = tweetDao;
    }

    protected void unbindTweetDao(TweetDao tweetDao) {
        if (this.tweetDao == tweetDao) {
            this.tweetDao = null;
        }
    }

    protected void bindTwitterApiService(TwitterApiService twitterApiService) {
        this.twitterApiService = twitterApiService;
    }

    protected void unbindTwitterApiService(TwitterApiService twitterApiService) {
        if (this.twitterApiService == twitterApiService) {
            this.twitterApiService = null;
        }
    }
}
