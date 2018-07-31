package edu.learn.cdi.demo.ws.config;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseCorsFilter implements ContainerResponseFilter {

    public ResponseCorsFilter() {
        System.out.println("ServerResponseFilter initialization");
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        containerResponseContext.getHeaders().add("X-Powered-By", "Jersey :-)");
        containerResponseContext.getHeaders().add("Access-Control-Allow-Origin", "http://noman");
        containerResponseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}