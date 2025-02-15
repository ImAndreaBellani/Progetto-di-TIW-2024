package controllers.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * QUESTA SERVLET NON E' (PIU') MAPPATA.
 * 
 * Aveva lo scopo di fornire un messaggio "personalizzato" e una conseguente redirezione nel momento in cui
 * l'utente (loggato o meno) richiede una risorsa non mappata.
 */
//@WebServlet("/")
public class NotFoundServletErrorHandler extends HttpServlet
	{
		private static final long serialVersionUID = 1L;
		
		public NotFoundServletErrorHandler()
		    {
		        super();
		    }
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
			{
				String dest = getServletContext().getContextPath();
				
				if (request.getSession().getAttribute("username")!=null)
					{
						dest = dest + "/Homepage";
					}
				else
					{
						dest = dest + "/Register";
					}
				
				response.sendRedirect(dest + "?error_message=la risorsa richiesta non esiste o non e' disponibile");
			}
	}