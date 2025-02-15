package controllers.pagesCreators.privates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beans.dbObjects.Image;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.ImageDAO;
import daos.MyDAO;

@MultipartConfig
public class CreateAlbum extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public CreateAlbum()
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
								ImageDAO image_dao = new ImageDAO(connection);
								
								Map<String, Object> gson_ret = new HashMap<>();	
								
								ArrayList<Image> images = (ArrayList<Image>) image_dao.getAtomicUserImagesInfos(request.getSession().getAttribute("username").toString());
								images.stream().forEach(img -> {
									try
										{
											img.setPath("/TIW-GestioneDiImmagini(con%20JavaScript)/ImagesHandler/"+Image.decode(img.getPath()));
										}
									catch (UnsupportedEncodingException e)
										{
											e.printStackTrace();
										}
								});
								
								gson_ret.put("my_images", images);
								
								gson_ret.put("fixed_table_height", //dimensionamento dinamico del box contente le immagini dell'utente
										images.size()<2?(images.size()*150)+"px":"300px"			
								);
																
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
