
package com.github.bstopp.kcjug.sling;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        LOG.debug("Starting Twitter DAO Bundle.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.debug("Stopping Twitter DAO Bundle.");
    }

}
