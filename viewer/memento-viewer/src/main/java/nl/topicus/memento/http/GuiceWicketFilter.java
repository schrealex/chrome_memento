package nl.topicus.memento.http;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.Injector;
import com.google.inject.Provider;

@Singleton
public class GuiceWicketFilter extends WicketFilter
{

	@Inject
	private Provider<WebApplication> webApplicationProvider;

	@Inject
	private Injector injector;

	@Override
	protected IWebApplicationFactory getApplicationFactory()
	{
		return new IWebApplicationFactory()
		{
			@Override
			public WebApplication createApplication(WicketFilter filter)
			{
				WebApplication application = webApplicationProvider.get();

				application.getComponentInstantiationListeners().add(new GuiceComponentInjector(application, injector));

				return application;
			}

			@Override
			public void destroy(WicketFilter filter)
			{
			}
		};
	}

}
