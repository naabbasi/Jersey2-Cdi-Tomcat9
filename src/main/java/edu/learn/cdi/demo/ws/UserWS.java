package edu.learn.cdi.demo.ws;

import edu.learn.cdi.demo.bean.ServiceBean;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/users")
@io.swagger.annotations.Api(value = "user")
@RequestScoped
public class UserWS {
    private ServiceBean service;

    public UserWS() {
    }

    @Inject
    public UserWS(ServiceBean service) {
        this.service = service;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public String sayHello(){
        return "Hello, I m working ...";
    }

    @GET
    @Path("/userInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String userInfo(){
        return "User info: " + this.service.doWork(1,2);
    }

    @GET
    @Path("sse/events")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getServerSentEvent(){
        final EventOutput eventOutput = new EventOutput();
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    // ... code that waits 1 second
                    final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
                    eventBuilder.name("message-to-client");
                    eventBuilder.data(String.class,"Hello world " + i + "!");
                    final OutboundEvent event = eventBuilder.build();
                    eventOutput.write(event);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error when writing the event.", e);
            } finally {
                try {
                    eventOutput.close();
                } catch (IOException ioClose) {
                    throw new RuntimeException("Error when closing the event output.", ioClose);
                }
            }
        }).start();
        return eventOutput;
    }
}