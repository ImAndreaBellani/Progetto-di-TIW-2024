package controllers.actionsPerformers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.annotation.MultipartConfig;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import daos.MyDAO;
import beans.dbObjects.Image;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import daos.ImageDAO;

@MultipartConfig
public class DoUploadImage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		
	    public DoUploadImage()
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
						Part filePart = request.getPart("image");
							
						if (filePart == null || filePart.getSize() <= 0)
						   	{
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								response.getWriter().println("non e' stato selezionato alcun file");
						   	}
						else
							  {
						    		if (!filePart.getContentType().startsWith("image")) //"il file caricato è un'immagine?"
						    			{
						    				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						    				response.getWriter().println("il file scelto non e' un'immagine");
						    			}
						    		else
							    		{		
						    				try 
						    					{
						    						//"dbPath" sarà il path che verrà scritto nella base di dati
							    					String dbPath = Image.encode(
								    						request.getSession().getAttribute("username").toString(),
								    						request.getParameter("name"),
								    						filePart.getSubmittedFileName()
								    				);
							    					
						    						try
														{
															ImageDAO image_dao = new ImageDAO(connection);
															
															image_dao.uploadImage(
																	request.getParameter("name"),
																	request.getSession().getAttribute("username").toString(),
																	request.getParameter("description"),
																	dbPath
															);
															
															File dir_maker = new File(getServletContext().getInitParameter("images_folder")+
														    		"\\"+URLEncoder.encode(request.getSession().getAttribute("username").toString(), "UTF-8")+
														    		"\\"+URLEncoder.encode(request.getParameter("name"), "UTF-8")
														    );
															
															dir_maker.mkdirs();
															
										    				File file = new File(getServletContext().getInitParameter("images_folder")+"\\"+dbPath);
															Files.copy(filePart.getInputStream(), file.toPath());
															
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
						    				catch (IOException | java.lang.IllegalArgumentException e)
						    					{
						    						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						    						response.getWriter().println(e.getMessage());
						    					}
							    		}
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