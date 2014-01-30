package it.hakvoort.jndi;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

public class PropertiesBasedInitialContextFactory implements InitialContextFactory
{

	private static final Logger logger = LoggerFactory.getLogger(PropertiesBasedInitialContextFactory.class);

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
	{
		String providerUrl = String.valueOf(environment.get(Context.PROVIDER_URL));

		logger.debug("Loading initial context from {}", providerUrl);

		URL resource = Resources.getResource(providerUrl);

		Properties properties = new Properties();
		try
		{
			properties.load(resource.openStream());
		}
		catch (IOException e)
		{
			throw new NoInitialContextException(
					"No initial context can be created due to a failure reading the context provider url (" + e.getMessage()
							+ ")");
		}

		return new PropertiesBasedContext(properties);
	}
}
