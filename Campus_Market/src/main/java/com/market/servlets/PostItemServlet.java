package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/PostItemServlet")
public class PostItemServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Check session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            res.sendRedirect("signin.html");
            return;
        }

        Map<String, String> body = readRequestData(req);

        String title = body.get("title");
        String category = body.get("category");
        String condition = body.get("condition");
        String description = body.get("description");
        String priceStr = body.get("price");
        String imagePath = body.get("image_path");

        // Validate required fields
        if (title == null || title.trim().isEmpty() ||
            category == null || category.trim().isEmpty() ||
            condition == null || condition.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            priceStr == null || priceStr.trim().isEmpty() ||
            imagePath == null || imagePath.trim().isEmpty()) {
            res.sendRedirect("post-item.html?error=missing_fields");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "INSERT INTO items (seller_email, title, category, " +
                    "condition_type, description, price, image_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) session.getAttribute("userEmail"));
            ps.setString(2, title);
            ps.setString(3, category);
            ps.setString(4, condition);
            ps.setString(5, description);
            try {
                ps.setDouble(6, Double.parseDouble(priceStr));
            } catch (NumberFormatException e) {
                res.sendRedirect("post-item.html?error=invalid_price");
                return;
            }
            ps.setString(7, imagePath.trim());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                res.sendRedirect("home.html?success=posted");
            } else {
                res.sendRedirect("post-item.html?error=failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("post-item.html?error=database");
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
        data.put("title", req.getParameter("title"));
        data.put("category", req.getParameter("category"));
        data.put("condition", req.getParameter("condition"));
        data.put("description", req.getParameter("description"));
        data.put("price", req.getParameter("price"));
        data.put("image_path", req.getParameter("image_path"));
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
