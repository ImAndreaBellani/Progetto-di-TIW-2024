package controllers.pagesCreators.privates;

import java.io.IOException;
import java.sql.Connection;
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

public class UploadImage extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public UploadImage()
		    {
		        super();
		    }
	    public void init()
		    {
		    	ServletContext servletContext = getServletContext();
	    		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
	    		templateResolver.setTemplateMode(TemplateMode.HTML);
	    		templateResolver.setCharacterEncoding("UTF-8");
	    		this.templateEngine = new TemplateEngine();
	    		this.templateEngine.setTemplateResolver(templateResolver);
				templateResolver.setSuffix(".html");
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
						WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
						
						String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
						ctx.setVariable("error_message", error_msg);
						templateEngine.process("/WEB-INF/pages/privates/upload_image.html", ctx, response.getWriter());
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
