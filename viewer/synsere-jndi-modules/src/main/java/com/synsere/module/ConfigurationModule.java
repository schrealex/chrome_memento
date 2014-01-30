package com.synsere.module;

import com.google.inject.AbstractModule;
import com.google.inject.jndi.JndiIntegration;
import com.google.inject.name.Names;

public abstract class ConfigurationModule extends AbstractModule
{
	protected <T> void quickBind(Class<T> clazz, String key)
	{
		quickBind(clazz, key, key);
	}

	protected <T> void quickBind(Class<T> clazz, String from, String to)
	{
		bind(clazz).annotatedWith(Names.named(to)).toProvider(JndiIntegration.fromJndi(clazz, from));
	}
}
