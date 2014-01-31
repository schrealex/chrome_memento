package nl.topicus.memento.web;

import java.io.File;

import javax.inject.Singleton;

import nl.topicus.memento.videoservice.VideoService;
import nl.topicus.memento.web.page.HomePage;
import nl.topicus.memento.web.page.LoginPage;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.resource.FileResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.shiro.annotation.AnnotationsShiroAuthorizationStrategy;
import org.wicketstuff.shiro.authz.ShiroUnauthorizedComponentListener;
import org.wicketstuff.shiro.page.LogoutPage;

import com.google.inject.Inject;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;

@Singleton
public class MementoApplication extends WebApplication
{
	@Inject
	private VideoService videoservice;

	private static final Logger logger = LoggerFactory.getLogger(MementoApplication.class);

	@Override
	protected void init()
	{
		super.init();
		logger.debug("Initializing Memento viewer app");

		getMarkupSettings().setStripWicketTags(true);

		getSharedResources().add("video", new FolderContentResource(videoservice.getStorageFolder()));
		mountResource("video", new SharedResourceReference("video"));

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

	static class FolderContentResource implements IResource
	{
		private final File rootFolder;

		public FolderContentResource(File rootFolder)
		{
			this.rootFolder = rootFolder;
		}

		public void respond(Attributes attributes)
		{
			PageParameters parameters = attributes.getParameters();
			String fileName = parameters.get(0).toString();
			File file = new File(rootFolder, fileName);
			FileResourceStream fileResourceStream = new FileResourceStream(file);
			ResourceStreamResource resource = new ResourceStreamResource(fileResourceStream);
			resource.respond(attributes);
		}
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return HomePage.class;
	}

}
