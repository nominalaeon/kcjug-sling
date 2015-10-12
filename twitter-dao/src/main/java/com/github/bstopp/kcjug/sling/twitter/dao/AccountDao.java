
package com.github.bstopp.kcjug.sling.twitter.dao;

import com.github.bstopp.kcjug.sling.twitter.Account;

public interface AccountDao {

    void save(Account account) throws PersistenceException;

    Account findAccount(String screenName);

}
