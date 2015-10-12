
package com.github.bstopp.kcjug.sling.twitter.servlet;

import java.io.IOException;

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
import com.github.bstopp.kcjug.sling.twitter.dao.AccountDao;

@SlingServlet(paths = { "/bin/twitter/account" })
public class ShowAccountServlet extends SlingSafeMethodsServlet {

    private static final long   serialVersionUID = -8280901137017777540L;

    private static final Logger LOG              = LoggerFactory.getLogger(ShowAccountServlet.class);

    @Reference
    private AccountDao          accountDao;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {

        response.setContentType("application/json");
        ObjectMapper om = new ObjectMapper();
        om.configure(Feature.AUTO_CLOSE_TARGET, false);
        String screenName = request.getParameter("screenName");
        Account account = accountDao.findAccount(screenName);
        try {
            om.writeValue(response.getOutputStream(), account);
        } catch (JsonProcessingException ex) {
            LOG.error("Error occured writing JSON.", ex);
        }
    }
}
