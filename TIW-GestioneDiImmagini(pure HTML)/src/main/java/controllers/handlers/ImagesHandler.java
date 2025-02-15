package controllers.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.exceptions.BadFormatException;
import daos.ImageDAO;
import daos.MyDAO;

@WebServlet("/ImagesHandler/*")
public class ImagesHandler extends HttpServlet
	{
		private static final long serialVersionUID = 1L;

		private String folderPath = "";

		private Connection connection = null;
		
	    public void init() throws ServletException
		    {	
	    		this.folderPath = getServletContext().getInitParameter("images_folder");
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
				if (request.getSession().getAttribute("username")!=null)
					{
						try
							{
								String filename = request.getPathInfo().substring(1);
								
								StringTokenizer st = new StringTokenizer(filename, "/");
								String user = URLDecoder.decode(st.nextToken(), "UTF-8");
								String name = URLDecoder.decode(st.nextToken(), "UTF-8");
								
								if (!(request.getSession().getAttribute("username").toString()).equalsIgnoreCase(user))
									{
										ImageDAO imageDAO = new ImageDAO(connection);
										
										try
											{
												imageDAO.isPrivate(name, user);
											}
										catch (BadFormatException e)
											{
												response.sendRedirect(getServletContext().getContextPath()+"/Homepage?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8"));
											}
									}

								File file = new File(folderPath, filename);
								response.setHeader("Content-Type", getServletContext().getMimeType(filename));
								response.setHeader("Content-Length", String.valueOf(file.length()));
						
								response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
								Files.copy(file.toPath(), response.getOutputStream());
							}
						catch (Exception e)
							{
								response.sendRedirect(getServletContext().getContextPath()+"/Homepage?error_message=L'immagine richiesta non e' disponibile o non esiste");
							}
					}
				else
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
			}
	}
