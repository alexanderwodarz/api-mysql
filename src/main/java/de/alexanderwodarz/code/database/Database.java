package de.alexanderwodarz.code.database;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.List;

@Getter
public class Database {

    private String user, password, db, host, dburl, path, type, connectionString = "?useUnicode=true&autoReconnect=true&characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private Connection connection;

    @SneakyThrows
    public Database(String host, String username, String password, String db) {
        this.type = "mysql";
        this.user = username;
        this.password = password;
        this.db = db;
        this.host = host;
        this.dburl = "jdbc:mysql://" + host + "/" + db
                + connectionString;
        initDBConnection();
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
        } catch (SQLException throwables) {
        }
        return null;
    }

    private void initDBConnection() {
        initDBConnection(false);
    }

    private void initDBConnection(boolean notTryAgain) {
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
            if (!notTryAgain) {
                System.out.println("konnte nicht verbinden versuche datenbank zu erstellen");
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://" + host, user, this.password);
                    connection.prepareStatement("CREATE DATABASE "+db).executeUpdate();
                    initDBConnection(true);
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public <abstractTable> abstractTable getTable(Class<abstractTable> table) {
        return table.getDeclaredConstructor(Database.class).newInstance(this);
    }

    public SQLWarning update(String query, List<Object> values) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            int i = 1;
            if (values != null)
                for (Object value : values) {
                    stmt.setObject(i, value);
                    i++;
                }
            stmt.executeUpdate();
            return stmt.getWarnings();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
