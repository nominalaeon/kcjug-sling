
package com.github.bstopp.kcjug.sling.twitter.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bstopp.kcjug.sling.twitter.Account;
import com.github.bstopp.kcjug.sling.twitter.Tweet;
import com.github.bstopp.kcjug.sling.twitter.dao.AccountDao;
import com.github.bstopp.kcjug.sling.twitter.dao.TweetDao;

@SlingServlet(paths = { "/bin/twitter/tweets" })
public class ShowTweetsServlet extends SlingSafeMethodsServlet {

    private static final long   serialVersionUID = -8280901137017777540L;

    private static final Logger LOG              = LoggerFactory.getLogger(ShowTweetsServlet.class);

    @Reference
    private AccountDao          accountDao;

    @Reference(target = "(screen.name=StoppThinking)")
    private TweetDao            tweetDao;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {

        response.setContentType("application/json");
        ObjectMapper om = new ObjectMapper();
        om.configure(Feature.AUTO_CLOSE_TARGET, false);
        String screenName = request.getParameter("screenName");
        int count = 20;
        String tweetCount = request.getParameter("count");
        try {
            count = Integer.parseInt(tweetCount);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid tweet count parameter value: {}", tweetCount);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        Account account = accountDao.findAccount(screenName);
        data.put("account", account);
        List<Tweet> tweets = tweetDao.getTweets(count);
        data.put("tweets", tweets);

        try {
            om.writeValue(response.getOutputStream(), data);
        } catch (JsonProcessingException ex) {
            LOG.error("Error occured writing JSON.", ex);
        }
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
}
