package org.joget.plugin;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    public final static String VERSION = "9.0.5";
    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        registrationList.add(context.registerService(SectionTabs.class.getName(), new SectionTabs(), null));
        registrationList.add(context.registerService(SectionTabsChild.class.getName(), new SectionTabsChild(), null));
        registrationList.add(context.registerService(LegacyColumns.class.getName(), new LegacyColumns(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}