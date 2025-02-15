package controllers.actionsPerformers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import daos.MyDAO;

import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.AlbumDAO;

public class DoDeleteAlbum extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoDeleteAlbum()
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
				response.getWriter().append("Served at: ").append(request.getContextPath()); //la servlet non accetta richieste "GET"
			}
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{			    
					    try
							{
								AlbumDAO album_dao = new AlbumDAO(connection);
								
								album_dao.deleteAlbum(request.getSession().getAttribute("username").toString(), request.getParameter("name"));	
								response.sendRedirect(getServletContext().getContextPath() + "/Homepage");
							}
					    catch (BadFormatException e)
							{
					    		response.sendRedirect(getServletContext().getContextPath()
					    				+ "/AlbumPage?name="+URLEncoder.encode(request.getParameter("name"), "UTF-8")
					    				+ "&user="+URLEncoder.encode(request.getSession().getAttribute("username").toString(), "UTF-8")
					    				+ "&error_message="+URLEncoder.encode(e.getMessage(), "UTF-8"));
							}
						catch (DbErrorException e)
							{
								response.sendRedirect(getServletContext().getContextPath()
				    				+ "/AlbumPage?name="+URLEncoder.encode(request.getParameter("name"), "UTF-8")
				    				+ "&user="+URLEncoder.encode(request.getSession().getAttribute("username").toString(), "UTF-8")
				    				+ "&error_message="+URLEncoder.encode(e.getMessage(), "UTF-8"));
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
