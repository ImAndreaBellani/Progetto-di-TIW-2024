package controllers.pagesCreators.privates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beans.dbObjects.Image;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.AlbumDAO;
import daos.MyDAO;

public class Homepage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public Homepage()
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
				this.doPost(request, response);
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
								AlbumDAO album_dao = new AlbumDAO(connection);
								
								Map<String, Object> gson_ret = album_dao.getUserResourcesInfos(request.getSession().getAttribute("username").toString());
								/*
								 * si reperiscono le informazioni relative alle immagini dell'utente, ai suoi album e agli album degli altri utenti
								 */
								ArrayList<Image> images = (ArrayList<Image>) gson_ret.get("images_infos");
								images.stream().forEach(img -> {
									try {
										img.setPath("/TIW-GestioneDiImmagini(con%20JavaScript)/ImagesHandler/"+Image.decode(img.getPath()));
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								});
								
								response.setStatus(HttpServletResponse.SC_OK);
								response.setContentType("application/json");
								Gson gson = new GsonBuilder().disableHtmlEscaping().create();
								String json = gson.toJson(gson_ret);
								response.getWriter().println(json);
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
