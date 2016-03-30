package vn.khmt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import vn.khmt.entity.Author;
import vn.khmt.entity.Book;
import vn.khmt.entity.User;

/**
 *
 * @author TheNhan
 */
public class ConnectToSQL {

    public static final String SQLSERVER = "sqlserver";
    public static final String SQLSERVERDRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String MYSQL = "mysql";
    public static final String MYSQLDRIVER = "com.mysql.jdbc.Driver";
    public static final String POSTGRESQL = "postgresql";
    public static final String POSTGRESQLDRIVER = "org.postgresql.Driver";

    Connection dbConnection = null;

    public ConnectToSQL(String type, String host, String dbname, String user, String pwd) {
        this.dbConnection = getDBConnection(type, host, dbname, user, pwd);
    }

    private Connection getDBConnection(String type, String host, String dbname, String user, String pwd) {
        if (type != null && !type.isEmpty()) {
            try {
                if (type.equalsIgnoreCase(SQLSERVER)) {
                    Class.forName(SQLSERVERDRIVER);
                    dbConnection = DriverManager.getConnection("jdbc:sqlserver://" + host + ":1433;database=" + dbname + ";sendStringParametersAsUnicode=true;useUnicode=true;characterEncoding=UTF-8;", user, pwd);
                } else if (type.equalsIgnoreCase(MYSQL)) {
                    Class.forName(MYSQLDRIVER);
                    dbConnection = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + dbname, user, pwd);
                } else if (type.equalsIgnoreCase(POSTGRESQL)) {
                    Class.forName(POSTGRESQLDRIVER);
                    Properties props = new Properties();
                    props.put("user", user);
                    props.put("password", pwd);
                    props.put("sslmode", "require");
                    dbConnection = DriverManager.getConnection("jdbc:postgresql://" + host + ":5432/" + dbname + "?sslmode=require&user=" + user + "&password=" + pwd);
                }
                return dbConnection;
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return dbConnection;
    }

    public Book getBook(int id) {
        try {
            String SQL = "SELECT id, title, year FROM public.book WHERE id = " + id;
            String authorSQL = "SELECT id, name, age FROM public.author WHERE id IN (SELECT author_id FROM public.book_author WHERE book_id = " + id + ")";
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            if (rs.next()) {
                Book res = new Book();
                res.setId(rs.getInt(1));
                res.setTitle(rs.getString(2));
                res.setYear(rs.getInt(3));

                stmt = this.dbConnection.createStatement();
                ResultSet rs2 = stmt.executeQuery(authorSQL);
                if (rs2.next()) {
                    Author a = new Author();
                    a.setId(rs2.getInt(1));
                    a.setName(rs2.getString(2));
                    a.setAge(rs2.getInt(3));
                    res.setAuthor(a);
                }
                return res;
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return null;
    }

    public List<Book> getBookList(int page) {
        try {
            String SQL = "SELECT id, title, year FROM public.book ORDER BY title LIMIT 10 OFFSET " + (page - 1) * 10;

            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);

            List<Book> l = new ArrayList<>();
            while (rs.next()) {
                Book res = new Book();
                res.setId(rs.getInt(1));
                res.setTitle(rs.getString(2));
                res.setYear(rs.getInt(3));

                String authorSQL = "SELECT id, name, age FROM public.author WHERE id IN (SELECT author_id FROM public.book_author WHERE book_id = " + rs.getInt(1) + ")";
                stmt = this.dbConnection.createStatement();
                ResultSet rs2 = stmt.executeQuery(authorSQL);
                if (rs2.next()) {
                    Author a = new Author();
                    a.setId(rs2.getInt(1));
                    a.setName(rs2.getString(2));
                    a.setAge(rs2.getInt(3));
                    res.setAuthor(a);
                }
                l.add(res);
            }
            return l;
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return null;
    }

    public boolean createBook(Book b) {
        PreparedStatement preparedStatement = null;
        String insertTableSQL = "INSERT INTO public.book(id, title, year) VALUES(?,?,?)";
        try {
            if (b != null) {
                preparedStatement = this.dbConnection.prepareStatement(insertTableSQL);
                preparedStatement.setLong(1, b.getId());
                preparedStatement.setString(2, b.getTitle());
                //preparedStatement.setString(3, b.getAuthor());
                preparedStatement.setInt(3, b.getYear());
            }

            // execute insert SQL stetement
            if (preparedStatement != null) {
                int res = preparedStatement.executeUpdate();
                return res == 1;
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException sqle) {
                    System.err.println(sqle.getMessage());
                }
            }
        }
        return false;
    }

    public User getUser(String username, String password) {
        try {
            if (username != null && password != null) {
                String SQL = "SELECT id, username, password, email, status, name FROM public.user WHERE username = '" + username + "' AND password = '" + password + "';";
                Statement stmt = this.dbConnection.createStatement();
                ResultSet rs = stmt.executeQuery(SQL);

                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt(1));
                    u.setUsername(rs.getString(2));
                    u.setPassword(rs.getString(3));
                    u.setEmail(rs.getString(4));
                    u.setStatus(rs.getInt(5));
                    u.setName(rs.getString(6));
                    return u;
                } else {
                    return null;
                }
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return null;
    }

    public User getUser(int id) {
        try {
            String SQL = "SELECT id, username, email, name, status FROM public.user WHERE id = " + id;
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt(1));
                u.setUsername(rs.getString(2));
                u.setEmail(rs.getString(3));
                u.setName(rs.getString(4));
                u.setStatus(rs.getInt(5));
                return u;
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return null;
    }

    public List<User> getUserList(int page) {
        try {
            String SQL = "SELECT id, username, email, name, status FROM public.user ORDER BY name LIMIT 10 OFFSET " + (page - 1) * 10;
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);

            List<User> res = new ArrayList<>();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt(1));
                u.setUsername(rs.getString(2));
                u.setEmail(rs.getString(3));
                u.setName(rs.getString(4));
                u.setStatus(rs.getInt(5));
                res.add(u);
            }
            return res;
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return null;
    }

    public boolean addUser(String username, String password, String email, int status, String name) {
        try {
            String SQL = "INSERT INTO public.user VALUES ((SELECT MAX(id) + 1 FROM public.user),'" + username + "','" + password + "','" + email + "'," + status + ",'" + name + "');";
            Statement stmt = this.dbConnection.createStatement();
            return stmt.execute(SQL);
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return false;
    }

    public boolean renameUser(int id, String name) {
        PreparedStatement preparedStatement = null;
        String updateTableSQL = "UPDATE public.user SET name = ? WHERE id = ?";
        try {
            //con = getDBConnection();
            if (name != null) {
                preparedStatement = this.dbConnection.prepareStatement(updateTableSQL);
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, id);
            }

            // execute insert SQL stetement
            if (preparedStatement != null) {
                System.out.println("updateName : " + updateTableSQL);
                int res = preparedStatement.executeUpdate();
                return res == 1;
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException sqle) {
                    System.err.println(sqle.getMessage());
                }
            }
        }
        return false;
    }

    public int countUser() {
        try {
            String SQL = "SELECT COUNT(*) FROM public.user;";
            Statement stmt = this.dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return 0;
    }
}
