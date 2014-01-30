package nl.topicus.memento.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InvalidRequestServlet extends HttpServlet
{

	/** */
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		resp.setContentType("text/plain");
		resp.setContentType("UTF-8");
		resp.getWriter().append("404");
	}

}