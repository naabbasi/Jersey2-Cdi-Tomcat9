# Jersey 2.x, CDI 2.x, Swagger
This project contains jersey2, cdi, drop wizard metrics, swagger
To generate jersey 2.x services documentation with swagger
<li>Add the following dependency in your pom.xml</li>

    <dependency>
        <groupId>io.swagger</groupId>
        <artifactId>swagger-jersey2-jaxrs</artifactId>
        <version>1.5.21</version>
    </dependency>

<li>Add the following in web.xml</li>
Note: No servlet mapping is required
    
    <servlet>
        <servlet-name>Jersey2Config</servlet-name>
        <servlet-class>io.swagger.jersey.config.JerseyJaxrsConfig</servlet-class>
        <init-param>
            <param-name>api.version</param-name>
            <param-value>1.0.0</param-value>
        </init-param>
        <init-param>
            <param-name>swagger.api.basepath</param-name>
            <param-value>http://localhost:8080/cdi/services</param-value>
        </init-param>
        <load-on-startup>2</load-on-startup>
    </servlet>

<li>Add the register(io.swagger.jaxrs.listing.ApiListingResource.class); and register(io.swagger.jaxrs.listing.SwaggerSerializers.class); in your existing rest configuration</li>

    public class RestController extends ResourceConfig {
        public RestController(){
            register(ResponseCorsFilter.class);
            register(io.swagger.jaxrs.listing.ApiListingResource.class);
            register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
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
    
<li>Add the following line at the top of your rest service e.g. UserWS.java</li>
@io.swagger.annotations.Api(value = "user")<br><br>
Now open the following url<br>
http://localhost:8080/cdi/services/swagger.json<br><br>
<li>Download the swagger ui from the following url</li>
https://github.com/swagger-api/swagger-ui/archive/master.zip<br><br>

<li>Now extract the master.zip and copy the content from dist folder and past into webapp directory</li>
Now open http://localhost:8080/cdi/ in your browser and now you should see swagger page<br>
Now add http://localhost:8080/cdi/services/swagger.json and press Explore button 