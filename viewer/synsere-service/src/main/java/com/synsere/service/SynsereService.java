package com.synsere.service;

import java.util.Arrays;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.synsere.module.ConfigurationModule;
import com.synsere.module.JndiModule;
import com.twitter.common.application.Application;
import com.twitter.common.application.Lifecycle;
import com.twitter.common.application.StartupStage;
import com.twitter.common.application.modules.AppLauncherModule;
import com.twitter.common.application.modules.LifecycleModule;
import com.twitter.common.args.Arg;
import com.twitter.common.args.ArgFilters;
import com.twitter.common.args.ArgScanner;
import com.twitter.common.args.ArgScanner.ArgScanException;
import com.twitter.common.args.CmdLine;
import com.twitter.common.args.constraints.NotNull;
import com.twitter.common.base.ExceptionalCommand;

public class SynsereService implements Daemon
{
	private static final Logger logger = LoggerFactory.getLogger(SynsereService.class);

	@NotNull
	@CmdLine(name = "service_class", help = "Fully-qualified name of the application class, which must implement Application.")
	private static final Arg<Class<? extends Application>> SERVICE_CLASS = Arg.create();

	@CmdLine(name = "service_stage", help = "Guice development stage to create injector with.")
	private static final Arg<Stage> SERVICE_STAGE = Arg.create(Stage.DEVELOPMENT);

	@Inject
	@StartupStage
	private ExceptionalCommand startupCommand;

	@Inject
	private Lifecycle lifecycle;

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception
	{
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		DaemonController controller = context.getController();

		try
		{
			if(!new ArgScanner().parse(ArgFilters.selectClass(SynsereService.class),
				Arrays.asList(context.getArguments())))
			{
				controller.fail("Failed to parse arguments");
			}
		}
		catch(ArgScanException e)
		{
			controller.fail("Failed to scan arguments", e);
		}
		catch(IllegalArgumentException e)
		{
			controller.fail("Failed to apply arguments", e);
		}
	}

	@Override
	public void start() throws Exception
	{
		Application application = SERVICE_CLASS.get().newInstance();

		configureInjection(application);

		logger.info("Executing startup actions.");

		try
		{
			startupCommand.execute();
		}
		catch(Exception e)
		{
			logger.error("Startup action failed, quitting.", e);
			throw e;
		}

		application.run();
	}

	private void configureInjection(Application application)
	{
		Iterable<Module> modules = ImmutableList.<Module> builder().add(new LifecycleModule())
			.add(new AppLauncherModule()).addAll(application.getModules()).build();

		Injector rootInjector = null;
		if(application instanceof ConfigurableApplication)
		{
			ConfigurableApplication configurableApplication = (ConfigurableApplication) application;
			ConfigurationModule configurationModule = configurableApplication.getConfigurationModule();

			rootInjector = Guice.createInjector(SERVICE_STAGE.get(),
				Modules.combine(new JndiModule(), configurationModule));

			for(Module module : modules)
			{
				rootInjector.injectMembers(module);
			}
		}

		Injector injector;

		if(rootInjector != null)
		{
			injector = rootInjector.createChildInjector(Modules.combine(modules));
		}
		else
		{
			injector = Guice.createInjector(SERVICE_STAGE.get(), Modules.combine(modules));
		}

		injector.injectMembers(this);
		injector.injectMembers(application);
	}

	@Override
	public void stop() throws Exception
	{
		logger.info("Shutting down application.");

		lifecycle.shutdown();
		lifecycle.awaitShutdown();
	}

	@Override
	public void destroy()
	{
		logger.info("Destroying application.");
	}

}
