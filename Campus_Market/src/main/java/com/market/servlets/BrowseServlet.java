package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/BrowseServlet")
public class BrowseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String category = req.getParameter("category");
        String search   = req.getParameter("search");
        String format   = req.getParameter("format"); // "json" when called from home.html JS

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            StringBuilder sql = new StringBuilder(
                "SELECT i.item_id, i.title, i.description, i.price, " +
                "i.category, i.condition_type, i.image_path, i.posted_at, " +
                "u.full_name AS seller_name, u.hostel_block, u.email AS seller_email " +
                "FROM items i " +
                "JOIN users u ON i.seller_email = u.email " +
                "WHERE i.status = 'available' ");

            if (category != null && !category.trim().isEmpty())
                sql.append("AND i.category = ? ");
            if (search != null && !search.trim().isEmpty())
                sql.append("AND (i.title ILIKE ? OR i.description ILIKE ?) ");
            sql.append("ORDER BY i.posted_at DESC");

            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (category != null && !category.trim().isEmpty())
                ps.setString(idx++, category.trim());
            if (search != null && !search.trim().isEmpty()) {
                ps.setString(idx++, "%" + search.trim() + "%");
                ps.setString(idx++, "%" + search.trim() + "%");
            }

            rs = ps.executeQuery();

            // Always return JSON (home.html and item-details.html both use fetch())
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                json.append("{")
                    .append("\"itemId\":"        ).append(rs.getInt("item_id")).append(",")
                    .append("\"title\":\""       ).append(escJson(rs.getString("title"))).append("\",")
                    .append("\"description\":\"" ).append(escJson(rs.getString("description"))).append("\",")
                    .append("\"price\":"         ).append(rs.getDouble("price")).append(",")
                    .append("\"category\":\""    ).append(escJson(rs.getString("category"))).append("\",")
                    .append("\"conditionType\":\"").append(escJson(rs.getString("condition_type"))).append("\",")
                    .append("\"imagePath\":"     ).append(rs.getString("image_path") != null
                            ? "\"" + escJson(rs.getString("image_path")) + "\"" : "null").append(",")
                    .append("\"sellerName\":\""  ).append(escJson(rs.getString("seller_name"))).append("\",")
                    .append("\"sellerEmail\":\"" ).append(escJson(rs.getString("seller_email"))).append("\",")
                    .append("\"hostelBlock\":"   ).append(rs.getString("hostel_block") != null
                            ? "\"" + escJson(rs.getString("hostel_block")) + "\"" : "null")
                    .append("}");
            }
            json.append("]");
            res.getWriter().write(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            res.setContentType("application/json");
            res.getWriter().write("[]");
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(ps);
            DBConnection.closeConnection(conn);
        }
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
