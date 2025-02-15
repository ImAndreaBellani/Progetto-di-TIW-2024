package controllers.actionsPerformers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import daos.MyDAO;
import beans.dbObjects.Comment;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.ImageDAO;

@MultipartConfig
public class DoPostComment extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoPostComment()
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
				this.doPost(request, response); //la servlet è progettata per rispondere a richieste "POST", ma tollera anche le "GET"
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{			    
					    try
							{
								ImageDAO image_dao = new ImageDAO(connection);
								Comment comment = new Comment(
									request.getSession().getAttribute("username").toString(),
									request.getParameter("name"),
									request.getParameter("user"),
									new Timestamp(System.currentTimeMillis()),
									request.getParameter("text")
								);
								image_dao.uploadComment(comment);
								
								response.setStatus(HttpServletResponse.SC_OK);
								response.setContentType("text/plain");
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
