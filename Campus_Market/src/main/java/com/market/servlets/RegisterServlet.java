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

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Map<String, String> body = readRequestData(req);

        String sic      = body.get("sic");
        String fullName = body.get("full_name");
        String email    = body.get("email");
        String password = body.get("password");
        String phone    = body.get("phone");
        String hostel   = body.get("hostel_block");
        String yearStr  = body.get("year_of_study");
        String branch   = body.get("branch");

        String sicValue      = sic != null ? sic.trim() : "";
        String fullNameValue = fullName != null ? fullName.trim() : "";
        String emailValue    = email != null ? email.trim() : "";
        String phoneValue    = phone != null ? phone.trim() : "";
        String hostelValue   = hostel != null ? hostel.trim() : "";
        String branchValue   = branch != null ? branch.trim() : "";

        if (sicValue.isEmpty() ||
            fullNameValue.isEmpty() ||
            emailValue.isEmpty() ||
            password == null || password.trim().isEmpty() ||
            phoneValue.isEmpty()) {
            res.sendRedirect("signin.html?error=failed");
            return;
        }

        if (sicValue.length() != 7) {
            res.sendRedirect("signin.html?error=invalid_sic");
            return;
        }

        // Validate Silicon University email domain
        if (!emailValue.endsWith("@silicon.ac.in")) {
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

            // Check duplicate SIC or email before insert.
            checkStmt = conn.prepareStatement(
                "SELECT sic, email FROM users WHERE sic = ? OR email = ?");
            checkStmt.setString(1, sicValue);
            checkStmt.setString(2, emailValue);
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                if (sicValue.equalsIgnoreCase(rs.getString("sic").trim())) {
                    res.sendRedirect("signin.html?error=sic_exists");
                } else {
                    res.sendRedirect("signin.html?error=email_exists");
                }
                return;
            }

            // Insert new user
            String sql = "INSERT INTO users (sic, full_name, email, password, phone, hostel_block, year_of_study, branch) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(sql);
            insertStmt.setString(1, sicValue);
            insertStmt.setString(2, fullNameValue);
            insertStmt.setString(3, emailValue);
            insertStmt.setString(4, password); // TODO: hash in production
            insertStmt.setString(5, phoneValue);
            insertStmt.setString(6, hostelValue.isEmpty() ? null : hostelValue);
            insertStmt.setInt   (7, yearOfStudy);
            insertStmt.setString(8, branchValue.isEmpty() ? null : branchValue);

            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                res.sendRedirect("home.html?email=" + URLEncoder.encode(emailValue, "UTF-8"));
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

    private Map<String, String> readRequestData(HttpServletRequest req) throws IOException {
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("application/json")) {
            return parseJsonObject(req.getReader());
        }

        Map<String, String> data = new HashMap<>();
        data.put("sic", req.getParameter("sic"));
        data.put("full_name", req.getParameter("full_name"));
        data.put("email", req.getParameter("email"));
        data.put("password", req.getParameter("password"));
        data.put("phone", req.getParameter("phone"));
        data.put("hostel_block", req.getParameter("hostel_block"));
        data.put("year_of_study", req.getParameter("year_of_study"));
        data.put("branch", req.getParameter("branch"));
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
