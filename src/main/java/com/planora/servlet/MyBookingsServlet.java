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

@WebServlet("/MyBookingsServlet")
public class MyBookingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // ==========================================
        // CHECK LOGIN
        // ==========================================

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            out.print("{");
            out.print("\"loggedIn\":false,");
            out.print("\"bookings\":[],");
            out.print("\"message\":\"Please login first.\"");
            out.print("}");

            return;
        }

        // ==========================================
        // GET CURRENT USER ID
        // ==========================================

        int userId = (Integer) session.getAttribute("userId");

        // ==========================================
        // GET USER BOOKINGS
        // ==========================================

        String sql =
                "SELECT " +
                "b.booking_id, " +
                "b.event_id, " +
                "e.event_name, " +
                "e.artist, " +
                "e.event_date, " +
                "e.event_time, " +
                "e.venue, " +
                "e.ticket_price, " +
                "b.ticket_quantity, " +
                "b.total_price, " +
                "b.booking_date " +
                "FROM bookings b " +
                "INNER JOIN events e ON b.event_id = e.event_id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.booking_date DESC";

        try (
                Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                out.print("{");
                out.print("\"loggedIn\":true,");
                out.print("\"bookings\":[");

                boolean first = true;

                while (rs.next()) {

                    if (!first) {
                        out.print(",");
                    }

                    out.print("{");

                    out.print("\"bookingId\":"
                            + rs.getInt("booking_id") + ",");

                    out.print("\"eventId\":"
                            + rs.getInt("event_id") + ",");

                    out.print("\"eventName\":\""
                            + escape(rs.getString("event_name")) + "\",");

                    out.print("\"artist\":\""
                            + escape(rs.getString("artist")) + "\",");

                    out.print("\"eventDate\":\""
                            + rs.getString("event_date") + "\",");

                    out.print("\"eventTime\":\""
                            + rs.getString("event_time") + "\",");

                    out.print("\"venue\":\""
                            + escape(rs.getString("venue")) + "\",");

                    out.print("\"ticketPrice\":"
                            + rs.getDouble("ticket_price") + ",");

                    out.print("\"quantity\":"
                            + rs.getInt("ticket_quantity") + ",");

                    out.print("\"totalPrice\":"
                            + rs.getDouble("total_price") + ",");

                    out.print("\"bookingDate\":\""
                            + escape(
                                String.valueOf(
                                    rs.getTimestamp("booking_date")
                                )
                              )
                            + "\"");

                    out.print("}");

                    first = false;
                }

                out.print("]");
                out.print("}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("{");
            out.print("\"loggedIn\":true,");
            out.print("\"bookings\":[],");
            out.print("\"error\":\"Database error while loading bookings\"");
            out.print("}");
        }
    }

    // ==========================================
    // JSON ESCAPE
    // ==========================================

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