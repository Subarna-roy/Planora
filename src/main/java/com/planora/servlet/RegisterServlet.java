package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name=req.getParameter("name");
        String email=req.getParameter("email");
        String phone=req.getParameter("phone");
        String password=req.getParameter("password");

        String sql="INSERT INTO users(name,email,phone,password,role) VALUES(?,?,?,?, 'USER')";

        try(Connection con=DBconnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,name);
            ps.setString(2,email);
            ps.setString(3,phone);
            ps.setString(4,password);
            ps.executeUpdate();
            resp.sendRedirect("login.html?registered=1");
        } catch(SQLException e) {
            resp.sendRedirect("signup.html?error=1");
        }
    }
}
