package controllers.pagesCreators.privates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.dbObjects.Image;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import beans.utils.Pair;
import daos.ImageDAO;
import daos.MyDAO;

public class CreateAlbum extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public CreateAlbum()
		    {
		        super();
		    }
	    public void init() throws ServletException
		    {
		    	ServletContext servletContext = getServletContext();
	    		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
	    		templateResolver.setTemplateMode(TemplateMode.HTML);
	    		templateResolver.setCharacterEncoding("UTF-8");
	    		this.templateEngine = new TemplateEngine();
	    		this.templateEngine.setTemplateResolver(templateResolver);
				templateResolver.setSuffix(".html");
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
				String next_page_path = getServletContext().getContextPath();
				
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{
						try
							{
								ImageDAO image_dao = new ImageDAO(connection);
								
								List<Pair<String>> userImagesInfos =
										image_dao.getUserImagesInfos(request.getSession().getAttribute("username").toString())
										.stream()
										.map(p -> {
											try
												{
													return new Pair<String>(p.getElem1(),"/TIW-GestioneDiImmagini(pure%20HTML)/ImagesHandler/"+Image.decode(p.getElem2()));
												}
											catch (UnsupportedEncodingException e)
												{
													return p;
												}
										})
										.toList();
								
								WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
								ctx.setVariable("my_images", userImagesInfos);
								ctx.setVariable("fixed_table_height", userImagesInfos.size()<2?"150px":"300px"); //dimensionamento dinamico del box contenente le immagini dell'utente
								
								String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
								ctx.setVariable("error_message", error_msg);
								
								templateEngine.process("/WEB-INF/pages/privates/create_album.html", ctx, response.getWriter());
							}
						catch (BadFormatException e)
							{
								next_page_path = next_page_path + "/Homepage?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8");
								response.sendRedirect(next_page_path);
							}
						catch (DbErrorException e)
							{
								next_page_path = next_page_path + "/Homepage?error_message="+URLEncoder.encode(e.getMessage(), "UTF-8");
								response.sendRedirect(next_page_path);
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
