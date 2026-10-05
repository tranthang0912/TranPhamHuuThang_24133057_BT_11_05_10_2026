package huuthang.controller;

import huuthang.config.DatabaseConnection_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JspCompatibilityController_24133057 extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("databaseAvailable", DatabaseConnection_24133057.isAvailable());
        request.getRequestDispatcher("/WEB-INF/jsp/compatibility_24133057.jsp").forward(request, response);
    }
}
