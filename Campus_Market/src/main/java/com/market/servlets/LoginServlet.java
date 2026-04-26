package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Map<String, String> body = readRequestData(req);
        String email = body.get("email");
        String password = body.get("password");

        // Validate required fields
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            res.sendRedirect("signin.html?error=failed");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT sic, full_name FROM users " +
                    "WHERE email = ? AND password = ?";

            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Login successful
                HttpSession session = req.getSession();
                session.setAttribute("userId", rs.getString("sic"));
                session.setAttribute("userName", rs.getString("full_name"));
                session.setAttribute("userEmail", email);

                res.sendRedirect("home.html");
            } else {
                res.sendRedirect("signin.html?error=invalid");
            }

            DBConnection.closeResultSet(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("signin.html?error=database");
        } finally {
            DBConnection.closeStatement(ps);
            DBConnection.closeConnection(conn);
        }
    }

    private Map<String, String> readRequestData(HttpServletRequest req) throws IOException {
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("application/json")) {
            return parseJsonObject(req.getReader());
        }

        Map<String, String> data = new HashMap<>();
        data.put("email", req.getParameter("email"));
        data.put("password", req.getParameter("password"));
        return data;
    }

    private Map<String, String> parseJsonObject(BufferedReader reader) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> values = new HashMap<>();
        String json = body.toString().trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            if (!json.trim().isEmpty()) {
                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
                    if (keyValue.length == 2) {
                        String key = cleanJsonValue(keyValue[0]);
                        String value = cleanJsonValue(keyValue[1]);
                        values.put(key, value);
                    }
                }
            }
        }
        return values;
    }

    private String cleanJsonValue(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
