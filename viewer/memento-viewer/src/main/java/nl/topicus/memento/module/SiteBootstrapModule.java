package nl.topicus.memento.module;

import nl.topicus.memento.http.GuiceShiroFilter;
import nl.topicus.memento.http.GuiceWicketFilter;
import nl.topicus.memento.rest.MementoResource;
import nl.topicus.memento.web.MementoApplication;

import java.util.Collections;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.eclipse.jetty.servlet.DefaultServlet;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class SiteBootstrapModule extends ServletModule
{
	@Inject
	@Named("web.root")
	private String webRoot;

	@Override
	protected void configureServlets()
	{
		/*
		 * Jersey
		 */
		Map<String, String> parameters = Maps.newHashMap();
		bind(GuiceContainer.class);

		parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES, MementoResource.class.getPackage().getName());

		serve("/api/*").with(GuiceContainer.class, parameters);

		bind(MementoResource.class).asEagerSingleton();

		/*
		 * Wicket
		 */
		bind(WebApplication.class).to(MementoApplication.class);

		Map<String, String> wicketInitParams = Collections.singletonMap(WicketFilter.FILTER_MAPPING_PARAM, "/*");

		bind(GuiceWicketFilter.class).in(Scopes.SINGLETON);
		bind(GuiceShiroFilter.class).in(Scopes.SINGLETON);

		filter("/*").through(GuiceShiroFilter.class);
		filter("/*").through(GuiceWicketFilter.class, wicketInitParams);

		/*
		 * Static content (could also be done elsewhere?)
		 */
		Map<String, String> servletInitParams = Maps.newHashMap();

		servletInitParams.put("resourceBase", webRoot);

		bind(DefaultServlet.class).in(Scopes.SINGLETON);

		serve("/assets/*").with(DefaultServlet.class, servletInitParams);
	}
}
