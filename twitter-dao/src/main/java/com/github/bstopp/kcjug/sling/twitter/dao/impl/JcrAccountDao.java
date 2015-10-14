
package com.github.bstopp.kcjug.sling.twitter.dao.impl;

import java.util.Dictionary;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
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
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.JcrResourceUtil;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bstopp.kcjug.sling.twitter.Account;
import com.github.bstopp.kcjug.sling.twitter.dao.AccountDao;
import com.github.bstopp.kcjug.sling.twitter.dao.PersistenceException;

//@formatter:off
@Service
@Component(metatype = true, enabled= true, immediate = true,
  policy = ConfigurationPolicy.REQUIRE,
  label= "KCJUG Twitter Account DAO")
@Properties({
  @Property(
      name = Constants.SERVICE_DESCRIPTION,
      value = "Saves account information to the JCR."),
})
//@formatter:on
public class JcrAccountDao implements AccountDao {

    private static final Logger     LOG                  = LoggerFactory.getLogger(JcrAccountDao.class);

    @Property(label = "Repository Path")
    private static final String     REPOSITORY_PATH_PROP = "repository.path";

    private String                  repositoryPath;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static final String     NODE_TYPE            = "nt:unstructured";

    @Override
    @SuppressWarnings("deprecation")
    public void save(Account account) throws PersistenceException {
        LOG.debug("Saving account({})", account.getScreenName());
        Session sesn = null;
        try {
            String screenName = account.getScreenName();
            sesn = resourceResolverFactory.getAdministrativeResourceResolver(null).adaptTo(Session.class);
            Node parent = JcrResourceUtil.createPath(repositoryPath, NODE_TYPE, NODE_TYPE, sesn, true);
            Node node = JcrResourceUtil.createPath(parent, screenName, NODE_TYPE, NODE_TYPE, true);
            node.addMixin(NodeType.MIX_CREATED);

            node = parent.getNode(screenName);
            LOG.debug("Adding account ({}) properties.", screenName);
            node.setProperty(Account.USER_ID_PROP, account.getUserId());
            node.setProperty(Account.SCREEN_NAME_PROP, screenName);
            node.setProperty(Account.PROFILE_URL_PROP, account.getProfileUrl());
            node.setProperty(Account.PROFILE_IMAGE_PROP, account.getProfileImage());
            node.setProperty(Account.DESCRIPTION_PROP, account.getDescription());
            node.setProperty(Account.FOLLOWERS_PROP, account.getFollowers());
            node.setProperty(Account.FRIENDS_PROP, account.getFriends());
            sesn.save();
        } catch (RepositoryException ex) {
            LOG.error("An error occured while trying to save the Account", ex);
        } catch (LoginException ex) {
            LOG.error("An error occured while trying to save the Account", ex);
        } finally {
            if (sesn != null) {
                sesn.logout();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Account findAccount(String screenName) {

        Session sesn = null;
        Account account = new Account();

        if (StringUtils.isBlank(screenName)) {
            return account;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * from [nt:unstructured] as acct ");
        sql.append("WHERE acct.[").append(Account.SCREEN_NAME_PROP).append("] = '").append(screenName).append("' ");
        sql.append("AND ISDESCENDANTNODE(acct, '").append(repositoryPath).append("') ");
        LOG.debug("SQL: {}", sql.toString());
        try {
            sesn = resourceResolverFactory.getAdministrativeResourceResolver(null).adaptTo(Session.class);
            QueryManager qm = sesn.getWorkspace().getQueryManager();
            Query query = qm.createQuery(sql.toString(), Query.JCR_SQL2);
            QueryResult result = query.execute();
            NodeIterator itr = result.getNodes();
            if (itr.hasNext()) {
                Node node = itr.nextNode();
                account.setUserId(node.getProperty(Account.USER_ID_PROP).getLong());
                account.setScreenName(node.getProperty(Account.SCREEN_NAME_PROP).getString());
                account.setProfileUrl(node.getProperty(Account.PROFILE_URL_PROP).getString());
                account.setProfileImage(node.getProperty(Account.PROFILE_IMAGE_PROP).getString());
                account.setDescription(node.getProperty(Account.DESCRIPTION_PROP).getString());
                account.setFollowers(((Long) node.getProperty(Account.FOLLOWERS_PROP).getLong()).intValue());
                account.setFriends(((Long) node.getProperty(Account.FRIENDS_PROP).getLong()).intValue());
            }

        } catch (RepositoryException ex) {
            LOG.error("An error ocucred while trying to query the JCR.", ex);
        } catch (LoginException ex) {
            LOG.error("An error occured while trying to save the Account", ex);
        } finally {
            if (sesn != null) {
                sesn.logout();
            }
        }
        return account;
    }

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        LOG.debug("Starting DAO Service");

        Dictionary<String, Object> props = context.getProperties();

        repositoryPath = PropertiesUtil.toString(props.get(REPOSITORY_PATH_PROP), "");
        if (StringUtils.isBlank(repositoryPath)) {
            throw new ConfigurationException(REPOSITORY_PATH_PROP, "Repository path must be specified.");
        }
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
