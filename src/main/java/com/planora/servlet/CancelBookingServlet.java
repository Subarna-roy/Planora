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

@WebServlet("/CancelBookingServlet")
public class CancelBookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();


        // ==========================================
        // CHECK LOGIN
        // ==========================================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            out.print("Please login first.");

            return;
        }


        // ==========================================
        // GET LOGGED-IN USER ID
        // ==========================================

        int userId =
                (Integer) session.getAttribute("userId");


        // ==========================================
        // GET BOOKING ID
        // ==========================================

        String bookingIdParameter =
                request.getParameter("bookingId");


        if (bookingIdParameter == null ||
            bookingIdParameter.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("Booking ID is required.");

            return;
        }


        int bookingId;

        try {

            bookingId =
                    Integer.parseInt(bookingIdParameter);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("Invalid booking ID.");

            return;
        }


        // ==========================================
        // DATABASE OPERATIONS
        // ==========================================

        Connection con = null;

        try {

            con = DBconnection.getConnection();

            /*
             * Start transaction.
             */
            con.setAutoCommit(false);


            // ==========================================
            // FIND BOOKING
            // ==========================================

            String selectSql =
                    "SELECT event_id, ticket_quantity " +
                    "FROM bookings " +
                    "WHERE booking_id = ? " +
                    "AND user_id = ? " +
                    "FOR UPDATE";


            int eventId;
            int quantity;


            try (PreparedStatement ps =
                         con.prepareStatement(selectSql)) {

                ps.setInt(1, bookingId);
                ps.setInt(2, userId);

                ResultSet rs =
                        ps.executeQuery();


                /*
                 * Booking does not exist OR
                 * does not belong to this user.
                 */

                if (!rs.next()) {

                    con.rollback();

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    out.print(
                            "Booking not found."
                    );

                    return;
                }


                eventId =
                        rs.getInt("event_id");

                quantity =
                        rs.getInt("ticket_quantity");
            }


            // ==========================================
            // RESTORE AVAILABLE TICKETS
            // ==========================================

            String updateEventSql =
                    "UPDATE events " +
                    "SET available_tickets = " +
                    "available_tickets + ? " +
                    "WHERE event_id = ?";


            try (PreparedStatement ps =
                         con.prepareStatement(updateEventSql)) {

                ps.setInt(1, quantity);
                ps.setInt(2, eventId);

                ps.executeUpdate();
            }


            // ==========================================
            // DELETE BOOKING
            // ==========================================

            String deleteSql =
                    "DELETE FROM bookings " +
                    "WHERE booking_id = ? " +
                    "AND user_id = ?";


            try (PreparedStatement ps =
                         con.prepareStatement(deleteSql)) {

                ps.setInt(1, bookingId);
                ps.setInt(2, userId);

                ps.executeUpdate();
            }


            // ==========================================
            // COMMIT
            // ==========================================

            con.commit();

            out.print(
                    "Booking cancelled successfully!"
            );


        } catch (Exception e) {

            e.printStackTrace();


            // ==========================================
            // ROLLBACK
            // ==========================================

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }


            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "Unable to cancel booking."
            );


        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);
                    con.close();

                }

            } catch (Exception closeError) {

                closeError.printStackTrace();
            }

        }
    }
}