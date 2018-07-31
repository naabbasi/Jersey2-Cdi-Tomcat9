package edu.learn.cdi.demo.ws;

import edu.learn.cdi.demo.bean.ServiceBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users")
@RequestScoped
public class UserWS {

    public UserWS(){
    }

    @Inject
    private ServiceBean service;

    @GET
    @Path("/userInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String userInfo(){
        System.out.println(this.service.doWork(1,2));
        return "User info";
    }
}