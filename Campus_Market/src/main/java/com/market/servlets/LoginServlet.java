package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

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
}
