package controllers.actionsPerformers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import daos.MyDAO;

import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.UserDAO;

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
				response.getWriter().append("Served at: ").append(request.getContextPath()); //la servlet non accetta richieste di tipo "GET"
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				String next_page_path = getServletContext().getContextPath();
				
				UserDAO user_dao = new UserDAO(connection);
				
				try
					{	
						String username = user_dao.checkAutentication(request.getParameter("username"), request.getParameter("password"));

						next_page_path = next_page_path + "/Homepage";
						
						request.getSession().setAttribute("username", username);	
					}
				catch (BadFormatException e)
					{
						next_page_path = next_page_path + "/Login?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8");
					}
				catch (DbErrorException e)
					{
						next_page_path = next_page_path + "/Login?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8");
					}
				
				response.sendRedirect(next_page_path);
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
