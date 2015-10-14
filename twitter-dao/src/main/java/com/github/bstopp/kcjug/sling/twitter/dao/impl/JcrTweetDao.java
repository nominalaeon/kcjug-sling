
package com.github.bstopp.kcjug.sling.twitter.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bstopp.kcjug.sling.twitter.Tweet;
import com.github.bstopp.kcjug.sling.twitter.dao.PersistenceException;
import com.github.bstopp.kcjug.sling.twitter.dao.TweetDao;

//@formatter:off
@Service
@Component(enabled = true, metatype = true,
    immediate = true,
    configurationFactory = true,
    policy = ConfigurationPolicy.REQUIRE,
    label= "KCJUG Twitter Tweet DAO"
)
@Properties({
    @Property(
        name = Constants.SERVICE_DESCRIPTION,
        value = "Persists twitter model objects to the JCR.")
})
//@formatter:on
public class JcrTweetDao implements TweetDao {

    private static final Logger     LOG         = LoggerFactory.getLogger(JcrTweetDao.class);

    @Property(label = "Screen Name", description = "The screen name used for this configuration,")
    static final String             SCREEN_NAME = "screen.name";

    @Property(label = "Save Path", description = "The absolute path in the JCR at which to save the data.")
    static final String             JCR_PATH    = "jcr.path";

    @Property(
            label = "Folder Structure",
            description = "Enter the folder structure to create based on the Tweet timestamps. "
                    + "See java.text.SimpleDateFormat for available patterns. "
                    + "(e.g. yyyy/MM/dd will segment the tweets by day)")
    static final String             FOLDERS     = "folder.structure";

    private String                  screenName;
    private String                  path;
    private String                  folders;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @SuppressWarnings("deprecation")
    @Override
    public void save(Tweet tweet) {
        LOG.debug("JcrTweetDao - save " + tweet.toString());
        Calendar created = tweet.getCreated();
        String segments = new SimpleDateFormat(folders).format(created.getTime());
        String name = tweet.getText().substring(0, 10).replaceAll("[^A-Za-z0-9_-]", "_");
        ;
        String savePath = buildPath(path, screenName, segments, name);

        ResourceResolver resourseResolver = null;
        try {
            LOG.debug("JcrTweetDao - saving tweet to: {}", savePath);
            resourseResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            Resource resource = resourseResolver.resolve(savePath);
            tweet.setResource(resource);
            tweet.persist();

        } catch (LoginException ex) {
            LOG.error("Unable to log in to the repository: {}", ex.getMessage());
            throw new PersistenceException("Unable to log in to the repository.", ex);
        } catch (org.apache.sling.api.resource.PersistenceException ex) {
            LOG.error("An error occured while trying to persist the Account:  {}", ex.getMessage());
            throw new PersistenceException("Error occured while trying to persist the account.", ex);
        } finally {
            if (resourseResolver != null) {
                resourseResolver.close();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Tweet getMostRecent() {
        LOG.debug("JcrTwitterDao - getMostRecent");
        String accountPath = buildPath(path, screenName);
        Tweet tweet = null;

        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT * from [nt:unstructured] as tweet ");
        sql.append("WHERE ISDESCENDANTNODE(tweet, '").append(accountPath).append("') ");
        sql.append("ORDER BY tweet.[created] DESC");
        LOG.debug("SQL: {}", sql.toString());

        ResourceResolver resourceResolver = null;
        try {

            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            Session sesn = resourceResolver.adaptTo(Session.class);

            QueryManager querymanager = sesn.getWorkspace().getQueryManager();
            Query query = querymanager.createQuery(sql.toString(), Query.JCR_SQL2);
            query.setLimit(1);
            QueryResult results = query.execute();

            NodeIterator nodeiterator = results.getNodes();
            if (nodeiterator.hasNext()) {
                Node node = nodeiterator.nextNode();
                Resource resource = resourceResolver.getResource(node.getPath());
                tweet = resource.adaptTo(Tweet.class);
            }
        } catch (LoginException ex) {
            LOG.error("Unable to log in to the repository: " + ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            LOG.error("An error occured while searching for the latest tweet.", ex);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }

        return tweet;
    }

    @Override
    public List<Tweet> getTweets(int count) {
        LOG.debug("JcrTwitterDao - getTweets({})", count);
        return getTweets(count, 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Tweet> getTweets(int count, int page) {
        LOG.debug("JcrTwitterDao - getTweets({}, {})", count, page);
        String accountPath = buildPath(path, screenName);
        List<Tweet> tweets = new ArrayList<Tweet>();
        Tweet tweet = null;

        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT * from [nt:unstructured] as tweet ");
        sql.append("WHERE ISDESCENDANTNODE(tweet, '").append(accountPath).append("') ");
        sql.append("AND CONTAINS(tweet.[").append(Tweet.SCREEN_NAME).append("], '").append(screenName).append("') ");
        sql.append("ORDER BY tweet.[created] DESC");
        LOG.debug("SQL: {}", sql.toString());

        ResourceResolver resourceResolver = null;
        try {

            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            Session sesn = resourceResolver.adaptTo(Session.class);

            QueryManager querymanager = sesn.getWorkspace().getQueryManager();
            Query query = querymanager.createQuery(sql.toString(), Query.JCR_SQL2);
            query.setLimit(count);
            query.setOffset(page * count);
            QueryResult results = query.execute();

            NodeIterator nodeiterator = results.getNodes();
            while (nodeiterator.hasNext()) {
                Node node = nodeiterator.nextNode();
                Resource resource = resourceResolver.getResource(node.getPath());
                tweet = resource.adaptTo(Tweet.class);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            }
        } catch (LoginException ex) {
            LOG.error("Unable to log in to the repository: " + ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            LOG.error("An error occured while searching for the latest tweet.", ex);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }

        return tweets;
    }

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        LOG.debug("JcrTweetDao - Activate");

        Dictionary<String, Object> props = context.getProperties();

        screenName = PropertiesUtil.toString(props.get(SCREEN_NAME), null);
        if (StringUtils.isEmpty(screenName)) {
            throw new ConfigurationException(SCREEN_NAME, "Screen name must be specified");
        }

        path = PropertiesUtil.toString(props.get(JCR_PATH), null);
        if (StringUtils.isEmpty(path)) {
            throw new ConfigurationException(JCR_PATH, "JCR Repository storage path name must be specified");
        } else if (!path.startsWith("/")) {
            throw new ConfigurationException(JCR_PATH, "Repository path must be an absolute path.");
        }

        folders = PropertiesUtil.toString(props.get(FOLDERS), null);
        if (StringUtils.isEmpty(folders)) {
            throw new ConfigurationException(FOLDERS, "Folder segmentation for tweets must be specfiied.");
        } else {
            try {
                new SimpleDateFormat(folders).format(new Date());
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException(FOLDERS, "Folder segmentation pattern is invalid.", ex);
            }
        }
    }

    private String buildPath(String... tokens) {
        LOG.debug("JcrTwitterDao - building Path ");
        return StringUtils.join(tokens, "/");
    }

    protected void bindResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
        this.resourceResolverFactory = resourceResolverFactory;
    }

    protected void unbindResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
        if (this.resourceResolverFactory == resourceResolverFactory) {
            this.resourceResolverFactory = null;
        }
    }
}
