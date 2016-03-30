package vn.khmt.hello.restful;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hello")
public class HelloService {

    @GET
    @Path("/{name}")
    public Response hello(@PathParam("name") String name) {
        String msg = "Hello " + name;
        return Response.status(Response.Status.OK).entity(msg).build();
    }

    @GET
    @Path("/json/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloInJSON(@PathParam("name") String name) {
        String msg = "{\"message\" : \"Hello " + name + "\"}";
        return Response.status(Response.Status.OK).entity(msg).build();
    }
}
