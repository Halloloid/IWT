package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

/**
 * Returns the logged-in user's profile + their item listings as JSON.
 * Called as: GET /ProfileServlet
 * Requires active session with "userEmail" attribute.
 */
@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Guard: must be logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.sendRedirect("signin.html");
            return;
        }

        String userEmail = (String) session.getAttribute("userEmail");

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // User profile
            ps = conn.prepareStatement(
                "SELECT full_name, email, phone, hostel_block, year_of_study, branch " +
                "FROM users WHERE email = ?");
            ps.setString(1, userEmail);
            rs = ps.executeQuery();

            if (!rs.next()) {
                res.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }

            String fullName = rs.getString("full_name");
            String email    = rs.getString("email");
            String phone    = rs.getString("phone");
            String hostel   = rs.getString("hostel_block");
            int    year     = rs.getInt("year_of_study");
            String branch   = rs.getString("branch");

            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(ps);

            // User's items
            ps = conn.prepareStatement(
                "SELECT item_id, title, category, condition_type, price, image_path, status " +
                "FROM items WHERE seller_email = ? ORDER BY posted_at DESC");
            ps.setString(1, userEmail);
            rs = ps.executeQuery();

            StringBuilder itemsArr = new StringBuilder("[");
            int count = 0;
            boolean first = true;
            while (rs.next()) {
                count++;
                if (!first) itemsArr.append(",");
                first = false;
                itemsArr.append("{")
                    .append("\"itemId\":"         ).append(rs.getInt("item_id")).append(",")
                    .append("\"title\":\""         ).append(esc(rs.getString("title"))).append("\",")
                    .append("\"category\":\""      ).append(esc(rs.getString("category"))).append("\",")
                    .append("\"conditionType\":\"" ).append(esc(rs.getString("condition_type"))).append("\",")
                    .append("\"price\":"           ).append(rs.getDouble("price")).append(",")
                    .append("\"imagePath\":"       ).append(rs.getString("image_path") != null
                            ? "\"" + esc(rs.getString("image_path")) + "\"" : "null").append(",")
                    .append("\"status\":\""        ).append(esc(rs.getString("status"))).append("\"")
                    .append("}");
            }
            itemsArr.append("]");

            String json = "{"
                + "\"fullName\":\""   + esc(fullName)             + "\","
                + "\"email\":\""      + esc(email)                + "\","
                + "\"phone\":\""      + esc(phone  != null ? phone  : "") + "\","
                + "\"hostelBlock\":\"" + esc(hostel != null ? hostel : "") + "\","
                + "\"yearOfStudy\":"  + year                      + ","
                + "\"branch\":\""     + esc(branch != null ? branch : "") + "\","
                + "\"itemCount\":"    + count                     + ","
                + "\"items\":"        + itemsArr.toString()
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
