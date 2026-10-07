package com.planora.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Get the current session.
         */
        HttpSession session =
                request.getSession(false);


        /*
         * Destroy the session if it exists.
         */
        if (session != null) {

            session.invalidate();
        }


        /*
         * Redirect to login page with
         * logout success message.
         */
        response.sendRedirect(
                "login.html?logout=success"
        );
    }


    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }
}