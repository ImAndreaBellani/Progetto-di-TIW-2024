package controllers.actionsPerformers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DoLogout extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoLogout()
		    {
		        super();
		    }
	    
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				HttpSession session = request.getSession(false);
				if (session != null)
					{
						session.invalidate();
					}
				
				response.sendRedirect(getServletContext().getContextPath()+"/Login");
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				this.doGet(request, response);
			}
		public void destroy()
			{
				if (connection != null)
					{
						try
							{
								connection.close();
							}
						catch (SQLException ignored)
							{
							
							}
					}
			}
	}
