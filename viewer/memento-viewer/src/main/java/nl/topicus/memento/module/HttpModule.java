package nl.topicus.memento.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.twitter.common.application.ShutdownRegistry;
import com.twitter.common.application.modules.LifecycleModule;
import com.twitter.common.application.modules.LifecycleModule.LaunchException;
import com.twitter.common.application.modules.LifecycleModule.ServiceRunner;
import com.twitter.common.application.modules.LocalServiceRegistry.LocalService;
import com.twitter.common.base.Command;

public class HttpModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		requireBinding(ShutdownRegistry.class);
		LifecycleModule.bindServiceRunner(binder(), HttpLauncher.class);
	}

	private static final class HttpLauncher implements ServiceRunner
	{
		@Inject
		private HttpServerDispatch dispatch;

		@Inject
		@Named("http.port")
		private int port;

		@Override
		public LocalService launch() throws LaunchException
		{
			dispatch.listen(port);

			Command shutdownCommand = new Command()
			{

				@Override
				public void execute() throws RuntimeException
				{
					dispatch.stop();
				}
			};

			return LocalService.primaryService(port, shutdownCommand);
		}
	}
}
