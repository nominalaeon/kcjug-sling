
package apps.twitter.components.tweets;

import java.util.List;

import javax.script.Bindings;

import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bstopp.kcjug.sling.twitter.Tweet;
import com.github.bstopp.kcjug.sling.twitter.dao.TweetDao;

public class TweetList implements Use {

    private static final Logger LOG = LoggerFactory.getLogger(TweetList.class);

    Bindings                    bindings;

    private TweetDao            tweetDao;

    @Override
    public void init(Bindings bindings) {

        SlingScriptHelper slinghelper = (SlingScriptHelper) bindings.get("sling");
        if (slinghelper != null) {
            LOG.error("Got the Sling Helper." + slinghelper.toString());
        }
        TweetDao[] daoList = slinghelper.getServices(TweetDao.class, "(screen.name=StoppThinking)");

        if ((daoList != null) && (daoList.length > 0)) {
            tweetDao = daoList[0];
        } else {
            LOG.error("Dao List is empty or null. " + daoList);
        }
        TweetDao dao = slinghelper.getService(TweetDao.class);
        if (dao != null) {
            tweetDao = dao;
        } else {
            LOG.error("Dao is null. " + dao);
        }
    }

    public List<Tweet> getTweets() {
        return tweetDao.getTweets(20);
    }

}
