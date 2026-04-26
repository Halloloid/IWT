package com.market.db;

import java.sql.*;

public class DBConnection {

    // PostgreSQL connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/campus_market";
    private static final String USER = "postgres";
    private static final String PASS = "12345"; 
    private static final String DRIVER = "org.postgresql.Driver";

    // Get database connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found! " +
                    "Make sure postgresql-42.7.1.jar is in WEB-INF/lib/");
        }
    }

    // Close connection
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Close PreparedStatement
    public static void closeStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Close ResultSet
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
