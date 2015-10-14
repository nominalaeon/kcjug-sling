/**
 *
 */

package com.github.bstopp.kcjug.sling.twitter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tweet
 *
 * @author Bryan Stopp (bstopp)
 */
@Model(adaptables = { Resource.class })
public class Tweet {

    public static final String  STATUS_ID   = "statusId";
    public static final String  SCREEN_NAME = "screenName";
    public static final String  CREATED     = "created";
    public static final String  FORMATTED   = "formatted";
    public static final String  TEXT        = "text";
    public static final String  RETWEET     = "retweet";
    public static final String  ORIGINAL    = "original";
    private static final Logger LOG         = LoggerFactory.getLogger(Tweet.class);

    @Inject
    @Named(STATUS_ID)
    private Long                statusId;

    @Inject
    @Named(SCREEN_NAME)
    private String              screenName;

    @Inject
    @Named(CREATED)
    private Calendar            created;

    @Inject
    @Named(FORMATTED)
    private String              formattedText;

    @Inject
    @Named(TEXT)
    private String              text;

    @Inject
    @Named(RETWEET)
    private boolean             retweet;

    @Inject
    @Named(ORIGINAL)
    @Optional
    private Tweet               original;

    private Resource            resource;

    public Tweet() {

    }

    public void persist() throws PersistenceException {
        LOG.debug("Tweet - persist");
        if (resource == null) {
            throw new IllegalStateException("Resource must not be null.");
        }

        ResourceResolver resourceResolver = resource.getResourceResolver();
        Resource tweet = ResourceUtil.getOrCreateResource(resourceResolver, resource.getPath(), "nt:unstructured",
                "nt:unstructured", false);
        ModifiableValueMap mvm = tweet.adaptTo(ModifiableValueMap.class);
        mvm.put(STATUS_ID, statusId);
        mvm.put(SCREEN_NAME, screenName);
        mvm.put(CREATED, created);
        mvm.put(FORMATTED, formattedText);
        mvm.put(TEXT, text);
        mvm.put(RETWEET, retweet);
        resourceResolver.commit();
        if (retweet) {
            Resource origres = resourceResolver.resolve(resource.getPath() + "/original");
            original.setResource(origres);
            original.persist();
        }
        resourceResolver.commit();
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getFormattedCreated() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(created.getTime());
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public void setFormattedText(String formatted) {
        formattedText = formatted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRetweet() {
        return retweet;
    }

    public void setRetweet(boolean retweet) {
        this.retweet = retweet;
    }

    public Tweet getOriginal() {
        return original;
    }

    public void setOriginal(Tweet original) {
        this.original = original;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((statusId == null) ? 0 : statusId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tweet other = (Tweet) obj;
        if (statusId == null) {
            if (other.statusId != null) {
                return false;
            }
        } else if (!statusId.equals(other.statusId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Tweet [statusId=" + statusId + ", screenName=" + screenName + ", created=" + created.getTime()
                + ", formattedText=" + formattedText + ", text=" + text + ", retweet=" + retweet + ", original="
                + original + "]";
    }

}
