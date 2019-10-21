package edu.learn.cdi.demo.ws;

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import edu.learn.cdi.demo.bean.ServiceBean;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.credentials.authenticator.UserInfoOidcAuthenticator;
import org.pac4j.oidc.profile.OidcProfile;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    private OidcClient getOidcClient(String clientName) {
        OidcConfiguration configuration = new OidcConfiguration();
        configuration.setClientId("test-client");
        configuration.setSecret("test-secret");
        configuration.setDiscoveryURI("http://localhost:4444/.well-known/openid-configuration");
        configuration.setScope("openid");
        configuration.setMaxAge(3600);
        /*configuration.setConnectTimeout(Integer.parseInt(getProperty("oauth.timeout")));
        configuration.setReadTimeout(Integer.parseInt(getProperty("oauth.timeout")));*/
        configuration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

        OidcClient client = new OidcClient<>(configuration);
        client.setCallbackUrlResolver(new NoParameterCallbackUrlResolver());
        client.setCallbackUrl("http://localhost:8080/cdi/services/users/oauth/callback");

        return client;
    }

    private CommonProfile getProfile(String account , HttpServletRequest request, HttpServletResponse response) {
        OidcClient client = this.getOidcClient(account);
        WebContext context = new J2EContext(request, response);
        Credentials credentials = client.getCredentials(context);
        return client.getUserProfile(credentials, context);
    }

    private Response redirectToLogin(String userFirstName, String errorMessage) throws URISyntaxException {
        String appUri = "";
        String redirectURI;

        if (!errorMessage.isEmpty()) {
            redirectURI = String.format("%s?errorMessage=%s&oauthLogin=1", appUri, URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
        }
        else {
            redirectURI = String.format("%s?userFirstName=%s&oauthLogin=1", appUri, userFirstName);
        }

        URI uri = new URI(redirectURI);
        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

    @GET
    //@Path("/oauth/{clientName: [a-zA-Z]*}")
    @Path("/oauth")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response oauth(@Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException {

        try {
            if ("blueleaf".equalsIgnoreCase("blueleaf")) {
                System.out.println("oauth(...): disabling SSL validation for this client due to invalid ssl certificates");
                // Note: Disabling SSL validation because Blueleaf's Discovery URLs have invalid ssl certificate.
            }

            OidcClient client = this.getOidcClient("blueleaf");
            WebContext context = new J2EContext(request, response);
            URI uri = new URI(client.getRedirectAction(context).getLocation());

            return  Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
        } catch(TechnicalException e) {
            e.printStackTrace();
            return this.redirectToLogin("", "Unable to generate OAuth2 request.");
        }
    }

    @GET
    //@Path("/oauth/callback/{clientName: [a-zA-Z]*}")
    @Path("/oauth/callback")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response oauthRedirect(@Context HttpServletRequest request, @Context HttpServletResponse response, @DefaultValue("") @QueryParam( "error" ) String error,
                                  @DefaultValue("") @QueryParam( "error_description" ) String errorDescription, @DefaultValue("") @QueryParam( "error_hint" ) String errorHint) throws URISyntaxException {

        if (!error.isEmpty()) {
            System.out.println("oauthRedirect(...): OAuth Authentication Failed");
            System.out.println(error);
            System.out.println(errorDescription);
            System.out.println(errorHint);
            return this.redirectToLogin("", "OAuth Error: " + error + ". Contact your administrator.");
        }

        AccessToken accessToken;
        try {
            CommonProfile profile = this.getProfile("blueleaf", request, response);
            System.out.println(profile);

            Map<String, Object> attributes = profile.getAttributes();
            System.out.println(attributes.get("access_token"));
            System.out.println(attributes);

            /*String email;
            if ("blueleaf".equalsIgnoreCase("blueleaf")) {
                email = profile.getId(); // Blueleaf only allow "sub" scope and supplies email in "sub" property.
            }
            else {
                email = profile.getEmail();
            }*/


            accessToken = ((OidcProfile) profile).getAccessToken();
            System.out.println(accessToken);


            OidcClient client = this.getOidcClient("blueleaf");
            OidcConfiguration configuration = client.getConfiguration();
            UserInfoOidcAuthenticator authenticator = new UserInfoOidcAuthenticator(configuration);
            this.validateToken(authenticator, request, response);
            System.out.println("oauthRedirect(...) email retrieved via OAth profile" );
        } catch(Exception e) {
            e.printStackTrace();
            return this.redirectToLogin("", "Authentication failed.");
        }

        return  Response.status(Response.Status.OK).entity(accessToken.toJSONString()).build();
    }

    private void validateToken(Authenticator authenticator, HttpServletRequest request, HttpServletResponse response) {
        // REST authentication with JWT token passed in the url as the "token" parameter
        ParameterClient parameterClient = new ParameterClient("token", authenticator);
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        HeaderClient httpClient = new HeaderClient("Authorization", "Bearer ", authenticator);
        System.out.println(httpClient.getHeaderName());

        // if the 'Authorization' header is passed with the 'Basic token' value
        HeaderClient client = new HeaderClient("Authorization", "Basic ", (credential, ctx) -> {
            String token = ((TokenCredentials) credential).getToken();
            // check the token and create a profile
            if ("goodToken".equals(token)) {
                CommonProfile profile = new CommonProfile();
                profile.setId("myId");
                // save in the credentials to be passed to the default AuthenticatorProfileCreator
                credential.setUserProfile(profile);
            }
        });
        
        /*Done by owais*/
        OidcConfiguration config = LoginWS.getOidcConfiguration(clientName);
        UserInfoOidcAuthenticator authenticator = new UserInfoOidcAuthenticator(config);
        WebContext context = new J2EContext(request, response);
        TokenCredentials credentials = new TokenCredentials(oauthToken);
        authenticator.validate(credentials, context);
        OidcProfile profile = (OidcProfile) credentials.getUserProfile();
        String email = profile.getEmail(); 
    }
}
