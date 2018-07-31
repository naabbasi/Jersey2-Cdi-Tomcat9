package edu.learn.cdi.demo.ws.config;

import edu.learn.cdi.demo.bean.ServiceBean;
import edu.learn.cdi.demo.ws.UserWS;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Set;

public class RestController extends ResourceConfig {
    public RestController(){
        register(ResponseCorsFilter.class);
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
}
