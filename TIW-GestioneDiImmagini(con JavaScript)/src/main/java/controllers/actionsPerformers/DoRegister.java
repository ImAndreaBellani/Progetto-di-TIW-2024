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

import beans.dbObjects.User;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.UserDAO;

@MultipartConfig
public class DoRegister extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoRegister()
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
				response.getWriter().append("Served at: ").append(request.getContextPath()); //la servlet non permette l'invio di richieste "GET"
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{				
				UserDAO user_dao = new UserDAO(connection);
				
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				try
					{
						User new_user = new User(
								request.getParameter("username"),
								request.getParameter("password"),
								request.getParameter("password1"),
								request.getParameter("mail")
						);
						
						user_dao.createUser(new_user);
						request.getSession().setAttribute("username", request.getParameter("username"));
						
						response.setStatus(HttpServletResponse.SC_OK);
						response.setContentType("text/plain");
						response.getWriter().println(request.getParameter("username"));	
					}
				catch (BadFormatException e)
					{
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						response.getWriter().println(e.getMessage());
					}
				catch (DbErrorException e)
					{
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
