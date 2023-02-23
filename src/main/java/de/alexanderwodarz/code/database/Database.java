package de.alexanderwodarz.code.database;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.List;

@Getter
public class Database {

    private String user;
    private String password;
    private String db;
    private String host;
    private String dburl;
    private String path;
    private String type;
    private final String connectionString = "?useUnicode=true&autoReconnect=true&characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private Connection connection;
    private boolean verbose;


    public Database(String host, String username, String password, String db) {
        this(host, username, password, db, false);
    }

    @SneakyThrows
    public Database(String host, String username, String password, String db, boolean verbose) {
        this.type = "mysql";
        this.user = username;
        this.password = password;
        this.db = db;
        this.host = host;
        this.dburl = "jdbc:mysql://" + host + "/" + db
                + connectionString;
        this.verbose = verbose;
        initDBConnection();
    }

    public boolean isVerbose() {
        return verbose;
    }

    @SneakyThrows
    public Database(String path) {
        this.type = "sqlite";
        this.path = path;
        try {
            System.out.println("Creating Connection to Database...");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            connection.setNetworkTimeout(System.out::println, 1000);
            if (!connection.isClosed())
                System.out.println("...Connection established");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static Database getLightDatabase(String path) {
        return new Database(path);
    }

    public String getType() {
        return type;
    }

    public ResultSet query(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException ignored) {
        }
        return null;
    }

    private void initDBConnection() {
        try {
            if (connection != null)
                return;
            System.out.println("Creating Connection to Database...");

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            DriverManager.setLoginTimeout(1);
            connection = DriverManager.getConnection(this.dburl, user, this.password);
            if (!connection.isClosed())
                System.out.println("...Connection established");
        } catch (SQLNonTransientConnectionException e) {
            try {
                String message = e.getCause().getMessage();
                if (message.startsWith("Unknown database")) {
                    System.out.println("datenbank existiert nicht, probiere sie zu erstellen");
                    createDatabase();
                    initDBConnection();
                }
                if (message.startsWith("Communications link failure")) {
                    System.out.println("Connection to database failed");
                    Thread.sleep(5000);
                    initDBConnection();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDatabase() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + host, user, this.password);
            connection.prepareStatement("CREATE DATABASE " + db).executeUpdate();
        } catch (Exception ignored) {
        }
    }

    @SneakyThrows
    public <T> T getTable(Class<T> table) {
        return table.getDeclaredConstructor(Database.class).newInstance(this);
    }

    public PreparedStatement update(String query, List<Object> values) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            if (values != null)
                for (Object value : values) {
                    stmt.setObject(i, value);
                    i++;
                }
            stmt.executeUpdate();
            return stmt;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
