package vn.khmt.hello.restful;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import vn.khmt.db.ConnectToSQL;

@Path("/webservice")
public class HelloService {

    String host = "localhost";
    String dbname = "bibli";
    String user = "nhanbk";
    String pass = "nhanbk";

    @GET
    @Path("/{param}")
    public Response hello(@PathParam("param") String name) {
        String msg = "Hello " + name;
        return Response.status(Response.Status.OK).entity(msg).build();
    }

    @GET
    @Path("/json/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloInJSON(@PathParam("param") String name) {
        String msg = "{\"message\" : \"Hello " + name + "\"}";
        return Response.status(Response.Status.OK).entity(msg).build();
    }

    @GET
    @Path("/book/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response book() {
        Book b = new Book();
        b.setId(1);
        b.setAuthor("Nguyen Nhat Anh");
        b.setTitle("Toi thay hoa vang");
        b.setYear(2012);
        return Response.status(Response.Status.OK).entity(b).build();
    }

    @GET
    @Path("/book/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response book(@PathParam("id") int id) {
        ConnectToSQL db = new ConnectToSQL(ConnectToSQL.SQLSERVER, host, dbname, user, pass);
        Book b = db.getBook(id);
        return Response.status(Response.Status.OK).entity(b).build();
    }

    @GET
    @Path("/book/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bookList(@Context HttpHeaders httpHeaders) {
        String authCredentials = httpHeaders.getRequestHeaders().getFirst("authorization");
        System.out.println("authCredentials = " + authCredentials);

        if (authenticate(authCredentials) != null) {
            ConnectToSQL db = new ConnectToSQL(ConnectToSQL.SQLSERVER, host, dbname, user, pass);
            List<Book> bl = db.getBookList();
            return Response.status(Response.Status.OK).entity(new GenericEntity<List<Book>>(bl) {
            }).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(null).build();
        }

        //return Response.status(Response.Status.OK).entity(bl).build();
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
        ConnectToSQL db = new ConnectToSQL(ConnectToSQL.SQLSERVER, host, dbname, user, pass);
        return db.getUser(token[0], token[1]);
    }

    @POST
    @Path("/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response receiveMessage(String message) {
        System.out.println("Message received = " + message);
        return Response.status(Response.Status.CREATED).entity("Message received").build();
    }

    @POST
    @Path("/postJSON")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveBook(Book b) {
        ConnectToSQL db = new ConnectToSQL(ConnectToSQL.SQLSERVER, host, dbname, user, pass);
        db.createBook(b);
        return Response.status(Response.Status.CREATED).entity("Book created").build();
    }
}
