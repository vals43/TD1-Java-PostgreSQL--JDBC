package PROG3.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private final String URL = "jdbc:postgresql://localhost:5432/product_management_db";
    private final String USERNAME = "product_manager_user";
    private final String PASSWORD = "123456";

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}