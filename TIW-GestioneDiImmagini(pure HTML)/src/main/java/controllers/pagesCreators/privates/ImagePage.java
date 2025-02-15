package controllers.pagesCreators.privates;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
import daos.ImageDAO;
import daos.MyDAO;

public class ImagePage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public ImagePage()
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
				String next_page_path = getServletContext().getContextPath();
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");

				if (request.getSession().getAttribute("username")==null)
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Register?error_message=Non sei autorizzato ad accedere a questa pagina");
					}
				else
					{
						ImageDAO image_dao = new ImageDAO(connection);
						
						try
							{
								Image image = image_dao.getImage(
									request.getParameter("user"), 
									request.getParameter("name"),
									request.getSession().getAttribute("username").toString()
								);
																
								WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
								ctx.setVariable("image_author", image.getCreator());
								ctx.setVariable("date", "Caricata il: " + image.getDate());
								ctx.setVariable("name", image.getName());
								ctx.setVariable("description", image.getText());
								ctx.setVariable("path", "/TIW-GestioneDiImmagini(pure%20HTML)/ImagesHandler/"+Image.decode(image.getPath()));
								ctx.setVariable("belongs_to_me", image.getCreator().equalsIgnoreCase(request.getSession().getAttribute("username").toString()));
								ctx.setVariable("returnAddress", request.getParameter("returnAddress"));
								ctx.setVariable("fixed_comment_box_height", image.getComments().size()<2?"100px":"200px");
							//	ctx.setVariable("hide_return",  request.getParameter("returnAddress").equals(Album.empty_placeholder));
								
								ctx.setVariable("comments", image.getComments());
								String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
								ctx.setVariable("error_message", error_msg);
								
								templateEngine.process("/WEB-INF/pages/privates/image_page.html", ctx, response.getWriter());
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
