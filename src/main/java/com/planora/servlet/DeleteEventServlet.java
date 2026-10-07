package com.planora.servlet;

import com.planora.util.DBconnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/delete-event")
public class DeleteEventServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        if(!"ADMIN".equals(req.getSession().getAttribute("role"))){ resp.sendError(403); return; }
        try(Connection con=DBconnection.getConnection();
            PreparedStatement ps=con.prepareStatement("DELETE FROM events WHERE event_id=?")){
            ps.setInt(1,Integer.parseInt(req.getParameter("id")));
            ps.executeUpdate();
            resp.sendRedirect("../../admin-dashboard.html?deleted=1");
        } catch(Exception e){ resp.sendRedirect("../../admin-dashboard.html?error=1"); }
    }
}
