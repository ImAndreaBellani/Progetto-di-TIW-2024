package controllers.pagesCreators.privates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.dbObjects.FullAlbum;
import beans.dbObjects.Image;
import daos.AlbumDAO;
import daos.MyDAO;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;

@MultipartConfig
public class AlbumPage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public AlbumPage()
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
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{
						AlbumDAO album_dao = new AlbumDAO(connection);
						
						try
							{
								FullAlbum album_app = album_dao.getUserAlbum(request.getSession().getAttribute("username").toString(), request.getParameter("user"), request.getParameter("name"));
								album_app.getImages().stream().forEach(img -> {
									try
										{
											img.setPath("/TIW-GestioneDiImmagini(con%20JavaScript)/ImagesHandler/"+Image.decode(img.getPath()));
										}
									catch (UnsupportedEncodingException e)
										{
											e.printStackTrace();
										}
								});
								
								response.setStatus(HttpServletResponse.SC_OK);
								response.getWriter().println(new Gson().toJson(album_app));
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
