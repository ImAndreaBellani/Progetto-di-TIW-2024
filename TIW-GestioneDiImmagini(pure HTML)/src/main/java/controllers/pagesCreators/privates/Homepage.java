package controllers.pagesCreators.privates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import daos.AlbumDAO;
import daos.MyDAO;

public class Homepage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public Homepage()
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
				if (request.getSession().getAttribute("username")==null)
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{
						try
							{
								AlbumDAO album_dao = new AlbumDAO(connection);
								
								List<List<Pair<String>>> ret = album_dao.getUserResourcesInfos(request.getSession().getAttribute("username").toString());
								
								/*
								 * si reperiscono le informazioni relative alle immagini dell'utente, ai suoi album e agli album degli altri utenti
								 */
								List<Pair<String>> userImagesInfos = ret.get(0)
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
								List<String> userAlbumNames = ret.get(1)
										.stream()
										.map(p -> p.getElem1())
										.toList();
								List<Pair<String>> nonUserAlbumNames = ret.get(2);	 				
								
								WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
								ctx.setVariable("username", request.getSession().getAttribute("username").toString());
								
								ctx.setVariable("my_images_infos", userImagesInfos);
								ctx.setVariable("my_album_names", userAlbumNames);
								ctx.setVariable("others_albums_infos", nonUserAlbumNames);
								
								String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
								ctx.setVariable("error_message", error_msg);
								
								templateEngine.process("/WEB-INF/pages/privates/homepage.html", ctx, response.getWriter());
							}
						catch (DbErrorException | BadFormatException e)
							{
								response.sendRedirect(getServletContext().getContextPath()+"/Login?error_message=Al momento non e' possibile accedere all'applicazione");
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
