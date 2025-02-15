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

import beans.dbObjects.Album;
import beans.dbObjects.Image;
import daos.AlbumDAO;
import daos.MyDAO;
import beans.exceptions.BadFormatException;
import beans.exceptions.DbErrorException;
import beans.utils.Pair;

public class AlbumPage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public AlbumPage()
		    {
		        super();
		    }
	    public void init() throws ServletException
		    {
		    	ServletContext servletContext = getServletContext();
	    		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
	    		templateResolver.setTemplateMode(TemplateMode.HTML);
	    		this.templateEngine = new TemplateEngine();
	    		this.templateEngine.setTemplateResolver(templateResolver);
	    		templateResolver.setCharacterEncoding("UTF-8");
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
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				String next_page_path = getServletContext().getContextPath();
				
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{
						AlbumDAO album_dao = new AlbumDAO(connection);
						
						try
							{
								final Album album = album_dao.getUserAlbum(request.getParameter("user"), request.getParameter("name"));
								
								//il valore di "start" presente (o meno) nella richiesta viene "corretto"
								int fixed_start = Album.fixStart(request.getParameter("start"), album.getImagesInfos().size());
								
								WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
								ctx.setVariable("name", album.getName());
								ctx.setVariable("author", album.getCreator());
								ctx.setVariable("date", album.getDate());
								
								//si prendono solo le "5" (al più) immagini richieste
								List<Pair<String>> images_infos = album.getImages(fixed_start,fixed_start+5).stream()
										.map(p -> {
											if (p.getElem1() != null)
												{
													try
														{
															return new Pair<String>(p.getElem1(),"/TIW-GestioneDiImmagini(pure%20HTML)/ImagesHandler/"+Image.decode(p.getElem2()));
														}
													catch (UnsupportedEncodingException e)
														{
															return p;
														}
												}
											else
												{
													return (p);
												}
										})
										.toList();
								
								ctx.setVariable("images_infos", images_infos);
				
								ctx.setVariable("is_last_page", fixed_start+5>=album.getImagesInfos().size());
								ctx.setVariable("is_first_page", fixed_start==0);
								ctx.setVariable("belongs_to_me", request.getSession().getAttribute("username").toString().equalsIgnoreCase(album.getCreator()));
								ctx.setVariable("fixed_start", fixed_start);
								
								String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
								ctx.setVariable("error_message", error_msg);								
								
								templateEngine.process("/WEB-INF/pages/privates/album_page.html", ctx, response.getWriter());
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
	
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				this.doGet(request, response); //la servlet "tollera" anche richieste "POST"
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
