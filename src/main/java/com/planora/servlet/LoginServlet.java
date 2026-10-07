package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req,
                           HttpServletResponse resp)
            throws ServletException, IOException {

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login == null || password == null ||
            login.trim().isEmpty() || password.trim().isEmpty()) {

            resp.sendRedirect("login.html?error=1");
            return;
        }

        String sql =
                "SELECT user_id, name, email, phone, role " +
                "FROM users " +
                "WHERE (email = ? OR phone = ? " +
                "OR CAST(user_id AS CHAR) = ?) " +
                "AND password = ?";

        try (
                Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, login.trim());
            ps.setString(2, login.trim());
            ps.setString(3, login.trim());
            ps.setString(4, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    /*
                     * Get/create the user's session.
                     */
                    HttpSession session = req.getSession(true);

                    /*
                     * Change the session ID after successful login.
                     * This keeps the same session data while giving
                     * the authenticated session a new ID.
                     */
                    req.changeSessionId();

                    /*
                     * Store all logged-in user information.
                     */
                    session.setAttribute(
                            "userId",
                            rs.getInt("user_id")
                    );

                    session.setAttribute(
                            "userName",
                            rs.getString("name")
                    );

                    session.setAttribute(
                            "email",
                            rs.getString("email")
                    );

                    session.setAttribute(
                            "phone",
                            rs.getString("phone")
                    );

                    session.setAttribute(
                            "role",
                            rs.getString("role")
                    );

                    /*
                     * Keep session active for 30 minutes
                     * between requests.
                     */
                    session.setMaxInactiveInterval(30 * 60);

                    /*
                     * Redirect according to role.
                     */
                    if ("ADMIN".equalsIgnoreCase(
                            rs.getString("role"))) {

                        resp.sendRedirect(
                                req.getContextPath()
                                + "/admin-dashboard.html"
                        );

                    } else {

                        resp.sendRedirect(
                                req.getContextPath()
                                + "/dashboard.html"
                        );
                    }

                } else {

                    resp.sendRedirect(
                            "login.html?error=1"
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new ServletException(
                    "Login failed because of database error.",
                    e
            );
        }
    }
}