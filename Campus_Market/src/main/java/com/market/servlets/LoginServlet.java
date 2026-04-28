package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.net.URLEncoder;
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
                res.sendRedirect("home.html?email=" + URLEncoder.encode(email, "UTF-8"));
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
            json = json.substring(1, json.length() - 1).trim();
            boolean inQuotes = false;
            boolean isEscaped = false;
            StringBuilder current = new StringBuilder();
            java.util.List<String> tokens = new java.util.ArrayList<>();

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);
                if (isEscaped) {
                    current.append(c);
                    isEscaped = false;
                } else if (c == '\\') {
                    current.append(c);
                    isEscaped = true;
                } else if (c == '"') {
                    inQuotes = !inQuotes;
                    current.append(c);
                } else if (c == ',' && !inQuotes) {
                    tokens.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                tokens.add(current.toString());
            }

            for (String token : tokens) {
                int colonIndex = -1;
                inQuotes = false;
                isEscaped = false;
                for (int i = 0; i < token.length(); i++) {
                    char c = token.charAt(i);
                    if (isEscaped) {
                        isEscaped = false;
                    } else if (c == '\\') {
                        isEscaped = true;
                    } else if (c == '"') {
                        inQuotes = !inQuotes;
                    } else if (c == ':' && !inQuotes) {
                        colonIndex = i;
                        break;
                    }
                }

                if (colonIndex != -1) {
                    String key = cleanJsonValue(token.substring(0, colonIndex));
                    String value = cleanJsonValue(token.substring(colonIndex + 1));
                    values.put(key, value);
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
        return cleaned.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }
}
