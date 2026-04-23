package com.market.servlets;

import com.market.db.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Field names match signin.html register form exactly
        String fullName = req.getParameter("full_name");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String phone    = req.getParameter("phone");
        String hostel   = req.getParameter("hostel_block");
        String yearStr  = req.getParameter("year_of_study");
        String branch   = req.getParameter("branch");

        // Basic null/empty check
        if (fullName == null || fullName.trim().isEmpty() ||
            email    == null || email.trim().isEmpty()    ||
            password == null || password.trim().isEmpty()) {
            res.sendRedirect("signin.html?error=failed");
            return;
        }

        // Validate Silicon University email domain
        if (!email.trim().endsWith("@silicon.ac.in")) {
            res.sendRedirect("signin.html?error=invalid_email");
            return;
        }

        // Safely parse year_of_study (dropdown sends "1","2","3","4")
        int yearOfStudy = 0;
        try {
            if (yearStr != null && !yearStr.trim().isEmpty())
                yearOfStudy = Integer.parseInt(yearStr.trim());
        } catch (NumberFormatException e) {
            yearOfStudy = 0;
        }

        Connection conn        = null;
        PreparedStatement checkStmt  = null;
        PreparedStatement insertStmt = null;
        ResultSet rs           = null;

        try {
            conn = DBConnection.getConnection();

            // Check duplicate email
            checkStmt = conn.prepareStatement("SELECT user_id FROM users WHERE email = ?");
            checkStmt.setString(1, email.trim());
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                res.sendRedirect("signin.html?error=email_exists");
                return;
            }

            // Insert new user
            String sql = "INSERT INTO users (full_name, email, password, phone, hostel_block, year_of_study, branch) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(sql);
            insertStmt.setString(1, fullName.trim());
            insertStmt.setString(2, email.trim());
            insertStmt.setString(3, password);                              // TODO: hash in production
            insertStmt.setString(4, phone  != null ? phone.trim()  : "");
            insertStmt.setString(5, hostel != null ? hostel.trim() : "");
            insertStmt.setInt   (6, yearOfStudy);
            insertStmt.setString(7, branch != null ? branch.trim() : "");

            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                HttpSession session = req.getSession();
                session.setAttribute("userEmail", email.trim());
                session.setAttribute("userName",  fullName.trim());
                res.sendRedirect("home.html");
            } else {
                res.sendRedirect("signin.html?error=failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            res.sendRedirect("signin.html?error=database");
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(checkStmt);
            DBConnection.closeStatement(insertStmt);
            DBConnection.closeConnection(conn);
        }
    }
}
