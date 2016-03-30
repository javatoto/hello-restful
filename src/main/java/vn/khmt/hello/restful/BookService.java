package vn.khmt.hello.restful;

import vn.khmt.entity.Book;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import vn.khmt.db.ConnectToSQL;
import vn.khmt.entity.Author;

@Path("/book")
public class BookService {

    private static final String TYPE = ConnectToSQL.POSTGRESQL;
    private static final String HOST = "ec2-54-227-253-228.compute-1.amazonaws.com";
    private static final String USER = "uzufecmqojhnyx";
    private static final String PASS = "WPJGueUbd3npLKslU2BEUOmMHx";
    private static final String DBNAME = "d8viikojj42e3b";

    private static final ConnectToSQL dbCon = new ConnectToSQL(TYPE, HOST, DBNAME, USER, PASS);

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response book() {
        Book b = new Book();
        b.setId(1);
        Author a = new Author();
        a.setId(11);
        a.setAge(50);
        a.setName("Nguyen Nhat Anh");
        b.setAuthor(a);
        b.setTitle("Toi thay hoa vang");
        b.setYear(2012);
        return Response.status(Response.Status.OK).entity(b).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response book(@PathParam("id") int id) {
        Book b = dbCon.getBook(id);
        if (b != null) {
            return Response.status(Response.Status.OK).entity(b).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(null).build();
        }
    }

    @GET
    @Path("/all/{page}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bookList(@PathParam("page") int page) {
        if (page < 0) {
            page = 1;
        }
        List<Book> bl = dbCon.getBookList(page);
        if (bl != null) {
            return Response.status(Response.Status.OK).entity(new GenericEntity<List<Book>>(bl) {
            }).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(null).build();
        }
        //return Response.status(Response.Status.OK).entity(bl).build();
    }

    @POST
    @Path("/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response receiveMessage(String message) {
        System.out.println("Message received = " + message);
        return Response.status(Response.Status.CREATED).entity("Message received").build();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveBook(Book b) {
        dbCon.createBook(b);
        return Response.status(Response.Status.CREATED).entity("Book created").build();
    }
}
