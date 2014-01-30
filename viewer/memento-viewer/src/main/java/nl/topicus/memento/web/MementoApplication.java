package nl.topicus.memento.web;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import nl.topicus.memento.web.page.HomePage;
import nl.topicus.memento.web.page.LoginPage;

import javax.inject.Singleton;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.shiro.annotation.AnnotationsShiroAuthorizationStrategy;
import org.wicketstuff.shiro.authz.ShiroUnauthorizedComponentListener;
import org.wicketstuff.shiro.page.LogoutPage;

@Singleton
public class MementoApplication extends WebApplication
{

	private static final Logger logger = LoggerFactory.getLogger(MementoApplication.class);

	@Override
	protected void init()
	{
		super.init();
		logger.debug("Initializing Memento viewer app");

		getMarkupSettings().setStripWicketTags(true);

        Bootstrap.install(this, new BootstrapSettings());

		{
			AnnotationsShiroAuthorizationStrategy strategy = new AnnotationsShiroAuthorizationStrategy();

			getSecuritySettings().setAuthorizationStrategy(strategy);
			getSecuritySettings().setUnauthorizedComponentInstantiationListener(
				new ShiroUnauthorizedComponentListener(LoginPage.class, LoginPage.class, strategy));
		}

		this.getPageSettings().addComponentResolver(new IComponentResolver()
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
			{
				Component result = new WebMarkupContainer(tag.getId());
				result.setRenderBodyOnly(true);
				return result;
			}
		});

        this.getResourceSettings().setThrowExceptionOnMissingResource(false);

		mountPage("/logout", LogoutPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return HomePage.class;
	}

}
