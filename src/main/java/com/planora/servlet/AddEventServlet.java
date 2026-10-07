package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/add-event")
public class AddEventServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        if(!isAdmin(req)){ resp.sendError(403); return; }

        String sql="INSERT INTO events(event_name,artist,event_date,event_time,venue,description,ticket_price,available_tickets,image_url) VALUES(?,?,?,?,?,?,?,?,?)";

        try(Connection con=DBconnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,req.getParameter("eventName"));
            ps.setString(2,req.getParameter("artist"));
            ps.setDate(3,Date.valueOf(req.getParameter("eventDate")));
            ps.setTime(4,Time.valueOf(req.getParameter("eventTime")+":00"));
            ps.setString(5,req.getParameter("venue"));
            ps.setString(6,req.getParameter("description"));
            ps.setDouble(7,Double.parseDouble(req.getParameter("ticketPrice")));
            ps.setInt(8,Integer.parseInt(req.getParameter("availableTickets")));
            ps.setString(9,req.getParameter("imageUrl"));
            ps.executeUpdate();
            resp.sendRedirect("../../admin-dashboard.html?added=1");
        } catch(Exception e) {
            resp.sendRedirect("../../add-event.html?error=1");
        }
    }

    private boolean isAdmin(HttpServletRequest req){
        return "ADMIN".equals(req.getSession().getAttribute("role"));
    }
}
