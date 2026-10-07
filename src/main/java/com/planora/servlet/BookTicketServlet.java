package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/book")
public class BookTicketServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req,
                           HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        PrintWriter out = resp.getWriter();

        // ==========================================
        // CHECK LOGIN SESSION
        // ==========================================

        HttpSession session = req.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            out.println("<h2>User session not found.</h2>");
            out.println("<p>Please login again.</p>");
            out.println("<a href='login.html'>Login</a>");

            return;
        }

        // ==========================================
        // GET USER ID
        // ==========================================

        int userId;

        try {

            userId = (Integer) session.getAttribute("userId");

        } catch (Exception e) {

            out.println("<h2>Invalid user session.</h2>");
            out.println("<a href='login.html'>Login Again</a>");

            return;
        }


        // ==========================================
        // GET FORM PARAMETERS
        // ==========================================

        String eventIdParam =
                req.getParameter("eventId");

        String quantityParam =
                req.getParameter("quantity");


        if (eventIdParam == null ||
            quantityParam == null) {

            out.println("<h2>Booking Error</h2>");
            out.println("<p>Event ID or quantity is missing.</p>");

            return;
        }


        int eventId;
        int quantity;


        try {

            eventId =
                    Integer.parseInt(eventIdParam);

            quantity =
                    Integer.parseInt(quantityParam);

        } catch (NumberFormatException e) {

            out.println("<h2>Booking Error</h2>");
            out.println("<p>Invalid event ID or quantity.</p>");

            return;
        }


        // ==========================================
        // VALIDATE QUANTITY
        // ==========================================

        if (quantity < 1 || quantity > 10) {

            out.println("<h2>Booking Error</h2>");
            out.println("<p>Quantity must be between 1 and 10.</p>");

            return;
        }


        // ==========================================
        // SQL
        // ==========================================

        String selectSQL =
                "SELECT ticket_price, available_tickets " +
                "FROM events " +
                "WHERE event_id = ? " +
                "FOR UPDATE";


        String insertSQL =
                "INSERT INTO bookings " +
                "(user_id, event_id, ticket_quantity, total_price) " +
                "VALUES (?, ?, ?, ?)";


        String updateSQL =
                "UPDATE events " +
                "SET available_tickets = available_tickets - ? " +
                "WHERE event_id = ?";


        // ==========================================
        // DATABASE
        // ==========================================

        try (Connection con =
                     DBconnection.getConnection()) {

            con.setAutoCommit(false);


            try {

                double ticketPrice;
                int availableTickets;


                // ==================================
                // GET EVENT
                // ==================================

                try (PreparedStatement ps =
                             con.prepareStatement(selectSQL)) {

                    ps.setInt(1, eventId);

                    try (ResultSet rs =
                                 ps.executeQuery()) {

                        if (!rs.next()) {

                            con.rollback();

                            out.println("<h2>Booking Error</h2>");
                            out.println(
                                "<p>Event ID "
                                + eventId
                                + " was not found.</p>"
                            );

                            return;
                        }

                        ticketPrice =
                                rs.getDouble("ticket_price");

                        availableTickets =
                                rs.getInt("available_tickets");
                    }
                }


                // ==================================
                // CHECK STOCK
                // ==================================

                if (availableTickets < quantity) {

                    con.rollback();

                    out.println("<h2>Booking Error</h2>");

                    out.println(
                        "<p>Only "
                        + availableTickets
                        + " tickets are available.</p>"
                    );

                    return;
                }


                // ==================================
                // CALCULATE TOTAL
                // ==================================

                double totalPrice =
                        ticketPrice * quantity;


                // ==================================
                // INSERT BOOKING
                // ==================================

                try (PreparedStatement ps =
                             con.prepareStatement(insertSQL)) {

                    ps.setInt(1, userId);
                    ps.setInt(2, eventId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, totalPrice);

                    ps.executeUpdate();
                }


                // ==================================
                // UPDATE EVENT STOCK
                // ==================================

                try (PreparedStatement ps =
                             con.prepareStatement(updateSQL)) {

                    ps.setInt(1, quantity);
                    ps.setInt(2, eventId);

                    ps.executeUpdate();
                }


                // ==================================
                // COMMIT
                // ==================================

                con.commit();


                // ==================================
                // SUCCESS
                // ==================================

                resp.sendRedirect(
                    req.getContextPath()
                    + "/my-bookings.html?booked=1"
                );


            } catch (Exception e) {

                con.rollback();

                e.printStackTrace();

                out.println("<h2>Booking Failed</h2>");

                out.println(
                    "<p><b>Error:</b> "
                    + escapeHtml(e.getMessage())
                    + "</p>"
                );

                out.println(
                    "<p>Check the NetBeans Output window "
                    + "for the complete error.</p>"
                );

                out.println(
                    "<br><a href='events.html'>Back to Events</a>"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            out.println("<h2>Database Connection Error</h2>");

            out.println(
                "<p><b>Error:</b> "
                + escapeHtml(e.getMessage())
                + "</p>"
            );

            out.println(
                "<br><a href='events.html'>Back to Events</a>"
            );
        }
    }


    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }
}