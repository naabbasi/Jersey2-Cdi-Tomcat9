package edu.learn.cdi.demo.ws.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import edu.learn.cdi.demo.bean.ServiceBean;
import edu.learn.cdi.demo.listener.metrics.WebMetricsListener;
import edu.learn.cdi.demo.ws.UserWS;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RestController extends ResourceConfig {
    public RestController(){
        register(ResponseCorsFilter.class);
        registerJerseyMetrics();
        registerClasses(getResourcesInstance());
    }

    private Set<Class<?>> getResourcesInstance() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        Class jsonProvider = null;
        try {
            jsonProvider = Class.forName("org.glassfish.jersey.jackson.JacksonFeature");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        resources.add(jsonProvider);
        resources.add(UserWS.class);

        return resources;
    }

    private void registerJerseyMetrics() {

        register(new InstrumentedResourceMethodApplicationListener(WebMetricsListener.METRIC_REGISTRY));

        /**
         * Create a meter for metering of requests
         * It will get incremented by {@link com.softpak.filter.URebalAuthorizationFilter}
         */
        WebMetricsListener.METRIC_REGISTRY.meter("requests");

        ConsoleReporter.forRegistry(WebMetricsListener.METRIC_REGISTRY)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start(1, TimeUnit.MINUTES);
    }
}
