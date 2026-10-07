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

@WebServlet("/UpdateProfileServlet")
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        /*
         * Get current login session.
         */
        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            out.print(
                    "{\"success\":false,\"message\":\"Please login first.\"}"
            );

            return;
        }


        /*
         * IMPORTANT:
         * Get user ID from session.
         * Do NOT get user ID from the browser.
         */
        int userId =
                (Integer) session.getAttribute("userId");


        /*
         * Get updated information.
         */
        String name =
                request.getParameter("name");

        String email =
                request.getParameter("email");

        String phone =
                request.getParameter("phone");


        /*
         * Basic validation.
         */
        if (name == null ||
            email == null ||
            phone == null ||
            name.trim().isEmpty() ||
            email.trim().isEmpty() ||
            phone.trim().isEmpty()) {

            out.print(
                    "{\"success\":false,\"message\":\"All fields are required.\"}"
            );

            return;
        }


        name = name.trim();
        email = email.trim();
        phone = phone.trim();


        String updateSql =
                "UPDATE users " +
                "SET name = ?, email = ?, phone = ? " +
                "WHERE user_id = ?";


        try (Connection con =
                     DBconnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(updateSql)) {


            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);


            int rows =
                    ps.executeUpdate();


            if (rows == 0) {

                out.print(
                        "{\"success\":false,\"message\":\"User not found.\"}"
                );

                return;
            }


            /*
             * Update session values too.
             * This keeps the session synchronized
             * with the database.
             */
            session.setAttribute(
                    "userName",
                    name
            );

            session.setAttribute(
                    "email",
                    email
            );

            session.setAttribute(
                    "phone",
                    phone
            );


            out.print(
                    "{\"success\":true,\"message\":\"Profile updated successfully.\"}"
            );


        } catch (Exception e) {

            e.printStackTrace();


            /*
             * Handle duplicate email/phone.
             */
            String message =
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Database error";


            if (message.contains("Duplicate") ||
                message.contains("duplicate")) {

                out.print(
                        "{\"success\":false,\"message\":\"Email or phone number is already registered.\"}"
                );

            } else {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                out.print(
                        "{\"success\":false,\"message\":\"Unable to update profile.\"}"
                );
            }
        }
    }
}