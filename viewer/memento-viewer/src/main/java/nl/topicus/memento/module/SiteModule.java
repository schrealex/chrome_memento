package nl.topicus.memento.module;

import java.util.UUID;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;

public class SiteModule extends AbstractModule
{

	@Override
	protected void configure()
	{

	}

	@RequestScoped
	@Provides
	@Named("randomString")
	protected String getRandomString()
	{
		return UUID.randomUUID().toString();
	}

}
