package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        /*
         * Get the existing login session.
         */
        HttpSession session = request.getSession(false);

        /*
         * If the user is not logged in,
         * send loggedIn = false.
         */
        if (session == null ||
            session.getAttribute("userId") == null) {

            out.print("{\"loggedIn\":false}");
            return;
        }

        /*
         * Get the ID of the currently logged-in user.
         */
        int userId = (Integer) session.getAttribute("userId");

        String sql =
                "SELECT user_id, name, email, phone, role " +
                "FROM users " +
                "WHERE user_id = ?";

        try (
            Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String role = rs.getString("role");

                out.print("{");

                out.print("\"loggedIn\":true,");

                out.print("\"userId\":" +
                        rs.getInt("user_id") + ",");

                out.print("\"name\":\"" +
                        escape(name) + "\",");

                out.print("\"email\":\"" +
                        escape(email) + "\",");

                out.print("\"phone\":\"" +
                        escape(phone) + "\",");

                out.print("\"role\":\"" +
                        escape(role) + "\"");

                out.print("}");

            } else {

                out.print("{\"loggedIn\":false}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"error\":\"Unable to load profile\"}"
            );
        }
    }


    /*
     * Prevent invalid characters from
     * breaking the JSON response.
     */
    private String escape(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}