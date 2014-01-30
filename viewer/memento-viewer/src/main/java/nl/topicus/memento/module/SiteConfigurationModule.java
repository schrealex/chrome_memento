package nl.topicus.memento.module;

import java.io.File;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.ini4j.Ini;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.synsere.module.ConfigurationModule;

public class SiteConfigurationModule extends ConfigurationModule
{
	/** */
	private static final Logger logger = LoggerFactory.getLogger(SiteConfigurationModule.class);

	@Override
	protected void configure()
	{
		quickBind(String.class, "memento.web.root", "web.root");
		quickBind(Integer.class, "memento.http.port", "http.port");
		quickBind(String.class, "memento.storage.root", "storage.root");
		quickBind(String.class, "memento.security.username", "security.username");
		quickBind(String.class, "memento.security.password", "security.password");

	}


	@Provides
	@Named("storage.folder")
	protected File provideStorageFolder(@Named("storage.root") String storageRoot)
	{
		File file = new File(storageRoot);

		if(!file.exists())
		{
			logger.info("Storate folder does not exist, trying to create folder.");
			
			if(file.mkdir())
			{
				logger.info("Created storate folder '{}'.", file.getAbsoluteFile());
			}
			else
			{
				logger.info("Creating storate folder '{}' failed.", file.getAbsoluteFile());
			}
		}

		return file;
	}
}
