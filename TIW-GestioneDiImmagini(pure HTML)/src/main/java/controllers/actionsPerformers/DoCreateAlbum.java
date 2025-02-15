package controllers.actionsPerformers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.dbObjects.Album;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;

import daos.MyDAO;

import daos.AlbumDAO;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;

public class DoCreateAlbum extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoCreateAlbum()
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
						/*if (request.getParameterValues("choices")==null || request.getParameterValues("choices").length==0)
							{
								response.sendRedirect(getServletContext().getContextPath() + "/CreateAlbum?error_message="+"Nessuna immagine selezionata!");
							}
						else
							{*/
								try
									{							   
										AlbumDAO album_dao = new AlbumDAO(connection);
										
										album_dao.createAlbum(new Album(
												request.getParameterValues("choices"),
												new Date(Calendar.getInstance().getTime().getTime()),
												request.getParameter("title").toString(),
												request.getSession().getAttribute("username").toString()
										));
										
										response.sendRedirect(getServletContext().getContextPath()+"/Homepage");
									}
							    catch (BadFormatException e)
									{
							    		response.sendRedirect(getServletContext().getContextPath() + "/CreateAlbum?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8"));
									}
								catch (DbErrorException e)
									{
							    		response.sendRedirect(getServletContext().getContextPath() + "/CreateAlbum?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8"));
							    	}
							//}
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
