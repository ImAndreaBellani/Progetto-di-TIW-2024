package controllers.actionsPerformers;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import daos.MyDAO;

import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.AlbumDAO;

@MultipartConfig
public class DoSortAlbum extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoSortAlbum()
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
				this.doPost(request, response); //la servlet è progettata per richieste "POST", "tollera" comunque anche richieste "GET"
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
					    		String[] sorting = request.getParameterValues("sorting");
						    	if (sorting==null)
									{
										throw (new BadFormatException("ordinamento", null, "l'ordinamento e' obbligatorio"));
									}
						    	
								AlbumDAO album_dao = new AlbumDAO(connection);
								
								album_dao.sortAlbum(request.getSession().getAttribute("username").toString(),
										request.getParameter("user"),
										request.getParameter("name"),
										request.getParameterValues("sorting")
								);
								
								response.setStatus(HttpServletResponse.SC_OK);
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
