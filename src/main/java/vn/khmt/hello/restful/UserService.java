package vn.khmt.hello.restful;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import vn.khmt.db.ConnectToSQL;
import vn.khmt.entity.User;

/**
 *
 * @author TheNhan
 */
@Path("/user")
public class UserService {

    private static final String AUTHENTICATION_HEADER = "Authorization";
    private static final int ADMIN_STATUS = 3;

    private static final String TYPE = ConnectToSQL.POSTGRESQL;
    private static final String HOST = "ec2-54-227-253-228.compute-1.amazonaws.com";
    private static final String USER = "uzufecmqojhnyx";
    private static final String PASS = "WPJGueUbd3npLKslU2BEUOmMHx";
    private static final String DBNAME = "d8viikojj42e3b";

    private static final ConnectToSQL dbCon = new ConnectToSQL(TYPE, HOST, DBNAME, USER, PASS);

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int id, @Context HttpHeaders httpHeaders) {
        String authCredentials = httpHeaders.getRequestHeaders().getFirst(AUTHENTICATION_HEADER);
        User authenticated = authenticate(authCredentials);
        if (authenticated != null) {
            if (authenticated.getId() == id || authenticated.getStatus() == ADMIN_STATUS) {
                User u = dbCon.getUser(id);
                if (u != null) {
                    return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity(u).build());
                } else {
                    return CORSFilter.configResponseHeader(Response.status(Response.Status.NOT_FOUND).entity(null).build());
                }
            } else {
                return CORSFilter.configResponseHeader(Response.status(Response.Status.UNAUTHORIZED).entity(null).build());
            }
        } else {
            return CORSFilter.configResponseHeader(Response.status(Response.Status.FORBIDDEN).entity(null).build());
        }
    }

    @GET
    @Path("/all/{page : (\\w+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserList(@PathParam("page") Integer page, @Context HttpHeaders httpHeaders) {
        if (page == null || page <= 0) {
            page = 1;
        }
        String authCredentials = httpHeaders.getRequestHeaders().getFirst(AUTHENTICATION_HEADER);
        User authenticated = authenticate(authCredentials);
        if (authenticated != null) {
            if (authenticated.getStatus() == ADMIN_STATUS) {
                List<User> res = dbCon.getUserList(page);
                if (res != null) {
                    return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity(new GenericEntity<List<User>>(res) {
                    }).build());
                } else {
                    return CORSFilter.configResponseHeader(Response.status(Response.Status.NOT_FOUND).entity(null).build());
                }
            } else {
                return CORSFilter.configResponseHeader(Response.status(Response.Status.UNAUTHORIZED).entity(null).build());
            }
        } else {
            return CORSFilter.configResponseHeader(Response.status(Response.Status.FORBIDDEN).entity(null).build());
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User u) {
        boolean res = dbCon.addUser(u.getUsername(), u.getPassword(), u.getEmail(), u.getStatus(), u.getName());
        return CORSFilter.configResponseHeader(Response.status(201).entity("{\"result\":\"" + res + "\"}").build());
    }

    @PUT
    @Path("/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeName(User u, @Context HttpHeaders httpHeaders) {
        String authCredentials = httpHeaders.getRequestHeaders().getFirst(AUTHENTICATION_HEADER);
        User authenticated = authenticate(authCredentials);
        if (authenticated != null) {
            if (authenticated.getId() == u.getId() || authenticated.getUsername().equals(u.getUsername())) {
                boolean output = dbCon.renameUser(authenticated.getId(), u.getName());
                return CORSFilter.configResponseHeader(Response.status(Response.Status.OK).entity("{\"result\":\"" + output + "\"}").build());
            } else {
                return CORSFilter.configResponseHeader(Response.status(Response.Status.UNAUTHORIZED).entity(null).build());
            }
        } else {
            return CORSFilter.configResponseHeader(Response.status(Response.Status.FORBIDDEN).entity(null).build());
        }
    }

    public User authenticate(String authCredentials) {
        if (authCredentials == null) {
            return null;
        }
        String encodedUserPassword = authCredentials.replaceFirst("Basic ", "");
        String usernameAndPassword = "";
        // Decode 
        try {
            byte[] decodedBytes = DatatypeConverter.parseBase64Binary(encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        String[] token = usernameAndPassword.split(":");
        return dbCon.getUser(token[0], token[1]);
    }

}
