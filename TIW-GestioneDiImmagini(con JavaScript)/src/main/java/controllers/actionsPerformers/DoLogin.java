package controllers.actionsPerformers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import daos.MyDAO;

import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.UserDAO;

@MultipartConfig
public class DoLogin extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoLogin()
		    {
		        super();
		    }
	    
	    public void init() throws ServletException
		    {
				try
					{
						String driver = MyDAO.DB_DRIVER_CLASS_NAME;
						String url = MyDAO.DB_URL;
						String user = MyDAO.DB_ADMIN;
						String password = MyDAO.DB_ADMIN_PASSWORD;
						Class.forName(driver);
						connection = DriverManager.getConnection(url, user, password);
					}
				catch (ClassNotFoundException e)
					{
						throw new ServletException("Can't load database driver");
					}
				catch (SQLException e)
					{
						throw new ServletException("Couldn't get db connection");
					}
			}
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				response.getWriter().append("Served at: ").append(request.getContextPath());
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{					
				UserDAO user_dao = new UserDAO(connection);
				
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				
				try
					{
						String username = user_dao.checkAutentication(request.getParameter("username"), request.getParameter("password"));						
						request.getSession().setAttribute("username", username);
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType("text/plain");
						response.getWriter().println(username);
					}
				catch (BadFormatException e)
					{
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(e.getMessage());
					}
				catch (DbErrorException e)
					{
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						response.getWriter().println(e.getMessage());
					}				
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
