package nl.topicus.memento.module;

import java.net.InetSocketAddress;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import nl.topicus.memento.http.InvalidRequestServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;

public class HttpServerDispatch
{
	private static final Logger logger = LoggerFactory.getLogger(HttpServerDispatch.class);

	private Server server;

	private int port;

	@Inject
	private GuiceFilter guiceFilter;

	/**
	 * Opens the HTTP server on the given port.
	 * 
	 * @param port
	 *            The port to listen on.
	 * @return {@code true} if the server started successfully on the port, {@code false} otherwise.
	 */
	public synchronized boolean listen(int port)
	{
		Preconditions.checkState(!isStarted(), "HttpServerDispatch has already been started on port: %d", this.port);

		Server server = new Server(new InetSocketAddress("0.0.0.0", port));

		ServletContextHandler handler = new ServletContextHandler();
		handler.setSessionHandler(new SessionHandler());

		handler.addServlet(new ServletHolder(new InvalidRequestServlet()), "/*");

		FilterHolder guiceFilterHolder = new FilterHolder(guiceFilter);

		handler.setContextPath("/");
		handler.addFilter(guiceFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));

		server.setHandler(handler);

		try
		{
			server.start();
			this.port = port;
			return true;
		}
		catch (Exception e)
		{
			logger.error("Could not start server on port {}", port, e);
			this.port = 0;
			this.server = null;
			return false;
		}
	}

	public synchronized boolean isStarted()
	{
		return (server != null) && server.isStarted();
	}

	public synchronized int getPort()
	{
		Preconditions.checkState(isStarted(), "HttpServer must be started before the port can be determined");
		return port;
	}

	/**
	 * Stops the HTTP server.
	 */
	public synchronized void stop()
	{
		if (isStarted())
		{
			try
			{
				server.stop();

				this.server = null;
			}
			catch (Exception e)
			{
				logger.error("Error stopping HTTPServer on {}", port, e);
			}
		}
	}

}
