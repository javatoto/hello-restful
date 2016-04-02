package vn.khmt.hello.restful;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hello")
public class HelloService {

    private static int id = 0;

    @GET
    @Path("/{name}")
    public Response hello(@PathParam("name") String name) {
        String msg = "Hello " + name;
        return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity(msg).build());
    }

    @GET
    @Path("/json/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloInJSON(@PathParam("name") String name) {
        String msg = "{\"message\" : \"Hello " + name + "\"}";
        return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity(msg).build());
    }

    @GET
    @Path("/sensor/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setTemperatureFromSensor() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Random r = new Random();
        id++;
        sb.append("{");
        sb.append("\"id\": ");
        sb.append(id);
        sb.append(", ");
        sb.append("\"time\": \"");
        sb.append(sdf.format(new Date()));
        sb.append("\", ");
        sb.append("\"temperature\": ");
        sb.append(r.nextFloat() * 5 + 30);
        sb.append(", ");
        sb.append("\"humidity\": ");
        sb.append(r.nextFloat() * 30 + 40);
        sb.append("}");
        String msg = sb.toString();
        System.out.println(msg);
        return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity(msg).build());
    }
}
