package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/PostItemServlet")
@MultipartConfig(maxFileSize = 10485760) // 10MB
public class PostItemServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads/items";

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Check session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            res.sendRedirect("signin.html");
            return;
        }

        String title = req.getParameter("title");
        String category = req.getParameter("category");
        String condition = req.getParameter("condition");
        String description = req.getParameter("description");
        String priceStr = req.getParameter("price");

        // Validate required fields
        if (title == null || title.trim().isEmpty() ||
            category == null || category.trim().isEmpty() ||
            condition == null || condition.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            priceStr == null || priceStr.trim().isEmpty()) {
            res.sendRedirect("post-item.html?error=missing_fields");
            return;
        }

        // Handle file upload
        Part filePart = req.getPart("photo");
        String fileName = null;

        if (filePart != null && filePart.getSize() > 0) {
            fileName = System.currentTimeMillis() + "_" + getFileName(filePart);
            String uploadPath = getServletContext().getRealPath("") +
                    File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            filePart.write(uploadPath + File.separator + fileName);
        } else {
            // Image is required per database schema (image_path NOT NULL)
            res.sendRedirect("post-item.html?error=no_image");
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
            ps.setString(7, UPLOAD_DIR + "/" + fileName);

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

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "file";
    }
}
