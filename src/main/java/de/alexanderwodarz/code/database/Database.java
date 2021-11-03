package de.alexanderwodarz.code.database;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.List;

@Getter
public class Database {

    private String user, password, db, host, dburl, path, type;
    private Connection connection;

    @SneakyThrows
    public Database(String host, String username, String password, String db) {
        this.type = "mysql";
        this.user = username;
        this.password = password;
        this.db = db;
        this.host = host;
        this.dburl = "jdbc:mysql://" + host + "/" + db
                + "?useUnicode=true&autoReconnect=true&characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        initDBConnection();
    }

    @SneakyThrows
    public Database(String path){
        this.type = "sqlite";
        this.path = path;
        try {
            System.out.println("Creating Connection to Database...");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            if (!connection.isClosed())
                System.out.println("...Connection established");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return type;
    }

    @SneakyThrows
    public static Database getLightDatabase(String path) {
        return new Database(path);
    }

    public ResultSet query(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException throwables) {
        }
        return null;
    }

    private void initDBConnection() {
        try {
            if (connection != null)
                return;
            System.out.println("Creating Connection to Database...");

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(this.dburl, user, this.password);
            if (!connection.isClosed())
                System.out.println("...Connection established");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public <abstractTable> abstractTable getTable(Class<abstractTable> table) {
        return table.getDeclaredConstructor(Database.class).newInstance(this);
    }

    public void update(String query, List<Object> values) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            int i = 1;
            if (values != null)
                for (Object value : values) {
                    stmt.setObject(i, value);
                    i++;
                }
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
