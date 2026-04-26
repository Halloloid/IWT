package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

/**
 * Returns a single item as JSON for item-details.html.
 * Called as: GET /ItemDetailServlet?id=<item_id>
 */
@WebServlet("/ItemDetailServlet")
public class ItemDetailServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            res.getWriter().write("{\"error\":\"Missing item id\"}");
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            res.getWriter().write("{\"error\":\"Invalid item id\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql =
                "SELECT i.item_id, i.title, i.description, i.price, " +
                "i.category, i.condition_type, i.image_path, i.status, i.posted_at, " +
                "u.full_name AS seller_name, u.email AS seller_email, " +
                "u.hostel_block, u.phone AS seller_phone " +
                "FROM items i " +
                "JOIN users u ON i.seller_email = u.email " +
                "WHERE i.item_id = ?";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                res.getWriter().write("{\"error\":\"Item not found\"}");
                return;
            }

            String json = "{"
                + "\"itemId\":"         + rs.getInt("item_id")               + ","
                + "\"title\":\""        + esc(rs.getString("title"))         + "\","
                + "\"description\":\""  + esc(rs.getString("description"))   + "\","
                + "\"price\":"          + rs.getDouble("price")              + ","
                + "\"category\":\""     + esc(rs.getString("category"))      + "\","
                + "\"conditionType\":\"" + esc(rs.getString("condition_type")) + "\","
                + "\"imagePath\":"      + (rs.getString("image_path") != null
                        ? "\"" + esc(rs.getString("image_path")) + "\"" : "null") + ","
                + "\"status\":\""       + esc(rs.getString("status"))        + "\","
                + "\"sellerName\":\""   + esc(rs.getString("seller_name"))   + "\","
                + "\"sellerEmail\":\""  + esc(rs.getString("seller_email"))  + "\","
                + "\"sellerPhone\":"    + (rs.getString("seller_phone") != null
                        ? "\"" + esc(rs.getString("seller_phone")) + "\"" : "null") + ","
                + "\"hostelBlock\":"    + (rs.getString("hostel_block") != null
                        ? "\"" + esc(rs.getString("hostel_block")) + "\"" : "null")
                + "}";

            res.getWriter().write(json);

        } catch (SQLException e) {
            e.printStackTrace();
            res.getWriter().write("{\"error\":\"Database error\"}");
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(ps);
            DBConnection.closeConnection(conn);
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
