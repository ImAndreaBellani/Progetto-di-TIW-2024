package controllers.pagesCreators.publics;

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

public class Login extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		private Connection connection = null;
		private TemplateEngine templateEngine;
		
	    public Login()
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
				request.setCharacterEncoding("UTF-8");
				response.setCharacterEncoding("UTF-8");
				if (request.getSession().getAttribute("username")!=null) //se la servlet viene chiamata quando esiste già una sessione, viene caricata la Homepage
					{
						response.sendRedirect(getServletContext().getContextPath()+"/Homepage");
					}
				else
					{
						WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
												
						String error_msg = request.getParameter("error_message")==null?"":request.getParameter("error_message");
						ctx.setVariable("error_message", error_msg);
						templateEngine.process("/WEB-INF/pages/publics/login.html", ctx, response.getWriter());
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
