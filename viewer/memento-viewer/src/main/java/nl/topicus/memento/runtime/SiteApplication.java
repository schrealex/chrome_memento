package nl.topicus.memento.runtime;

import java.util.Arrays;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import nl.topicus.memento.module.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.synsere.module.ConfigurationModule;
import com.synsere.service.ConfigurableApplication;
import com.twitter.common.application.AbstractApplication;

public class SiteApplication extends AbstractApplication implements ConfigurableApplication
{
	/** */
	private static final Logger logger = LoggerFactory.getLogger(SiteApplication.class);

	@Inject
	private Provider<Liquibase> liquibaseProvider;

	@Override
	public void run()
	{
		Liquibase liquibase = liquibaseProvider.get();

		logger.info("Started Memento viewer");
		logger.info("Updating database to latest version");
		logger.info("{}", liquibase);

		try
		{
			liquibase.update("");
		}
		catch(LiquibaseException e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				liquibase.getDatabase().close();
			}
			catch(DatabaseException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public ConfigurationModule getConfigurationModule()
	{
		return new SiteConfigurationModule();
	}

	@Override
	public Iterable<? extends Module> getModules()
	{
		return Arrays.asList(new HttpModule(), new DatabaseModule(), new LiquibaseModule(), new SecurityModule(),
			new SiteBootstrapModule(), new SiteModule());
	}

}
